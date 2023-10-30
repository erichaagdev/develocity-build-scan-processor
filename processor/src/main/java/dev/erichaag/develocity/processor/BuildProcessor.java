package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.DevelocityClient;

public final class BuildProcessor {

    private final DevelocityClient client;

    public BuildProcessor(DevelocityClient client) {
        this.client = client;
    }

    public void process(String query, int maxBuilds, BuildConsumer buildConsumer) {
        switch (buildConsumer) {
            case BuildAttributesAndPerformanceConsumer c -> new BuildAttributesAndPerformanceProcessorTask(client, query, maxBuilds, c).process();
            case BuildAttributesConsumer c -> new BuildAttributesProcessorTask(client, query, maxBuilds, c).process();
            case BuildPerformanceConsumer c -> new BuildPerformanceProcessorTask(client, query, maxBuilds, c).process();
            default -> throw new RuntimeException("'BuildConsumer' must be one of: BuildAttributesAndPerformanceConsumer, BuildAttributesConsumer, or BuildPerformanceConsumer. Was " + buildConsumer.getClass());
        }
    }
}
