package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.GradleAttributes;
import dev.erichaag.develocity.api.GradleBuildCachePerformance;
import dev.erichaag.develocity.api.MavenAttributes;
import dev.erichaag.develocity.api.MavenBuildCachePerformance;

public interface BuildAttributesAndPerformanceConsumer extends BuildConsumer {

    void onGradleBuild(Build build, GradleAttributes attributes, GradleBuildCachePerformance performance);

    void onMavenBuild(Build build, MavenAttributes attributes, MavenBuildCachePerformance performance);
}
