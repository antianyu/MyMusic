package classes.utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.List;

import classes.model.Music;

public class MusicService extends Service {
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private int position = 0;
    private List<Music> musicList;
    private int progress;
    private boolean playWhenPrepared = false;
    private boolean sendBroadcast = false;
    private Thread actionThread;

    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(MusicApplication.getContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                try {
                    playWhenPrepared = true;
                    sendBroadcast = true;
                    position++;
                    if (position == musicList.size()) {
                        position = 0;
                    }
                    progress = 0;
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(musicList.get(position).getPath());
                    mediaPlayer.prepareAsync();
                } catch (Exception ignore) {

                }
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.seekTo(progress * 1000);
                if (playWhenPrepared) {
                    playWhenPrepared = false;
                    play();
                }
                if (sendBroadcast) {
                    sendBroadcast = false;
                    Intent intent = new Intent(Constant.ACTION_UPDATE_MUSIC);
                    intent.putExtra("music", musicList.get(position));
                    sendBroadcast(intent);
                }
            }
        });

        actionThread = new Thread(new ActionRunnable());
    }

    @SuppressWarnings("unchecked")
    public IBinder onBind(Intent intent) {
        setMusicList((List<Music>) intent.getSerializableExtra("musicList"));
        setCurrentMusic((Music) intent.getSerializableExtra("music"), intent.getIntExtra("progress", 0));
        return binder;
    }

    public class MusicBinder extends Binder {
        public MusicService getServices() {
            return MusicService.this;
        }
    }

    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
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
                    playWhenPrepared = true;
                    setCurrentMusic((Music) intent.getSerializableExtra("music"), intent.getIntExtra("progress", 0));
                    break;
                case Constant.ACTION_UPDATE_MUSIC_LIST:
                    setMusicList((List<Music>) intent.getSerializableExtra("musicList"));
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    private void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    private void setCurrentMusic(Music music, int progress) {
        try {
            if (MusicUtils.exists(music)) {
                this.position = musicList.indexOf(music);
                if (position < 0) {
                    position = 0;
                }
                this.progress = progress;
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicList.get(position).getPath());
                mediaPlayer.prepareAsync();
            } else {
                mediaPlayer.reset();
                actionThread.interrupt();
            }
        } catch (Exception e) {
            mediaPlayer.reset();
            actionThread.interrupt();
        }
    }

    private void setProgress(int progress) {
        this.progress = progress;
        try {
            mediaPlayer.seekTo(progress * 1000);
        } catch (Exception ignore) {

        }
    }

    private void play() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying() && !musicList.isEmpty()) {
                mediaPlayer.start();
            }
            if (!actionThread.isAlive()) {
                actionThread.start();
            }
        } catch (Exception ignore) {
            actionThread.interrupt();
        }
    }

    private void pause() {
        try {
            actionThread.interrupt();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception ignore) {

        }
    }

    private class ActionRunnable implements Runnable {
        public void run() {
            try {
                while (mediaPlayer.isPlaying()) {
                    Intent intent = new Intent(Constant.ACTION_UPDATE_PROGRESS);
                    intent.putExtra("progress", mediaPlayer.getCurrentPosition() / 1000);
                    sendBroadcast(intent);
                    Thread.sleep(Constant.UPDATE_INTERVAL);
                }
            } catch (Exception ignore) {

            }
        }
    }
}