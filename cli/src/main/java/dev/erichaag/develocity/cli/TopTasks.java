package dev.erichaag.develocity.cli;

import dev.erichaag.develocity.processor.BuildConsumer;
import dev.erichaag.develocity.processor.BuildProcessor;
import dev.erichaag.develocity.model.Build;
import dev.erichaag.develocity.model.GradleAttributes;
import dev.erichaag.develocity.model.MavenAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Comparator.comparingInt;

public class TopTasks {

    private static final String serverUrl = "https://ge.solutions-team.gradle.com";
    private static final String accessKey = new AccessKeyProvider().getAccessKey(serverUrl).orElse(null);

    public static void main(String[] args) {
        final var consumer = new TopTasksBuildConsumer();
        BuildProcessor
                .create(serverUrl, accessKey)
                .withQuery("user:ehaag")
                .withMaxBuilds(10000)
                .withConsumer(consumer)
                .process();

        consumer.taskCountByTask
                .entrySet()
                .stream()
                .sorted(comparingInt(Entry::getValue))
                .forEach(taskCount -> System.out.println(taskCount.getKey() + " " + taskCount.getValue()));

        System.out.println(consumer.taskCountByTask.values().stream().mapToInt(i -> i).sum());
    }

    public static final class TopTasksBuildConsumer implements BuildConsumer {

        final Map<String, Integer> taskCountByTask = new HashMap<>();

        @Override
        public void onGradleBuild(Build build, GradleAttributes attributes) {
            taskCountByTask.compute(String.join(" ", attributes.getRequestedTasks()), (k, v) -> v == null ? 1 : v + 1);
        }

        @Override
        public void onMavenBuild(Build build, MavenAttributes attributes) {
            taskCountByTask.compute(String.join(" ", attributes.getRequestedGoals()), (k, v) -> v == null ? 1 : v + 1);
        }
    }
}
