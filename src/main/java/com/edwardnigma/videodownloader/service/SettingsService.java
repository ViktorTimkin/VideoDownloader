package com.edwardnigma.videodownloader.service;

import java.util.prefs.Preferences;

public class SettingsService {
    private static final Preferences preferences = Preferences.userNodeForPackage(SettingsService.class);

    public static void saveLastFolder(String path){
        preferences.put("lastFolder", path);
    }

    public static String getLastFolder(){
        return preferences.get("lastFolder", System.getProperty("user.home"));
    }
}
