package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.GradleAttributes;
import dev.erichaag.develocity.api.MavenAttributes;

public interface BuildAttributesConsumer extends BuildConsumer {

    void onGradleBuild(Build build, GradleAttributes attributes);

    void onMavenBuild(Build build, MavenAttributes attributes);
}
