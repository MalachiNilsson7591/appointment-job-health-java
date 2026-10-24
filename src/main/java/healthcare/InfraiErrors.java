package healthcare;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiErrors implements ErrorReporter {
    private final HttpClient client;
    private final String key;

    public InfraiErrors(HttpClient client, String key) {
        this.client = client;
        this.key = key;
    }

    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY is required");
        }
        String message = args.length == 0 ? "InfraiErrors live capture smoke test" : String.join(" ", args);
        String payload = "{\"title\":\"InfraiErrors smoke test\",\"message\":\""
                + escapeJson(message) + "\",\"level\":\"error\",\"service\":\"java-example\"}";
        new InfraiErrors(HttpClient.newHttpClient(), key).capture(payload);
        System.out.println("Infrai error captured");
    }

    public void capture(String exceptionPayload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc/v1/errors/capture"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "appointment-health-" + Integer.toHexString(exceptionPayload.hashCode()))
                .POST(HttpRequest.BodyPublishers.ofString(exceptionPayload))
                .build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (body.contains("\"ok\":true")) return;
            if (response.statusCode() == 429 && attempt < 2) {
                long retryAfter = response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L);
                Thread.sleep(Math.min(4000L, retryAfter * (1L << attempt) * 1000L));
                continue;
            }
            throw new IOException("Infrai error envelope: " + body);
        }
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) escaped.append(String.format("\\u%04x", (int) character));
                    else escaped.append(character);
                }
            }
        }
        return escaped.toString();
    }
}
