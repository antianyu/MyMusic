package com.antianyu.mymusic.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Utils {

    public static String getAppVersion() {
        Context context = MusicApplication.getContext();
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatTime(int time) {
        String result = "";

        // init second
        int remainder = time % 60;
        if (remainder < 10) {
            result += "0";
        }
        result += remainder;

        // init minute
        time = time / 60;
        remainder = time % 60;
        String minute = "";
        if (remainder < 10) {
            minute += "0";
        }
        result = minute + remainder + ":" + result;

        // init hour
        time = time / 60;
        remainder = time % 60;
        if (remainder >= 10) {
            result = remainder + ":" + result;
        } else if (remainder > 0) {
            result = "0" + remainder + ":" + result;
        }

        return result;
    }
}