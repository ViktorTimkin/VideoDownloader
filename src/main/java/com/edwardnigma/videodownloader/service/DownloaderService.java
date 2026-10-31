package com.edwardnigma.videodownloader.service;


import com.edwardnigma.videodownloader.ui.QualityVideo;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Path;

public class DownloaderService {
    private Process currentProcess;
    private static final Pattern PROGRESS_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)%");
    private static final Pattern PLAYLIST_PATTERN = Pattern.compile("Downloading item (\\d+) of (\\d+)");
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("Destination: (.+)");
    private static final Pattern SIZE_PATTERN = Pattern.compile("of\\s+~?([\\d.]+(?:KiB|MiB|GiB|TiB))");

    public Task<Void> download(String url, String savePath, QualityVideo qualityVideo) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                int currentVideo = 1;
                int totalVideos = 1;
                String fileName = "";
                String fileSize = "";
                List<String> command = buildCommand(url, savePath, qualityVideo);
                System.out.println(command);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                try {
                    currentProcess = processBuilder.start();
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(currentProcess.getInputStream(), StandardCharsets.UTF_8));
                    String line;

                    while (!isCancelled() && (line = bufferedReader.readLine()) != null) {
                        System.out.println(line);

                        Matcher playlistMatcher = PLAYLIST_PATTERN.matcher(line);
                        Matcher fileNameMatcher = FILE_NAME_PATTERN.matcher(line);

                        if (playlistMatcher.find()) {
                            currentVideo = Integer.parseInt(playlistMatcher.group(1));
                            totalVideos = Integer.parseInt(playlistMatcher.group(2));

                            fileName = "";
                            fileSize = "";

                            updateMessage("Video " + currentVideo + " of " + totalVideos);
                        }

                        if (fileNameMatcher.find()) {
                            fileName = Path.of(fileNameMatcher.group(1))
                                    .getFileName()
                                    .toString();

                            fileName = fileName.replaceFirst("\\.f\\d+\\.[^.]+$", "");

                            updateMessage("Video " + currentVideo +
                                    " of " + totalVideos +
                                    " — " + fileName +
                                    (fileSize.isEmpty() ? "" : "\nSize: " + fileSize));
                        }

                        if (line.contains("[download]")) {
                            Matcher progressMatcher = PROGRESS_PATTERN.matcher(line);
                            Matcher sizeMatcher = SIZE_PATTERN.matcher(line);

                            if (progressMatcher.find()) {
                                double progress = Double.parseDouble(progressMatcher.group(1));
                                updateProgress(progress, 100);

                                if (sizeMatcher.find()) {
                                    fileSize = sizeMatcher.group(1);

                                    double totalSize = Double.parseDouble(
                                            fileSize.replaceAll("[^\\d.]", "")
                                    );

                                    double downloadedSize = totalSize * progress / 100;

                                    updateMessage("Video " + currentVideo +
                                            " of " + totalVideos +
                                            " — " + fileName +
                                            "\nSize: " +
                                            String.format(Locale.US, "%.2f", downloadedSize) +
                                            " / " + fileSize);
                                }
                            }
                        }
                    }
                    int exitCode = currentProcess.waitFor();
                    System.out.println("exitCode: " + exitCode);

                    if (exitCode != 0 && !isCancelled()) {
                        throw new IOException("yt-dlp exited with code: " + exitCode);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                } finally {
                    currentProcess = null;
                }
                return null;
            }
        }

                ;
    }

    public void cancelDownload() throws InterruptedException {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            if (!currentProcess.waitFor(2, TimeUnit.SECONDS)) {
                currentProcess.destroyForcibly();
            }
        }
    }

    private List<String> buildCommand(String url, String savePath, QualityVideo qualityVideo) {
        List<String> command = new ArrayList<>();
        command.add(ToolPaths.YT_DLP.toString());
        command.add("--encoding");
        command.add("utf-8");
        command.add("-f");
        switch (qualityVideo) {
            case P360 -> command.add("bv*[height<=360]+ba/b[height<=360]");
            case P480 -> command.add("bv*[height<=480]+ba/b[height<=480]");
            case P720 -> command.add("bv*[height<=720]+ba/b[height<=720]");
            case P1080 -> command.add("bv*[height<=1080]+ba/b[height<=1080]");
        }

        if (isPlaylist(url)) {
            command.add("-o");
            command.add("%(playlist_title)s/%(playlist_index)03d - %(title)s.%(ext)s");// для нумерации плейлиста
        } else {
            command.add("--no-playlist");
            command.add("-o");
            command.add("%(title)s.%(ext)s");
        }

        command.add("--ffmpeg-location");
        command.add(ToolPaths.TOOLS_DIRECTORY.toString());
        command.add("-P");
        command.add(savePath);
        command.add(url);

        return command;
    }

    private boolean isPlaylist(String url) {
        return url.contains("/playlist?");
    }

}
