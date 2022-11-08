package com.scimconnector.simple;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

}
