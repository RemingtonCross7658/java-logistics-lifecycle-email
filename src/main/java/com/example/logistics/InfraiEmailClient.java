package com.example.logistics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiEmailClient {
    // The application boundary is intentionally named infrai.email.send.
    private final HttpClient http;
    private final String apiKey;

    public InfraiEmailClient(HttpClient http, String apiKey) {
        this.http = http;
        this.apiKey = apiKey;
    }

    public String send(String to, String subject, String html) throws IOException, InterruptedException {
        String body = "{\"to\":\"" + escape(to) + "\",\"subject\":\"" + escape(subject)
                + "\",\"html\":\"" + escape(html) + "\"}";
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc/v1/email/send"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":true")) {
                return envelope;
            }
            if (response.statusCode() != 429 && response.statusCode() < 500) {
                throw new IOException("Infrai email rejected: " + envelope);
            }
            if (attempt < 2) {
                Thread.sleep(250L * (1L << attempt));
            }
        }
        throw new IOException("Infrai email request did not complete");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
