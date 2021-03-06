package com.antianyu.mymusic.utils;

import android.app.Application;
import android.content.Context;

public class MusicApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        initData();
    }

    public static Context getContext() {
        return context;
    }

    private void initData() {
        context = getApplicationContext();
        AppPreference.createAppPreference(context);
    }
}