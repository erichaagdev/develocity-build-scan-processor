package dev.erichaag.develocity.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.erichaag.develocity.model.Build;
import dev.erichaag.develocity.model.GradleAttributes;
import dev.erichaag.develocity.model.MavenAttributes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;

public class DevelocityApiClient {

    private final URI serverUrl;
    private final String accessKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DevelocityApiClient(String serverUrl, String accessKey, HttpClient httpClient, ObjectMapper objectMapper) {
        this.serverUrl = URI.create(serverUrl);
        this.accessKey = accessKey;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public List<Build> getBuilds(String query, int maxBuilds, String fromBuild) {
        final var queryParams = new HashMap<String, String>();
        queryParams.put("reverse", "true");
        queryParams.put("query", query);
        queryParams.put("maxBuilds", String.valueOf(maxBuilds));
        if (fromBuild != null) queryParams.put("fromBuild", fromBuild);
        final var response = sendRequest("/api/builds", queryParams);
        return handleResponse(response, new TypeReference<>() {
        });
    }

    public GradleAttributes getGradleAttributes(String id) {
        final var response = sendRequest("/api/builds/" + id + "/gradle-attributes");
        return handleResponse(response, new TypeReference<>() {
        });
    }

    public MavenAttributes getMavenAttributes(String id) {
        final var response = sendRequest("/api/builds/" + id + "/maven-attributes");
        return handleResponse(response, new TypeReference<>() {
        });
    }

    private HttpResponse<byte[]> sendRequest(String path) {
        return sendRequest(path, emptyMap());
    }

    private HttpResponse<byte[]> sendRequest(String path, Map<String, String> queryParams) {
        final var request = buildRequest(path, queryParams);
        try {
            return httpClient.send(request, ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest buildRequest(String path, Map<String, String> queryParams) {
        final var request = HttpRequest.newBuilder().uri(buildRequestUri(path, queryParams));
        if (accessKey != null) request.header("Authorization", "Bearer " + accessKey);
        return request.build();
    }

    private URI buildRequestUri(String path, Map<String, String> queryParams) {
        final var queryString = queryParams.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(joining("&"));
        try {
            return new URI(serverUrl.getScheme(), null, serverUrl.getHost(), serverUrl.getPort(), path, queryString, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T handleResponse(HttpResponse<byte[]> response, TypeReference<T> typeReference) {
        if (response.statusCode() == 200) {
            return readValue(response.body(), typeReference);
        }
        throw new RuntimeException("Received response code " + response.statusCode() + " from " + response.request().uri());
    }

    private <T> T readValue(byte[] value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
