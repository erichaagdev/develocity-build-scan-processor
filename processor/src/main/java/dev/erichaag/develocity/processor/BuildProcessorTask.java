package dev.erichaag.develocity.processor;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.DevelocityApiClient;
import dev.erichaag.develocity.api.GradleAttributes;
import dev.erichaag.develocity.api.MavenAttributes;

import java.time.Duration;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static java.lang.Math.min;
import static java.lang.Thread.ofVirtual;

final class BuildProcessorTask {

    private final DevelocityApiClient client;
    private final String query;
    private final BuildConsumer buildConsumer;

    private int buildsRemaining;
    private final Deque<Thread> threads = new LinkedList<>();
    private final Semaphore semaphore = new Semaphore(25);
    private final Lock lock = new ReentrantLock();
    private String fromBuild = null;

    private static final int maxRetries = 3;
    private static final int maxBuildsPerRequest = 1000;

    BuildProcessorTask(DevelocityApiClient client, String query, BuildConsumer buildConsumer, int buildsRemaining) {
        this.client = client;
        this.query = query;
        this.buildConsumer = buildConsumer;
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

    private void processBuild(Build build) {
        if (build.getBuildToolType().equals("gradle")) {
            final var attributes = withSemaphore(() -> fetchGradleBuild(build.getId()));
            withLock(() -> buildConsumer.onGradleBuild(build, attributes));
        } else if (build.getBuildToolType().equals("maven")) {
            final var attributes = withSemaphore(() -> fetchMavenBuild(build.getId()));
            withLock(() -> buildConsumer.onMavenBuild(build, attributes));
        }
    }

    private GradleAttributes fetchGradleBuild(String buildId) {
        return withRetry(() -> client.getGradleAttributes(buildId));
    }

    private MavenAttributes fetchMavenBuild(String buildId) {
        return withRetry(() -> client.getMavenAttributes(buildId));
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

    private void withLock(Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
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