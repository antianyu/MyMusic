package classes.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class AppPreference
{
	private static AppPreference appPreference = null;
	private Context context = null;

    private String currentMusicPath = "";
    private int currentPosition = 0;
    private String musicDirectory = "";
	
	private AppPreference(Context context)
	{
		this.context = context;
	}
	
	public static synchronized void createAppPreference(Context context)
	{
		if (appPreference == null)
		{
			appPreference = new AppPreference(context);
			appPreference.readAppPreference();
		}
	}
	
	public static AppPreference getAppPreference()
	{
		return appPreference;
	}

	public void readAppPreference()
	{
		SharedPreferences preferences = context.getSharedPreferences("MusicApplication", Application.MODE_PRIVATE);
		appPreference.setCurrentMusicPath(preferences.getString("currentMusicPath", ""));
        appPreference.setCurrentPosition(preferences.getInt("currentPosition", 0));
        appPreference.setMusicDirectory(Environment.getExternalStorageDirectory() + "/Music");
	}
	
	public void saveAppPreference()
	{
		SharedPreferences sharedPreference = context.getSharedPreferences("MusicApplication", Application.MODE_PRIVATE);
		AppPreference appPreference = AppPreference.getAppPreference();
		Editor editor = sharedPreference.edit();
		editor.putString("currentMusicPath", appPreference.getCurrentMusicPath());
        editor.putInt("currentPosition", appPreference.getCurrentPosition());
        editor.apply();
	}

    public String getCurrentMusicPath()
    {
        return currentMusicPath;
    }
    public void setCurrentMusicPath(String currentMusicPath)
    {
        this.currentMusicPath = currentMusicPath;
    }

    public int getCurrentPosition()
    {
        return currentPosition;
    }
    public void setCurrentPosition(int currentPosition)
    {
        this.currentPosition = currentPosition;
    }

    public String getMusicDirectory()
    {
        return musicDirectory;
    }
    public void setMusicDirectory(String musicDirectory)
    {
        this.musicDirectory = musicDirectory;
    }
}