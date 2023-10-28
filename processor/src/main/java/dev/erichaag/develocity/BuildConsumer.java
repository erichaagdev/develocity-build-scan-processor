package dev.erichaag.develocity;

import dev.erichaag.develocity.model.Build;
import dev.erichaag.develocity.model.GradleAttributes;
import dev.erichaag.develocity.model.MavenAttributes;

public interface BuildConsumer {

    void onGradleBuild(Build build, GradleAttributes attributes);

    void onMavenBuild(Build build, MavenAttributes attributes);
}
