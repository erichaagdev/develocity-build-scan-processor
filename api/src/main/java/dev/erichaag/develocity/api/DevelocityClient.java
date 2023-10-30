package dev.erichaag.develocity.api;

import java.util.List;

public interface DevelocityClient {

    List<Build> getBuilds(String query, int maxBuilds, String fromBuild);

    GradleAttributes getGradleAttributes(String id);

    GradleBuildCachePerformance getGradleBuildCachePerformance(String id);

    MavenAttributes getMavenAttributes(String id);

    MavenBuildCachePerformance getMavenBuildCachePerformance(String id);
}
