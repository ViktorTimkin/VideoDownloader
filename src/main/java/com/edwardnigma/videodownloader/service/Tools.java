package com.edwardnigma.videodownloader.service;

import java.nio.file.Path;

public class Tools {
    public static final Path YT_DLP = Path.of("tools", "yt-dlp.exe");
    public static final Path FFMPEG = Path.of("tools", "ffmpeg.exe");
    public static final Path FFPROBE = Path.of("tools", "ffprobe.exe");

    private Tools() {}
}
