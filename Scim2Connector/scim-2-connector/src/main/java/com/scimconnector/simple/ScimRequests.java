package com.scimconnector.simple;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

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

    public static String postCreate(String name) {
        String body = "{\"userName\":\"" + name + "\"}";
        return webClient.post()
                .uri("/scim/v2/Users")
                .header("Content-Type", "application/scim+json")
                .header("Authorization", "Basic c2NpbS11c2VyOmNoYW5nZWl0")
                .body(BodyInserters.fromObject(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
