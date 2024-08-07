package dev.erichaag.develocity.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;
import static java.util.Optional.empty;

public final class HttpClientDevelocityClient implements DevelocityClient {

    private static final ObjectMapper objectMapper = new JsonMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final URI serverUrl;
    private final String accessKey;
    private final HttpClient httpClient;

    HttpClientDevelocityClient(URI serverUrl, String accessKey, HttpClient httpClient) {
        this.serverUrl = serverUrl;
        this.accessKey = accessKey;
        this.httpClient = httpClient;
    }

    @Override
    public Optional<Build> getBuild(String id, Set<BuildModel> buildModels) {
        final var response = sendRequest("/api/builds/" + id, null, false, null, null, buildModels);
        if (response.statusCode() == 404) {
            return empty();
        }
        return Optional.of(Build.from(handleResponse(response, new TypeReference<>() {})));
    }

    @Override
    public List<Build> getBuilds(String query, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels) {
        final var response = sendRequest("/api/builds", query, true, maxBuilds, fromBuild, buildModels);
        return handleResponse(response, new TypeReference<List<ApiBuild>>() {}).stream().map(Build::from).toList();
    }

    private HttpResponse<byte[]> sendRequest(String path, String query, Boolean reverse, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels) {
        final var request = buildRequest(path, query, reverse, maxBuilds, fromBuild, buildModels);
        return sendRequest(request, ofByteArray());
    }

    private HttpResponse<byte[]> sendRequest(HttpRequest request, BodyHandler<byte[]> bodyHandler) {
        try {
            return httpClient.send(request, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest buildRequest(String path, String query, Boolean reverse, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels) {
        final var request = HttpRequest.newBuilder().uri(buildRequestUri(path, query, reverse, maxBuilds, fromBuild, buildModels));
        if (accessKey != null) request.header("Authorization", "Bearer " + accessKey);
        return request.build();
    }

    private URI buildRequestUri(String path, String query, Boolean reverse, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels) {
        try {
            final var parameters = new HashSet<String>();
            if (maxBuilds != null) parameters.add("maxBuilds=" + maxBuilds);
            if (reverse != null) parameters.add("reverse=" + reverse);
            if (fromBuild != null && !fromBuild.isEmpty()) parameters.add("fromBuild=" + fromBuild);
            if (query != null && !query.isEmpty()) parameters.add("query=" + query);
            if (buildModels != null) {
                if (buildModels.contains(BuildModel.ALL_MODELS)) {
                    parameters.add("allModels=" + true);
                } else {
                    buildModels.forEach(it -> parameters.add("models=" + it.modelName()));
                }
            }
            return new URI(serverUrl.getScheme(), null, serverUrl.getHost(), serverUrl.getPort(), path, String.join("&", parameters), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T handleResponse(HttpResponse<byte[]> response, TypeReference<T> typeReference) {
        if (response.statusCode() == 200) {
            return readValue(response.body(), typeReference);
        }
        throw new DevelocityClientException(response.request().uri(), response.statusCode(), response.headers().map());
    }

    private <T> T readValue(byte[] value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
