package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.DevelocityClient;

final class BuildPerformanceProcessorTask extends AbstractBuildProcessorTask {

    private final BuildPerformanceConsumer buildConsumer;

    BuildPerformanceProcessorTask(DevelocityClient client, String query, int buildsRemaining, BuildPerformanceConsumer buildConsumer) {
        super(client, query, buildsRemaining);
        this.buildConsumer = buildConsumer;
    }

    protected void processBuild(Build build) {
        if (build.getBuildToolType().equals("gradle")) {
            final var performance = fetchGradlePerformance(build.getId());
            buildConsumer.onGradleBuild(build, performance);
        } else if (build.getBuildToolType().equals("maven")) {
            final var performance = fetchMavenPerformance(build.getId());
            buildConsumer.onMavenBuild(build, performance);
        }
    }
}
