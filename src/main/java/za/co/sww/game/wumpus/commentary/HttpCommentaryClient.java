package za.co.sww.game.wumpus.commentary;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public class HttpCommentaryClient implements CommentaryClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI endpoint;
    private final Duration timeout;

    public HttpCommentaryClient(HttpClient httpClient, ObjectMapper objectMapper, URI endpoint, Duration timeout) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.endpoint = endpoint;
        this.timeout = timeout;
    }

    @Override
    public Optional<String> fetchCommentary(CommentarySnapshot snapshot) {
        try {
            String requestJson = objectMapper.writeValueAsString(snapshot);
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }
            String responseBody = new String(response.body(), StandardCharsets.UTF_8);
            CommentaryPayload payload = objectMapper.readValue(responseBody, CommentaryPayload.class);
            if (payload.commentary() == null || payload.commentary().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(payload.commentary().trim());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private record CommentaryPayload(String commentary, boolean fallback) {
    }
}
