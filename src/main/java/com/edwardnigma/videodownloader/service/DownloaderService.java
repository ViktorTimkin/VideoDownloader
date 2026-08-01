package com.edwardnigma.videodownloader.service;


import com.edwardnigma.videodownloader.ui.QualityVideo;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DownloaderService {

    public Task<Void> download(String url, String savePath, QualityVideo qualityVideo) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<String> command = buildCommand(url, savePath, qualityVideo);
                System.out.println(command);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                try {
                    Process process = processBuilder.start();
                    //нужно узнать что он получает и прочитать процесс
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)%");
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        if (line.contains("[download]")) {
                            Matcher matcher = pattern.matcher(line);

                            if (matcher.find()) {

                                double progress = Double.parseDouble(matcher.group(1));

                                updateProgress(progress, 100);
                            }
                        }

                    }
                    int exitCode = process.waitFor();
                    System.out.println("exitCode: " + exitCode);

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
    }

    private List<String> buildCommand(String url, String savePath, QualityVideo qualityVideo) {
        List<String> command = new ArrayList<>();
        command.add(Tools.YT_DLP.toString());
        command.add("-f");
        switch (qualityVideo) {
            case P480 -> command.add("bv*[height<=480]+ba/b[height<=480]");
            case P720 -> command.add("bv*[height<=720]+ba/b[height<=720]");
        }
        command.add("--ffmpeg-location");
        command.add(Tools.TOOLS.toString());
        command.add("-P");
        command.add(savePath);
        command.add(url);

        return command;
    }

}
