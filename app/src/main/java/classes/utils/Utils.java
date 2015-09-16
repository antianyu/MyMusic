package classes.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.text.DecimalFormat;
import java.util.Date;

public class Utils
{
	public static String getAppVersion()
	{
		Context context = MusicApplication.getContext();
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
			return "";
		}
	}

    public static int getCurrentTime()
    {
        Date date = new Date();
        long result = date.getTime() / 1000;
        return (int) result;
    }
	
    public static String formatDouble(double arg)
    {
		DecimalFormat format = new DecimalFormat("#0.00");
		return format.format(arg);
    }

    public static String formatTime(int time)
    {
        String result = "";

        // init second
        int remainder = time % 60;
        if (remainder < 10)
        {
            result += "0";
        }
        result += remainder;

        // init minute
        time = time / 60;
        remainder = time % 60;
        String minute = "";
        if (remainder < 10)
        {
            minute += "0";
        }
        result = minute + remainder + ":" + result;

        // init hour
        time = time / 60;
        remainder = time % 60;
        if (remainder >= 10)
        {
            result = remainder + ":" + result;
        }
        else if (remainder > 0)
        {
            result = "0" + remainder + ":" + result;
        }

        return result;
    }
    
    public static double parseDouble(String arg)
    {
    	return arg.equals("") ? 0 : Double.valueOf(arg);
    }
    
    public static int booleanToInt(boolean b)
    {
    	return b ? 1 : 0;
    }
}