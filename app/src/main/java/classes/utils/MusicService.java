package classes.utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

import classes.model.Music;

public class MusicService extends Service
{
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private int position = 0;
    private List<Music> musicList;
    private int progress;
    private Thread actionThread;

    public void onCreate()
    {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                try
                {
                    position++;
                    if (position == musicList.size())
                    {
                        position = 0;
                    }
                    progress = 0;
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(musicList.get(position).getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.seekTo(progress * 1000);
                    play();
                    Intent intent = new Intent(Constant.ACTION_UPDATE_MUSIC);
                    intent.putExtra("music", musicList.get(position));
                    sendBroadcast(intent);
                }
                catch (Exception ignore)
                {

                }
            }
        });

        actionThread = new Thread(new ActionRunnable());
    }

    @SuppressWarnings("unchecked")
    public IBinder onBind(Intent intent)
    {
        setMusicList((List<Music>) intent.getSerializableExtra("musicList"));
        setCurrentMusic((Music) intent.getSerializableExtra("music"));
        setProgress(intent.getIntExtra("progress", 0));
        return binder;
    }

    public class MusicBinder extends Binder
    {
        public MusicService getServices()
        {
            return MusicService.this;
        }
    }

    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        switch (intent.getAction())
        {
            case Constant.ACTION_PLAY:
                play();
                break;
            case Constant.ACTION_PAUSE:
                pause();
                break;
            case Constant.ACTION_UPDATE_PROGRESS:
                setProgress(intent.getIntExtra("progress", 0));
                play();
                break;
            case Constant.ACTION_UPDATE_MUSIC:
                setCurrentMusic((Music) intent.getSerializableExtra("music"));
                setProgress(intent.getIntExtra("progress", 0));
                play();
                break;
            case Constant.ACTION_UPDATE_MUSIC_LIST:
                setMusicList((List<Music>) intent.getSerializableExtra("musicList"));
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy()
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    private void setMusicList(List<Music> musicList)
    {
        this.musicList = musicList;
    }

    private void setCurrentMusic(Music music)
    {
        try
        {
            if (Music.exists(music))
            {
                this.position = musicList.indexOf(music);
                if (position < 0)
                {
                    position = 0;
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicList.get(position).getPath());
                mediaPlayer.prepare();
            }
            else
            {
                mediaPlayer.reset();
                actionThread.interrupt();
            }
        }
        catch (Exception e)
        {
            mediaPlayer.reset();
            actionThread.interrupt();
        }
    }

    private void setProgress(int progress)
    {
        this.progress = progress;
        try
        {
            mediaPlayer.seekTo(progress * 1000);
        }
        catch (Exception ignore)
        {

        }
    }

    private void play()
    {
        try
        {
            actionThread.interrupt();
            if (mediaPlayer != null && !mediaPlayer.isPlaying() && !musicList.isEmpty())
            {
                mediaPlayer.start();
                actionThread = new Thread(new ActionRunnable());
                actionThread.start();
            }
        }
        catch (Exception ignore)
        {
            actionThread.interrupt();
        }
    }

    private void pause()
    {
        try
        {
            actionThread.interrupt();
            if (mediaPlayer != null && mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
            }
        }
        catch (Exception ignore)
        {

        }
    }

    private class ActionRunnable implements Runnable
    {
        public void run()
        {
            try
            {
                while (mediaPlayer.isPlaying())
                {
                    Intent intent = new Intent(Constant.ACTION_UPDATE_PROGRESS);
                    intent.putExtra("progress", mediaPlayer.getCurrentPosition() / 1000);
                    sendBroadcast(intent);
                    Thread.sleep(Constant.UPDATE_INTERVAL);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}