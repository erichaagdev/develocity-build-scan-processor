package dev.erichaag.develocity.cli;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.GradleAttributes;
import dev.erichaag.develocity.api.MavenAttributes;
import dev.erichaag.develocity.processor.BuildAttributesConsumer;
import dev.erichaag.develocity.processor.BuildConsumer;
import picocli.CommandLine.Command;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Command(name = "topRequestedTasks", mixinStandardHelpOptions = true)
final class TopRequestedTasksReport extends AbstractReport implements BuildAttributesConsumer {

    private final Map<Key, Integer> topRequestedTasks = new ConcurrentHashMap<>();

    @Override
    public void onGradleBuild(Build build, GradleAttributes attributes) {
        addRequestedTask(attributes.getRootProjectName(), build.getBuildToolType(), String.join(" ", attributes.getRequestedTasks()));
    }

    @Override
    public void onMavenBuild(Build build, MavenAttributes attributes) {
        addRequestedTask(attributes.getTopLevelProjectName(), build.getBuildToolType(), String.join(" ", attributes.getRequestedGoals()));
    }

    private void addRequestedTask(String projectName, String buildTool, String requestedTasks) {
        topRequestedTasks.compute(new Key(projectName, buildTool, requestedTasks), (k, v) -> v == null ? 1 : v + 1);
    }

    @Override
    protected String getReport() {
        final var buildsProcessed = topRequestedTasks.values().stream().mapToInt(i -> i).sum();
        final var table = Table.withHeader("Project", "Build tool", "Requested tasks", "Count", "%");
        topRequestedTasks.entrySet()
                .stream()
                .sorted(Comparator.<Map.Entry<Key, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .forEach(entry -> table.row(
                        entry.getKey().project(),
                        entry.getKey().buildTool(),
                        entry.getKey().requestedTasks(),
                        entry.getValue().toString(),
                        String.valueOf(entry.getValue() / ((double) buildsProcessed))));
        return table + "\n\nBuilds processed: " + buildsProcessed;
    }

    private record Key(String project, String buildTool, String requestedTasks) {
    }

    @Override
    protected BuildConsumer getBuildConsumer() {
        return this;
    }
}
