
package com.edwardnigma.videodownloader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class ToolUpdateService {

    private static final String RELEASE_API =
            "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest";

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration VERSION_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration UPDATE_TIMEOUT = Duration.ofHours(2);

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}(?:\\.\\d+)?");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public UpdateInfo checkForUpdates()
            throws IOException, InterruptedException {

        String installedVersion = getInstalledVersion();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RELEASE_API))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "VideoDownloader")
                .GET()
                .build();

        HttpResponse<String> response;

        try {
            response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }

        if (response.statusCode() != 200) {
            throw new IOException(
                    "GitHub API returned HTTP " + response.statusCode()
            );
        }

        JsonNode release = objectMapper.readTree(response.body());

        String latestVersion = release.path("tag_name").asText();

        if (!VERSION_PATTERN.matcher(latestVersion).matches()) {
            throw new IOException(
                    "Invalid release version: " + latestVersion
            );
        }

        long downloadSize = 0;

        for (JsonNode asset : release.path("assets")) {
            if ("yt-dlp.exe".equals(asset.path("name").asText())) {
                downloadSize = asset.path("size").asLong();
                break;
            }
        }

        if (downloadSize <= 0) {
            throw new IOException("yt-dlp.exe release asset not found");
        }

        return new UpdateInfo(
                installedVersion,
                latestVersion,
                downloadSize
        );
    }

    public String update() throws IOException, InterruptedException {

        // Use the same stable channel as the GitHub release check.
        runCommand(
                List.of(
                        ToolPaths.YT_DLP.toString(),
                        "--update-to",
                        "stable"
                ),
                UPDATE_TIMEOUT
        );

        // Return the installed version for verification by the UI.
        return getInstalledVersion();
    }

    private String getInstalledVersion()
            throws IOException, InterruptedException {

        String version = runCommand(
                List.of(ToolPaths.YT_DLP.toString(), "--version"),
                VERSION_TIMEOUT
        );

        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IOException(
                    "Invalid installed yt-dlp version: " + version
            );
        }

        return version;
    }

    private String runCommand(List<String> command, Duration timeout)
            throws IOException, InterruptedException {

        Path outputFile = Files.createTempFile(
                "video-downloader-",
                ".log"
        );

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(outputFile.toFile());

            Process process = builder.start();

            try {
                boolean finished = process.waitFor(
                        timeout.toMillis(),
                        TimeUnit.MILLISECONDS
                );

                if (!finished) {
                    throw new IOException(
                            "yt-dlp command timed out"
                    );
                }

                String output = Files.readString(
                        outputFile,
                        StandardCharsets.UTF_8
                ).trim();

                if (process.exitValue() != 0) {
                    throw new IOException(
                            "yt-dlp command failed: " + output
                    );
                }

                return output;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;

            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();

                    try {
                        process.waitFor(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        } finally {
            Files.deleteIfExists(outputFile);
        }
    }
}
