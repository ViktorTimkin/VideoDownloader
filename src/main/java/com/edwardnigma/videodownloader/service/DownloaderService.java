package com.edwardnigma.videodownloader.service;


import com.edwardnigma.videodownloader.ui.QualityVideo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DownloaderService {

    public void  download(String url, String savePath, QualityVideo qualityVideo){
        List<String> command = buildCommand(url, savePath, qualityVideo);
        System.out.println(command);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            processBuilder.start();
            processBuilder.redirectErrorStream(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> buildCommand(String url, String savePath, QualityVideo qualityVideo){
        List<String>command = new ArrayList<>();
        command.add(Tools.YT_DLP.toString());
        command.add("-f");
        switch (qualityVideo){
            case P480 ->  command.add("bv*[height<=480]+ba/b[height<=480]");
            case P720 ->  command.add("bv*[height<=720]+ba/b[height<=720]");
        }
        command.add("-P");
        command.add(savePath);
        command.add(url);

        return command;
    }

}
