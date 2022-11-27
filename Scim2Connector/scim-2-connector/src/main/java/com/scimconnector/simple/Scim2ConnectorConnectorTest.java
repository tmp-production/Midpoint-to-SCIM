package com.scimconnector.simple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.*;


import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Scim2ConnectorConnectorTest {
    private final static String HOST_NAME = "http://0.0.0.0:8081";
    public static final Log LOG = Log.getLog(Scim2ConnectorConnectorTest.class);
    private static final ObjectClass objectClass = new ObjectClass("User");

    private static Scim2ConnectorConnector connector = null;
    private static Uid uid = null;

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
    @Order(0)
    void test1() {
        connector.test();
    }

    @org.junit.jupiter.api.Test
    @Order(1)
    void schema() {
        Schema schema = connector.schema();
        Assertions.assertNotNull(schema);
    }

    @org.junit.jupiter.api.Test
    @Order(2)
    void create() {
        Attribute a1 = AttributeBuilder.build("userName", "Unit Tester");
        Set<Attribute> set = new HashSet<Attribute>();
        set.add(a1);

        Uid uid = connector.create(objectClass, set, null);
        Assertions.assertNotNull(uid);

        LOG.info("Test create::uid={0}", uid.getUidValue());
        Scim2ConnectorConnectorTest.uid = uid;
    }

    @org.junit.jupiter.api.Test
    @Order(4)
    void delete() {
        Assertions.assertNotNull(connector);
        Assertions.assertNotNull(uid);
        connector.delete(objectClass, uid, null);
    }

    @org.junit.jupiter.api.Test
    @Order(3)
    void update() {
        Random r = new Random();
        Attribute a1 = AttributeBuilder.build("userName", "Unit Updater" + r.nextInt());
        Set<Attribute> set = new HashSet<Attribute>();
        set.add(a1);
        Uid res = connector.update(objectClass, uid, set, null);

        Assertions.assertEquals(uid, res);
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

    List<String> getMidpointUsernames() {
        WebClient webClient = WebClient.create("http://localhost:8080/");

        ObjectMapper objectMapper = new ObjectMapper();

        ArrayList<String> usernames = new ArrayList<>();
        String users = webClient.get()
                .uri("midpoint/ws/rest/users")
                .header("Authorization", "Basic YWRtaW5pc3RyYXRvcjo1ZWNyM3Q=")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println(users);

        JsonNode root = null;
        try {
            root = objectMapper.readTree(users);

            JsonNode userArray = root.get("object").get("object");
            assert userArray.isArray();
            Iterator<JsonNode> it = userArray.elements();
            while (it.hasNext()) {
                JsonNode user = it.next();
                assert user.isObject();
                usernames.add(user.get("name").asText());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return usernames;
    }

    /**
     * all necessary docker instances must be running to pass tests listed below
     */
    @org.junit.jupiter.api.Test
    void e2eCreateTest() {
        WebClient webClient = WebClient.create("http://localhost:8080/");

        String newUserName = UUID.randomUUID().toString();
        String newDescription = UUID.randomUUID().toString();
        String newFullName = UUID.randomUUID().toString();
        String newGivenName = UUID.randomUUID().toString();

        String body = String.format("<user>\n" +
                "    <name>%s</name>\n" +
                "    <description>%s</description>\n" +
                "    <fullName>%s</fullName>\n" +
                "    <givenName>%s</givenName>\n" +
                "    <familyName>AutomaticallyGenerated</familyName>\n" +
                "</user>\n", newUserName, newDescription, newFullName, newGivenName);

        String requestRes = webClient.post()
                .uri("midpoint/ws/rest/users")
                .header("Authorization", "Basic YWRtaW5pc3RyYXRvcjo1ZWNyM3Q=")
                .header("Content-Type", "application/xml")
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //TODO wait a little here,
        // then check validity through SCIM-REST (GET /users), parse list to see that
        // our new user propagated to SCIM successfully

        System.out.println(requestRes);
    }
}