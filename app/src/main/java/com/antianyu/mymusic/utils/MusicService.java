package com.antianyu.mymusic.utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.antianyu.mymusic.model.Music;

import java.util.List;

public class MusicService extends Service {

    public static final String KEY_MUSIC_LIST = "musicList";
    public static final String KEY_MUSIC = "music";
    public static final String KEY_PROGRESS = "progress";

    private MediaPlayer mediaPlayer;
    private int position = 0;
    private List<Music> musicList;
    private int progress;
    private boolean playWhenPrepared = false;
    private boolean sendBroadcast = false;
    private Thread actionThread;

    public void onCreate() {
        super.onCreate();
        initMediaPlayer();

        actionThread = new Thread(() -> {
            try {
                while (mediaPlayer.isPlaying()) {
                    Intent intent = new Intent(Constant.ACTION_UPDATE_PROGRESS);
                    intent.putExtra(MusicService.KEY_PROGRESS, mediaPlayer.getCurrentPosition() / 1000);
                    sendBroadcast(intent);
                    Thread.sleep(Constant.UPDATE_INTERVAL);
                }
            } catch (Exception ignore) {}
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            int progress = intent.getIntExtra(MusicService.KEY_PROGRESS, 0);
            Music music = (Music) intent.getSerializableExtra(MusicService.KEY_MUSIC);
            List<Music> musicList = (List<Music>) intent.getSerializableExtra(MusicService.KEY_MUSIC_LIST);
            switch (intent.getAction()) {
                case Constant.ACTION_CREATE:
                    setMusicList(musicList);
                    setCurrentMusic(music, progress);
                    break;
                case Constant.ACTION_PLAY:
                    play();
                    break;
                case Constant.ACTION_PAUSE:
                    pause();
                    break;
                case Constant.ACTION_UPDATE_PROGRESS:
                    setProgress(progress);
                    play();
                    break;
                case Constant.ACTION_UPDATE_MUSIC:
                    playWhenPrepared = true;
                    setCurrentMusic(music, progress);
                    break;
                case Constant.ACTION_UPDATE_MUSIC_LIST:
                    setMusicList(musicList);
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(MusicApplication.getContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnCompletionListener(mp -> {
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
            } catch (Exception ignore) {}
        });
        mediaPlayer.setOnPreparedListener(mp -> {
            mediaPlayer.seekTo(progress * 1000);
            if (playWhenPrepared) {
                playWhenPrepared = false;
                play();
            }
            if (sendBroadcast) {
                sendBroadcast = false;
                Intent intent = new Intent(Constant.ACTION_UPDATE_MUSIC);
                intent.putExtra(MusicService.KEY_MUSIC, musicList.get(position));
                sendBroadcast(intent);
            }
        });
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
        } catch (Exception ignore) {}
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
        } catch (Exception ignore) {}
    }

    private class ActionRunnable implements Runnable {
        public void run() {
            try {
                while (mediaPlayer.isPlaying()) {
                    Intent intent = new Intent(Constant.ACTION_UPDATE_PROGRESS);
                    intent.putExtra(MusicService.KEY_PROGRESS, mediaPlayer.getCurrentPosition() / 1000);
                    sendBroadcast(intent);
                    Thread.sleep(Constant.UPDATE_INTERVAL);
                }
            } catch (Exception ignore) {}
        }
    }
}