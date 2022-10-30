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
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.TestOp;

import java.util.Iterator;

// com.scimconnector.simple.Scim2ConnectorConnector

@ConnectorClass(displayNameKey = "scim2connector.connector.display", configurationClass = Scim2ConnectorConfiguration.class)
public class Scim2ConnectorConnector implements Connector, TestOp, SchemaOp {

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
            schema();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while receiving schema from scim", e);
        }
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
            assert root.isObject(); // I assume that we got only one schema

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
}
