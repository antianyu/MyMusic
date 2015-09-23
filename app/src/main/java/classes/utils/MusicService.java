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
    private int message;
    private int position = 0;
    private List<Music> musicList;
    private int progress;

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
                }
                catch (Exception ignore)
                {

                }
//                Intent intent = new Intent(Constant.ACTION_UPDATE);
//                intent.putExtra("music", musicList.get(position));
//                sendBroadcast(intent);
            }
        });
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
            case Constant.ACTION_CHANGE_PROGRESS:
                setProgress(intent.getIntExtra("progress", 0));
                play();
                break;
            case Constant.ACTION_CHANGE_MUSIC:
                setCurrentMusic((Music) intent.getSerializableExtra("music"));
                setProgress(intent.getIntExtra("progress", 0));
                play();
                break;
            case Constant.ACTION_CHANGE_MUSIC_LIST:
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
            }
        }
        catch (Exception e)
        {
            mediaPlayer.reset();
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
            if (mediaPlayer != null && !mediaPlayer.isPlaying() && !musicList.isEmpty())
            {
                mediaPlayer.start();
            }
        }
        catch (Exception ignore)
        {

        }
    }

    private void pause()
    {
        try
        {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
            }
        }
        catch (Exception ignore)
        {

        }
    }

//    private Handler handler = new Handler()
//    {
//        public void handleMessage(android.os.Message msg)
//        {
//            if (msg.what == 1)
//            {
//                if (mediaPlayer != null)
//                {
//                    progress = mediaPlayer.getProgress(); // 获取当前音乐播放的位置
//                    Intent intent = new Intent();
//                    intent.setAction(Constant.MUSIC_PROGRESS);
//                    intent.putExtra("progress", progress);
//                    sendBroadcast(intent); // 给Activity发送广播
//                    handler.sendEmptyMessageDelayed(1, 1000);
//                }
//            }
//        }
//    };
}