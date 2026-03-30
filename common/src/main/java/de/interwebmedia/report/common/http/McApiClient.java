package de.interwebmedia.report.common.http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.interwebmedia.report.api.model.PlayerEdition;
import de.interwebmedia.report.api.service.UuidLookupService;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async UUID resolver against mc-api.io with in-memory cache.
 */
public class McApiClient implements UuidLookupService {

    private final HttpClient httpClient;
    private final Cache<String, UUID> cache;

    public McApiClient(Duration ttl) {
        this.httpClient = HttpClient.newHttpClient();
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(ttl).build();
    }

    @Override
    public CompletableFuture<UUID> lookup(String username, PlayerEdition edition) {
        String cacheKey = edition + ":" + username.toLowerCase();
        UUID cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String endpoint = switch (edition) {
            case JAVA -> "https://mc-api.io/v3/profile/java/" + encoded;
            case BEDROCK -> "https://mc-api.io/v3/profile/bedrock/" + encoded;
        };

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::extractUuid)
                .thenApply(uuid -> {
                    cache.put(cacheKey, uuid);
                    return uuid;
                });
    }

    private UUID extractUuid(String response) {
        int keyStart = response.indexOf("\"id\":\"");
        if (keyStart < 0) {
            throw new IllegalArgumentException("UUID not found in response");
        }

        int valueStart = keyStart + 6;
        int valueEnd = response.indexOf('"', valueStart);
        String raw = response.substring(valueStart, valueEnd).replace("-", "");
        if (raw.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID format from response");
        }
        return UUID.fromString(raw.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        ));
    }
}
