package com.edwardnigma.videodownloader.service;

import java.nio.file.Path;

public final class ToolPaths {
    public static final Path TOOLS_DIRECTORY = Path.of("tools");
    public static final Path YT_DLP = TOOLS_DIRECTORY.resolve("yt-dlp.exe");
    public static final Path FFMPEG = TOOLS_DIRECTORY.resolve("ffmpeg.exe");
    public static final Path FFPROBE = TOOLS_DIRECTORY.resolve("ffprobe.exe");

    private ToolPaths() {
    }
}
