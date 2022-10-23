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

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.springframework.web.reactive.function.client.WebClient;

@ConnectorClass(displayNameKey = "scim2connector.connector.display", configurationClass = Scim2ConnectorConfiguration.class)
public class Scim2ConnectorConnector implements Connector, TestOp, SchemaOp {

    private static final Log LOG = Log.getLog(Scim2ConnectorConnector.class);

    private Scim2ConnectorConfiguration configuration;
    private Scim2ConnectorConnection connection;

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (Scim2ConnectorConfiguration)configuration;
        this.connection = new Scim2ConnectorConnection(this.configuration);
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
        LOG.error("BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA BEBRA");
        LOG.info("This is your sample property: " + configuration.getSampleProperty());
    }

    @Override
    public Schema schema() {
        WebClient client = WebClient.create("http://localhost:8080");
        String response = client.get()
                .uri("http://scim:8080/scim/v2/Schemas")
                .header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOG.info(response + "THIS IS MY STRING INDICATING SOME BEBRA");

        ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
        objectClassBuilder.setType("myAccount");
        objectClassBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("fullName", String.class));
        objectClassBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("homeDir", String.class));

        SchemaBuilder schemaBuilder = new SchemaBuilder(Scim2ConnectorConnector.class);
        schemaBuilder.defineObjectClass(objectClassBuilder.build());
        return schemaBuilder.build();
    }
}
