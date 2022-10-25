package com.scimconnector.simple;

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

}
