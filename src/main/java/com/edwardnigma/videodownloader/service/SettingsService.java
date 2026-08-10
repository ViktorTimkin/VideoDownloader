package com.edwardnigma.videodownloader.service;

import java.util.prefs.Preferences;

public final class SettingsService {
    private static final Preferences preferences = Preferences.userNodeForPackage(SettingsService.class);
    private static final String OPEN_FOLDER = "openFolder";
    private static final String LAST_FOLDER = "lastFolder";

    public static void saveLastFolder(String path) {
        preferences.put(LAST_FOLDER, path);
    }

    public static String getLastFolder() {
        return preferences.get(LAST_FOLDER, System.getProperty("user.home"));
    }

    public static boolean isOpenFolderAfterDownload() {
        return preferences.getBoolean(OPEN_FOLDER, false); // в начале по умолчанию галочка снята.
    }

    public static void saveOpenFolderAfterDownload(boolean value) {
        preferences.putBoolean(OPEN_FOLDER, value);
    }
}
