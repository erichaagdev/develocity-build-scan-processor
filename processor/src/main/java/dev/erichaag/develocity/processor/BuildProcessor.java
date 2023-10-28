package dev.erichaag.develocity.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.erichaag.develocity.api.DevelocityApiClient;

import java.net.http.HttpClient;

public final class BuildProcessor {

    private final DevelocityApiClient client;
    private final String query;
    private final int maxBuilds;
    private final BuildConsumer buildConsumer;

    public BuildProcessor(DevelocityApiClient client, String query, int maxBuilds, BuildConsumer buildConsumer) {
        this.client = client;
        this.query = query;
        this.maxBuilds = maxBuilds;
        this.buildConsumer = buildConsumer;
    }

    public static BuildProcessorWithClient create(String serverUrl, String apiKey) {
        final var httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        final var objectMapper = new ObjectMapper().findAndRegisterModules();
        final var client = new DevelocityApiClient(serverUrl, apiKey, httpClient, objectMapper);
        return new BuildProcessorWithClient(client);
    }

    public void process() {
        new BuildProcessorTask(client, query, buildConsumer, maxBuilds).process();
    }

    public record BuildProcessorWithClient(DevelocityApiClient client) {
        public BuildProcessorWithQuery withQuery(String query) {
            return new BuildProcessorWithQuery(client, query);
        }
    }

    public record BuildProcessorWithQuery(DevelocityApiClient client, String query) {
        public BuildProcessorWithMaxBuilds withMaxBuilds(int maxBuilds) {
            return new BuildProcessorWithMaxBuilds(client, query, maxBuilds);
        }
    }

    public record BuildProcessorWithMaxBuilds(DevelocityApiClient client, String query, int maxBuilds) {
        public BuildProcessor withConsumer(BuildConsumer buildConsumer) {
            return new BuildProcessor(client, query, maxBuilds, buildConsumer);
        }
    }
}
