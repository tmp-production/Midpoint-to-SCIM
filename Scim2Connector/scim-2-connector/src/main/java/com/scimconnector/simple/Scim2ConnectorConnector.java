/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scimconnector.simple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Iterator;
import java.util.Set;

// com.scimconnector.simple.Scim2ConnectorConnector

@ConnectorClass(displayNameKey = "scim2connector.connector.display", configurationClass = Scim2ConnectorConfiguration.class)
public class Scim2ConnectorConnector implements Connector, TestOp, SchemaOp, CreateOp, DeleteOp {

    private static final Log LOG = Log.getLog(Scim2ConnectorConnector.class);

    private Scim2ConnectorConfiguration configuration;
    private Scim2ConnectorConnection connection;

    @Override
    public Scim2ConnectorConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (Scim2ConnectorConfiguration)configuration;
        this.connection = new Scim2ConnectorConnection(this.configuration);

        ScimRequests.init(this.configuration.getHostname());

        LOG.ok("Connector initialized");
    }

    @Override
    public void dispose() {
        configuration = null;
        if (connection != null) {
            connection.dispose();
            connection = null;
        }
    }

    @Override
    public void test() {
        LOG.ok("This is your hostname property: " + configuration.getHostname());
        try {
            String response = ScimRequests.getResponse();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error occurred while receiving schema from scim", e);
        }
        LOG.ok("Test finished");
    }

    @Override
    public Schema schema() {
        String response = ScimRequests.getSchema();

        // replace curly brackets because it's special symbol in this logger
        LOG.ok(response.
                replace("{", "<(").
                replace("}", ")>") +
                "\nGot schema from SCIM server");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode root = objectMapper.readTree(response);

            ObjectClassInfo classInfo = buildObjectClassInfo(root);

            SchemaBuilder schemaBuilder = new SchemaBuilder(Scim2ConnectorConnector.class);
            schemaBuilder.defineObjectClass(classInfo);
            return schemaBuilder.build();
        } catch (JsonProcessingException e) {
            LOG.ok("JSON parsing failed");
            throw new RuntimeException(e);
        }
    }

    private static ObjectClassInfo buildObjectClassInfo(JsonNode root) {
        ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();

        // TODO generalize
        JsonNode resourceInfo = root.get(0);

        // get resource type
        String name = resourceInfo.get("name").textValue();
        objectClassBuilder.setType(name);

        // add attributes
        assert resourceInfo.get("attributes").isArray();

        Iterator<JsonNode> it = resourceInfo.get("attributes").elements();
        while (it.hasNext()) {
            JsonNode attribute = it.next();
            assert attribute.isObject();

            /*TODO: now we add attribute into the class info only if it's "required"
             * there is only one such attribute in our schema - "userName"
             * so we need to add more
             */
            assert attribute.get("required").isBoolean();
            if (attribute.get("required").asBoolean()) {
                String attributeName = attribute.get("name").textValue();

                objectClassBuilder.addAttributeInfo(
                        AttributeInfoBuilder.build(attributeName, String.class)
                );
            }
        }

        return objectClassBuilder.build();
    }

    @Override
    public Uid create(final ObjectClass objectClass, final Set<Attribute> createAttributes, final OperationOptions options) {
        LOG.info("create::begin attributes {0}", createAttributes);
        String name = (String) createAttributes.toArray(new Attribute[0])[0].getValue().get(0);
        LOG.info("create::name: " + name);
        String response = ScimRequests.postCreateUser(name);

        LOG.ok(response.
                replace("{", "<(").
                replace("}", ")>") +
                "\nGot response from create account on SCIM server");

        ObjectMapper objectMapper = new ObjectMapper();
        String id;
        try {
            JsonNode root = objectMapper.readTree(response);
            id = root.get("id").textValue();
        } catch (JsonProcessingException e) {
            LOG.ok("JSON parsing failed");
            throw new RuntimeException(e);
        }
        LOG.info("create::response id: " + id);

        Uid uid = new Uid(id);

        LOG.info("create::end");
        return uid;
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
        LOG.info("delete::begin id: " + uid.getUidValue());

        int code = ScimRequests.deleteUser(uid.getUidValue());

        LOG.info("delete::response code: " + code);
        LOG.info("delete::end");
    }
}
