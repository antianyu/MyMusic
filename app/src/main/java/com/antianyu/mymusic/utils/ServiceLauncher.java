package com.antianyu.mymusic.utils;

import android.content.Context;
import android.content.Intent;

import com.antianyu.mymusic.model.Music;

import java.io.Serializable;
import java.util.List;

/**
 * @author TianyuAn
 */
public class ServiceLauncher {

    public static void launch(Context context, String action) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    public static void launch(Context context, String action, int progress) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        intent.putExtra(MusicService.KEY_PROGRESS, progress);
        context.startService(intent);
    }

    public static void launch(Context context, String action, List<Music> musicList) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        intent.putExtra(MusicService.KEY_MUSIC_LIST, (Serializable) musicList);
        context.startService(intent);
    }

    public static void launch(Context context, String action, Music music, int progress) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        intent.putExtra(MusicService.KEY_MUSIC, music);
        intent.putExtra(MusicService.KEY_PROGRESS, progress);
        context.startService(intent);
    }

    public static void launch(Context context, String action, Music music, int progress, List<Music> musicList) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        intent.putExtra(MusicService.KEY_MUSIC_LIST, (Serializable) musicList);
        intent.putExtra(MusicService.KEY_MUSIC, music);
        intent.putExtra(MusicService.KEY_PROGRESS, progress);
        context.startService(intent);
    }
}
