package classes.utils;

import android.util.Log;

public class LogUtils
{
    private static final String TAG = "music";
    private static final boolean debugMode = false;

    public static void println(Object object)
    {
        if (debugMode)
        {
            Log.i(TAG, object.toString());
        }
    }

    public static void println(Object object, String tag)
    {
        if (debugMode)
        {
            Log.i(tag, object.toString());
        }
    }

    public static void printError(Object object)
    {
        if (debugMode)
        {
            Log.e(TAG, object.toString());
        }
    }

    public static void tempPrint(Object object)
    {
        Log.i(TAG, object.toString());
    }
}