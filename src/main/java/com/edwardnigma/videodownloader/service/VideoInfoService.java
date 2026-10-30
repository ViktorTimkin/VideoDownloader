package com.edwardnigma.videodownloader.service;

import com.edwardnigma.videodownloader.ui.QualityVideo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class VideoInfoService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Task<String>checkSize(String url, QualityVideo quality) {
        AtomicReference<Process> processRef = new AtomicReference<>();
        return new Task<>() {

            @Override
            protected String call() throws Exception {

                List<String> command = new ArrayList<>();
                command.add(ToolPaths.YT_DLP.toString());
                command.add("--dump-json");
                command.add("--simulate");
                command.add("--no-warnings");
                command.add("-f");
                command.add(getFormat(quality));

                if (!url.contains("/playlist?")) {
                    command.add("--no-playlist");
                }

                command.add(url);

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

                long totalSize = 0;
                int videoCount = 0;
                int unknownCount = 0;
                boolean approximate = false;

                try {
                    Process process = processBuilder.start();
                    processRef.set(process);

                    if (isCancelled()) {
                        process.destroyForcibly();
                        return null;
                    }

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream(),
                                    StandardCharsets.UTF_8))) {

                        String line;

                        while (!isCancelled() && (line = reader.readLine()) != null) {
                            JsonNode video = objectMapper.readTree(line);

                            if (video == null || !video.isObject()) {
                                continue;
                            }

                            videoCount++;

                            JsonNode formats = video.path("requested_formats");
                            long videoSize = 0;
                            boolean sizeKnown = true;

                            if (formats.isArray() && !formats.isEmpty()) {

                                for (JsonNode format : formats) {
                                    JsonNode size = format.path("filesize");

                                    if (!size.isNumber()) {
                                        size = format.path("filesize_approx");
                                        approximate = true;
                                    }

                                    if (size.isNumber()) {
                                        videoSize += size.asLong();
                                    } else {
                                        sizeKnown = false;
                                    }
                                }

                            } else {
                                JsonNode size = video.path("filesize");

                                if (!size.isNumber()) {
                                    size = video.path("filesize_approx");
                                    approximate = true;
                                }

                                if (size.isNumber()) {
                                    videoSize = size.asLong();
                                } else {
                                    sizeKnown = false;
                                }
                            }

                            if (sizeKnown) {
                                totalSize += videoSize;
                            } else {
                                unknownCount++;
                            }

                            updateMessage("Checking: " + videoCount + " videos");
                        }
                    }

                    int exitCode = process.waitFor();

                    if (isCancelled()) {
                        return null;
                    }

                    if (exitCode != 0 || videoCount == 0) {
                        return "Size unavailable";
                    }

                    if (unknownCount > 0) {
                        return "Size unavailable for "
                                + unknownCount + " of "
                                + videoCount + " videos";
                    }

                    double sizeMiB = totalSize / (1024.0 * 1024.0);
                    double sizeGiB = sizeMiB / 1024.0;

                    String formattedSize = sizeGiB >= 1
                            ? String.format(Locale.US, "%.2f GiB", sizeGiB)
                            : String.format(Locale.US, "%.2f MiB", sizeMiB);

                    return (approximate ? "Estimated size: " : "Size: ")
                            + formattedSize;

                } finally {
                    Process process = processRef.getAndSet(null);
                    if (process != null && process.isAlive()) {
                        process.destroyForcibly();
                    }
                }
            }

            @Override
            protected void cancelled() {
                Process process = processRef.getAndSet(null);

                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                }
            }
        };
    }

    private String getFormat(QualityVideo quality) {
        return switch (quality) {
            case P360 -> "bv*[height<=360]+ba/b[height<=360]";
            case P480 -> "bv*[height<=480]+ba/b[height<=480]";
            case P720 -> "bv*[height<=720]+ba/b[height<=720]";
            case P1080 -> "bv*[height<=1080]+ba/b[height<=1080]";
        };
    }
}
