package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.DevelocityClient;

final class BuildAttributesAndPerformanceProcessorTask extends AbstractBuildProcessorTask {

    private final BuildAttributesAndPerformanceConsumer buildConsumer;

    BuildAttributesAndPerformanceProcessorTask(DevelocityClient client, String query, int buildsRemaining, BuildAttributesAndPerformanceConsumer buildConsumer) {
        super(client, query, buildsRemaining);
        this.buildConsumer = buildConsumer;
    }

    protected void processBuild(Build build) {
        if (build.getBuildToolType().equals("gradle")) {
            final var attributes = fetchGradleAttributes(build.getId());
            final var performance = fetchGradlePerformance(build.getId());
            buildConsumer.onGradleBuild(build, attributes, performance);
        } else if (build.getBuildToolType().equals("maven")) {
            final var attributes = fetchMavenAttributes(build.getId());
            final var performance = fetchMavenPerformance(build.getId());
            buildConsumer.onMavenBuild(build, attributes, performance);
        }
    }
}
