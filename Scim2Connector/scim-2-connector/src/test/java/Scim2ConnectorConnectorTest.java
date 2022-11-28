import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scimconnector.simple.Scim2ConnectorConfiguration;
import com.scimconnector.simple.Scim2ConnectorConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @Test
    @Order(0)
    void test1() {
        connector.test();
    }

    @Test
    @Order(1)
    void schema() {
        Schema schema = connector.schema();
        Assertions.assertNotNull(schema);
    }

    @Test
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

    @Test
    @Order(4)
    void delete() {
        Assertions.assertNotNull(connector);
        Assertions.assertNotNull(uid);
        connector.delete(objectClass, uid, null);
    }

    @Test
    @Order(3)
    void update() {
        Random r = new Random();
        Attribute a1 = AttributeBuilder.build("userName", "Unit Updater" + r.nextInt());
        Set<Attribute> set = new HashSet<Attribute>();
        set.add(a1);
        Uid res = connector.update(objectClass, uid, set, null);

        Assertions.assertEquals(uid, res);
    }

    @Test
    void sync() {
    }

    @Test
    void getLatestSyncToken() {
    }

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

    // defined in resource schema
    private static final String resourceOid = "7331ce47-d667-4fa3-81d5-c6974c37d29c";

    private static final String userOid = "ab0ba777-b1ba-b0ba-bebe-777111111111";

    /**
     * all necessary docker instances must be running to pass tests listed below
     */
    @Test
    void e2eCreateTest() {
        WebClient midpointClient = WebClient.create("http://localhost:8080/");
        WebClient scimClient = WebClient.create("http://0.0.0.0:8081/");

        String newUserName = UUID.randomUUID().toString();
        String newDescription = UUID.randomUUID().toString();
        String newFullName = UUID.randomUUID().toString();
        String newGivenName = UUID.randomUUID().toString();

        /*
         * Add new user into Midpoint
         */

        String body = String.format("<user oid=\"%s\">\n" +
                "    <name>%s</name>\n" +
                "    <description>%s</description>\n" +
                "    <fullName>%s</fullName>\n" +
                "    <givenName>%s</givenName>\n" +
                "    <familyName>AutomaticallyGenerated</familyName>\n" +
                "</user>\n", userOid, newUserName, newDescription, newFullName, newGivenName);

        String requestRes = midpointClient.post()
                .uri("midpoint/ws/rest/users")
                .header("Authorization", "Basic YWRtaW5pc3RyYXRvcjo1ZWNyM3Q=")
                .header("Content-Type", "application/xml")
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Answer: " + requestRes);

        /*
         * assign account to created user
         */

        String testFileContent = null;
        try {
            testFileContent = readTestFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        body = String.format(testFileContent, resourceOid);

        requestRes = midpointClient.post()
                .uri("midpoint/ws/rest/users/" + userOid)
                .header("Authorization", "Basic YWRtaW5pc3RyYXRvcjo1ZWNyM3Q=")
                .header("Content-Type", "application/xml")
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        /*
         * wait a moment
         */

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        /*
         * get userName from SCIM server
         */

        String responseFromScim = scimClient.get().
                uri("scim/v2/Users/").
                header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0").
                retrieve().
                bodyToMono(String.class).
                block();

        ObjectMapper objectMapper = new ObjectMapper();
        String userNameFromScim = null;
        try {
            JsonNode root = objectMapper.readTree(responseFromScim);
            Assertions.assertNotNull(root);

            JsonNode user = root.get("Resources").get(0);
            Assertions.assertNotNull(user);

            JsonNode userName = user.get("userName");
            Assertions.assertNotNull(userName);

            userNameFromScim = userName.asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertNotNull(userNameFromScim);
        System.out.println("Got username: " + userNameFromScim);

        /*
         * and actually compare strings
         */

        // hallelujah !!!!
        Assertions.assertEquals(newFullName, userNameFromScim);

        System.out.println(responseFromScim);
    }

    static String readTestFile() throws IOException
    {
        byte[] encoded = Files.readAllBytes(
                Paths.get("src/test/resources/add-account.xml"));
        return new String(encoded, Charset.defaultCharset());
    }
}