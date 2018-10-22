package com.antianyu.mymusic.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import lombok.Getter;
import lombok.Setter;

public class AppPreference {

    private static final String KEY_CURRENT_MUSIC_PATH = "currentMusicPath";
    private static final String KEY_PROGRESS = "progress";

    private static AppPreference appPreference = null;
    private SharedPreferences preferences;

    @Getter @Setter private String musicDirectory = "";
    @Getter @Setter private String currentMusicPath = "";
    @Getter @Setter private int progress = 0;

    private AppPreference(Context context) {
        preferences = context.getSharedPreferences("MusicApplication", Application.MODE_PRIVATE);
        musicDirectory = Environment.getExternalStorageDirectory() + "/Music";
    }

    public static synchronized void createAppPreference(Context context) {
        if (appPreference == null) {
            appPreference = new AppPreference(context);
            appPreference.readAppPreference();
        }
    }

    public static AppPreference getAppPreference() {
        return appPreference;
    }

    public void save() {
        preferences.edit()
            .putString(KEY_CURRENT_MUSIC_PATH, appPreference.getCurrentMusicPath())
            .putInt(KEY_PROGRESS, appPreference.getProgress())
            .apply();
    }

    private void readAppPreference() {
        appPreference.setCurrentMusicPath(preferences.getString(KEY_CURRENT_MUSIC_PATH, ""));
        appPreference.setProgress(preferences.getInt(KEY_PROGRESS, 0));
    }
}