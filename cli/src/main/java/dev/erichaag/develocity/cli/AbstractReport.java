package dev.erichaag.develocity.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.erichaag.develocity.api.AccessKeyProvider;
import dev.erichaag.develocity.api.HttpClientDevelocityClient;
import dev.erichaag.develocity.processor.BuildConsumer;
import dev.erichaag.develocity.processor.BuildProcessor;
import picocli.CommandLine.Option;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Properties;

abstract class AbstractReport implements Runnable {

    @SuppressWarnings("unused")
    @Option(names = {"--query", "-q"}, defaultValue = "")
    private String query;

    @SuppressWarnings("unused")
    @Option(names = {"--count", "-c"}, defaultValue = "100")
    private int count;

    @Override
    public void run() {
        createDefaultBuildProcessor().process(query, count, getBuildConsumer());
        System.out.println(getReport());
    }

    protected abstract BuildConsumer getBuildConsumer();

    protected abstract String getReport();

    private static BuildProcessor createDefaultBuildProcessor() {
        final var config = loadConfig();
        final var serverUrl = config.serverUrl();
        final var accessKey = new AccessKeyProvider().getAccessKey(serverUrl).orElse(null);

        final var httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        final var objectMapper = new ObjectMapper().findAndRegisterModules();
        final var develocityClient = new HttpClientDevelocityClient(serverUrl, accessKey, httpClient, objectMapper);

        return new BuildProcessor(develocityClient);
    }

    private static ConfigProperties loadConfig() {
        final var properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from 'config.properties' file", e);
        }
        return new ConfigProperties(URI.create(properties.getProperty("serverUrl")));
    }

    record ConfigProperties(URI serverUrl) {
    }
}
