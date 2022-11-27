package com.scimconnector.simple;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

class Scim2ConnectorConnectorTest {
    private final static String HOST_NAME = "http://0.0.0.0:8081";


    private static Scim2ConnectorConnector connector = null;

    @org.junit.jupiter.api.Test
    @BeforeAll
    static void init() {
        Scim2ConnectorConnector connector = new Scim2ConnectorConnector();
        Scim2ConnectorConfiguration configuration = new Scim2ConnectorConfiguration();
        configuration.setHostname(HOST_NAME);
        connector.init(configuration);

        Assertions.assertEquals(configuration, connector.getConfiguration());
        connector.getConfiguration().validate();
        Assertions.assertEquals(HOST_NAME, connector.getConfiguration().getHostname());

        Assertions.assertNotNull(connector.getConnection());

        Scim2ConnectorConnectorTest.connector = connector;
    }

    @org.junit.jupiter.api.Test
    void test1() {
        connector.test();
    }

    @org.junit.jupiter.api.Test
    void schema() {
    }

    @org.junit.jupiter.api.Test
    void create() {
    }

    @org.junit.jupiter.api.Test
    void delete() {
    }

    @org.junit.jupiter.api.Test
    void update() {
    }

    @org.junit.jupiter.api.Test
    void sync() {
    }

    @org.junit.jupiter.api.Test
    void getLatestSyncToken() {
    }

    @org.junit.jupiter.api.Test
    @AfterAll
    static void dispose() {
        Assertions.assertNotNull(connector);

        connector.dispose();

        Assertions.assertNull(connector.getConnection());
        Assertions.assertNull(connector.getConnection());
    }
}