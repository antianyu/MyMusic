package com.antianyu.mymusic.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

public class ViewUtils {

    public static String getString(int resID) {
        return MusicApplication.getContext().getResources().getString(resID);
    }

    public static String getString(int resID, Object... args) {
        return MusicApplication.getContext().getResources().getString(resID, args);
    }

    public static int getColor(int resID) {
        return MusicApplication.getContext().getResources().getColor(resID);
    }

    public static void showToast(int resID) {
        Toast.makeText(MusicApplication.getContext(), resID, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(String content) {
        Toast.makeText(MusicApplication.getContext(), content, Toast.LENGTH_SHORT).show();
    }

    public static int dpToPixel(double dp) {
        DisplayMetrics metrics = MusicApplication.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, metrics);
    }

    public static int getPhoneWindowHeight() {
        DisplayMetrics metrics = MusicApplication.getContext().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static int getStatusBarHeight() {
        int resourceId =
            MusicApplication.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? MusicApplication.getContext().getResources().getDimensionPixelSize(resourceId) : 0;
    }

    public static int getActionBarHeight() {
        Context context = MusicApplication.getContext();
        TypedValue typedValue = new TypedValue();
        MusicApplication.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        return context.getResources().getDimensionPixelSize(typedValue.resourceId);
    }
}
