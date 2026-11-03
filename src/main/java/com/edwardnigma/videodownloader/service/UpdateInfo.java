
package com.edwardnigma.videodownloader.service;

public record UpdateInfo(
        String installedVersion,
        String latestVersion,
        long downloadSize
) {

    public boolean updateAvailable() {
        String[] installed = installedVersion.split("\\.");
        String[] latest = latestVersion.split("\\.");

        int length = Math.max(installed.length, latest.length);

        for (int i = 0; i < length; i++) {
            int currentPart = i < installed.length
                    ? Integer.parseInt(installed[i]) : 0;

            int latestPart = i < latest.length
                    ? Integer.parseInt(latest[i]) : 0;

            if (latestPart != currentPart) {
                return latestPart > currentPart;
            }
        }

        return false;
    }
}
