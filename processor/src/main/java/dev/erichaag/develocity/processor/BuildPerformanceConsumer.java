package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.GradleBuildCachePerformance;
import dev.erichaag.develocity.api.MavenBuildCachePerformance;

public interface BuildPerformanceConsumer extends BuildConsumer {

    void onGradleBuild(Build build, GradleBuildCachePerformance performance);

    void onMavenBuild(Build build, MavenBuildCachePerformance performance);
}
