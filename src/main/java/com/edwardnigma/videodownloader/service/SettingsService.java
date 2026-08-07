package com.edwardnigma.videodownloader.service;

import java.util.prefs.Preferences;

public class SettingsService {
    private static final Preferences preferences = Preferences.userNodeForPackage(SettingsService.class);
    private static final String OPEN_FOLDER = "openFolder";

    public static void saveLastFolder(String path){
        preferences.put("lastFolder", path);
    }

    public static String getLastFolder(){
        return preferences.get("lastFolder", System.getProperty("user.home"));
    }

    public static boolean isOpenFolderAfterDownload() {
        return preferences.getBoolean(OPEN_FOLDER, false); // в начале по умолчанию галочка снята.
    }
    public static void saveOpenFolderAfterDownload(boolean value) {
        preferences.putBoolean(OPEN_FOLDER, value);
    }
}
