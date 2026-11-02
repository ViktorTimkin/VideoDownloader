package com.edwardnigma.videodownloader.service;

public class ToolUpdateService {
    public UpdateInfo checkForUpdates(){

    }

    public void update(){

    }
    public record UpdateInfo(
            String installedVersion,
            String latestVersion,
            long downloadSize
    ) {
        public boolean updateAvailable() {
            return !installedVersion.equals(latestVersion);
        }
    }
}
