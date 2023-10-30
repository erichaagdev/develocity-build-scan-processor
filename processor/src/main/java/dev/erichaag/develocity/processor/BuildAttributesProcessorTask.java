package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.DevelocityClient;

final class BuildAttributesProcessorTask extends AbstractBuildProcessorTask {

    private final BuildAttributesConsumer buildConsumer;

    BuildAttributesProcessorTask(DevelocityClient client, String query, int buildsRemaining, BuildAttributesConsumer buildConsumer) {
        super(client, query, buildsRemaining);
        this.buildConsumer = buildConsumer;
    }

    protected void processBuild(Build build) {
        if (build.getBuildToolType().equals("gradle")) {
            final var attributes = fetchGradleAttributes(build.getId());
            buildConsumer.onGradleBuild(build, attributes);
        } else if (build.getBuildToolType().equals("maven")) {
            final var attributes = fetchMavenAttributes(build.getId());
            buildConsumer.onMavenBuild(build, attributes);
        }
    }
}
