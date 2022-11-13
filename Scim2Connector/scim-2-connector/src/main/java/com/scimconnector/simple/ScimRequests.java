package com.scimconnector.simple;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Map;

public class ScimRequests {

    private static WebClient webClient;

    public static void init(String hostname) {
        webClient = WebClient.create(hostname);
    }

    public static String getSchema() {
        return webClient.get()
                .uri("/scim/v2/Schemas")
                .header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public static String getResponse() {
        return webClient.get()
                .uri("/")
                .header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public static String postCreateUser(String name) {
        String body = "{\"userName\":\"" + name + "\"}";
        return webClient.post()
                .uri("/scim/v2/Users")
                .header("Content-Type", "application/scim+json")
                .header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0")
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public static int deleteUser(String uid) {
        return webClient.delete()
                .uri("/scim/v2/Users/" + uid)
                .header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0")
                .exchangeToMono(response -> {
                    return Mono.just(response.statusCode().value());
                })
                .block();
    }

    // https://www.rfc-editor.org/rfc/rfc7644#section-3.5.2.3 - at the end
    public static String updateUser(String uid, Map<String, String> attrsToUpdate) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n" +
                "     \"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],\n" +
                "     \"Operations\": [{\n" +
                "       \"op\":\"replace\",\n" +
                "       \"value\":{\n");

        ArrayList<String> values = new ArrayList<>();

        for(Map.Entry<String, String> entry : attrsToUpdate.entrySet()) {
            String newValue = "             \"" + entry.getKey() + "\" : \"" + entry.getValue() + "\"";
            values.add(newValue);
        }

        int n = values.size();
        for (int i = 0; i < n; i++) {
            stringBuilder.append(values.get(i));
            if (i != n - 1) {
                stringBuilder.append(',');
            }
            stringBuilder.append('\n');
        }
        stringBuilder.append("          }\n");
        stringBuilder.append("      }]\n" +
                "}\n");

        String body = stringBuilder.toString();
        Scim2ConnectorConnector.LOG.info("updateUser:: request body:\n" +
                body.replace("{", "<(").replace("}", ")>"));

        return webClient.patch()
                .uri("/scim/v2/Users/" + uid)
                .header("Content-Type", "application/scim+json")
                .header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0")
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
