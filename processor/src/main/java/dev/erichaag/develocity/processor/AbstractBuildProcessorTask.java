package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.api.GradleAttributes;
import dev.erichaag.develocity.api.GradleBuildCachePerformance;
import dev.erichaag.develocity.api.MavenAttributes;
import dev.erichaag.develocity.api.MavenBuildCachePerformance;

import java.time.Duration;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.Math.min;
import static java.lang.Thread.ofVirtual;

public abstract class AbstractBuildProcessorTask {

    private final DevelocityClient client;
    private final String query;

    private int buildsRemaining;
    private final Deque<Thread> threads = new LinkedList<>();
    private final Semaphore semaphore = new Semaphore(25);
    private String fromBuild = null;

    private static final int maxRetries = 3;
    private static final int maxBuildsPerRequest = 1000;

    AbstractBuildProcessorTask(DevelocityClient client, String query, int buildsRemaining) {
        this.client = client;
        this.query = query;
        this.buildsRemaining = buildsRemaining;
    }

    void process() {
        while (buildsRemaining > 0) {
            final var maxBuildsForQuery = min(buildsRemaining, maxBuildsPerRequest);
            final var builds = client.getBuilds(query, maxBuildsForQuery, fromBuild);
            builds.forEach(build -> threads.add(ofVirtual().start(() -> processBuild(build))));
            buildsRemaining = builds.size() < maxBuildsForQuery ? 0 : buildsRemaining - maxBuildsForQuery;
            if (buildsRemaining > 0) fromBuild = builds.getLast().getId();
        }

        while (!threads.isEmpty()) {
            join(threads.pop());
        }
    }

    protected abstract void processBuild(Build build);

    protected GradleAttributes fetchGradleAttributes(String buildId) {
        return withRetry(() -> withSemaphore(() -> client.getGradleAttributes(buildId)));
    }

    protected GradleBuildCachePerformance fetchGradlePerformance(String buildId) {
        return withRetry(() -> withSemaphore(() -> client.getGradleBuildCachePerformance(buildId)));
    }

    protected MavenAttributes fetchMavenAttributes(String buildId) {
        return withRetry(() -> withSemaphore(() -> client.getMavenAttributes(buildId)));
    }

    protected MavenBuildCachePerformance fetchMavenPerformance(String buildId) {
        return withRetry(() -> withSemaphore(() -> client.getMavenBuildCachePerformance(buildId)));
    }

    private <T> T withSemaphore(Supplier<T> supplier) {
        try {
            semaphore.acquire();
            return supplier.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    private static <T> T withRetry(Supplier<T> supplier) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return supplier.get();
            } catch (Exception ignored) {
                if (i < maxRetries - 1) {
                    sleep(Duration.ofSeconds(1));
                }
            }
        }
        throw new RuntimeException("Failed to call API " + maxRetries + " times.");
    }

    private static void sleep(Duration duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
