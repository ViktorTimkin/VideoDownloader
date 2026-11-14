
package com.edwardnigma.videodownloader.service;

import com.edwardnigma.videodownloader.AppVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

public final class ApplicationUpdateService {

    private static final String RELEASE_API =
            "https://api.github.com/repos/ViktorTimkin/VideoDownloader/releases/latest";

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("v?\\d+\\.\\d+\\.\\d+");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public ReleaseInfo checkForUpdates()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RELEASE_API))
                .timeout(Duration.ofSeconds(20))
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
        String description = release.path("body").asText("");
        String releaseUrl = release.path("html_url").asText();

        if (!VERSION_PATTERN.matcher(latestVersion).matches()) {
            throw new IOException("Invalid release version: " + latestVersion);
        }

        if (releaseUrl.isBlank()) {
            throw new IOException("Release URL not found");
        }

        URI uri = URI.create(releaseUrl);

        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !"github.com".equalsIgnoreCase(uri.getHost())
                || !uri.getPath().startsWith(
                "/ViktorTimkin/VideoDownloader/releases/")) {
            throw new IOException("Unexpected release URL");
        }

        boolean updateAvailable = isNewerVersion(
                latestVersion,
                AppVersion.CURRENT
        );

        return new ReleaseInfo(
                latestVersion,
                description,
                uri,
                updateAvailable
        );
    }

    private boolean isNewerVersion(String latest, String installed) {

        String[] latestParts = latest.replaceFirst("^v", "").split("\\.");
        String[] installedParts = installed.replaceFirst("^v", "").split("\\.");

        for (int i = 0; i < 3; i++) {
            int latestNumber = Integer.parseInt(latestParts[i]);
            int installedNumber = Integer.parseInt(installedParts[i]);

            if (latestNumber != installedNumber) {
                return latestNumber > installedNumber;
            }
        }

        return false;
    }

    public record ReleaseInfo(
            String version,
            String description,
            URI releaseUrl,
            boolean updateAvailable
    ) {
    }
}
