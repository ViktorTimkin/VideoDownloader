package com.edwardnigma.videodownloader.service;


import com.edwardnigma.videodownloader.ui.QualityVideo;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Path;

public class DownloaderService {
    private Process currentProcess;

    public Task<Void> download(String url, String savePath, QualityVideo qualityVideo) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                int currentVideo = 1;
                int totalVideos = 1;
                String fileName = "";
                List<String> command = buildCommand(url, savePath, qualityVideo);
                System.out.println(command);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                try {
                    currentProcess = processBuilder.start();
                    //нужно узнать что он получает и прочитать процесс
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(currentProcess.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)%");
                    // для отображения прогресса
                    Pattern playlistPattern = Pattern.compile("Downloading item (\\d+) of (\\d+)");
                    Pattern fileNamePattern = Pattern.compile("Destination: (.+)");

                    while (!isCancelled() && (line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        Matcher playlistMatcher = playlistPattern.matcher(line);
                        Matcher fileNameMatcher = fileNamePattern.matcher(line);
                        if (line.contains("[download]")) {
                            Matcher matcher = pattern.matcher(line);

                            if (matcher.find()) {

                                double progress = Double.parseDouble(matcher.group(1));

                                updateProgress(progress, 100);
                            }
                        }
                        if (playlistMatcher.find()) {
                            currentVideo = Integer.parseInt(playlistMatcher.group(1));
                            totalVideos = Integer.parseInt(playlistMatcher.group(2));
                            updateMessage("Видео " + currentVideo +
                                    " из " + totalVideos +
                                    " — " + fileName);
                        }
                        if (fileNameMatcher.find()) {
                            fileName = Path.of(fileNameMatcher.group(1))
                                    .getFileName()
                                    .toString();
                            fileName = fileName.replaceFirst("\\.f\\d+\\.[^.]+$", "");
                            updateMessage("Видео " + currentVideo +
                                    " из " + totalVideos +
                                    " — " + fileName);
                        }

                    }
                    int exitCode = currentProcess.waitFor();
                    System.out.println("exitCode: " + exitCode);

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    currentProcess = null;
                }
                return null;
            }
        };
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
        command.add(Tools.YT_DLP.toString());
        command.add("--encoding");
        command.add("utf-8");
        command.add("-f");
        switch (qualityVideo) {
            case P480 -> command.add("bv*[height<=480]+ba/b[height<=480]");
            case P720 -> command.add("bv*[height<=720]+ba/b[height<=720]");
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
        command.add(Tools.TOOLS.toString());
        command.add("-P");
        command.add(savePath);
        command.add(url);

        return command;
    }

    private boolean isPlaylist(String url) {
        return url.contains("/playlist?");
    }

}
