package classes.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import classes.model.Music;

/**
 * @author TianyuAn
 */
public class MusicUtils {

    public static boolean exists(Music music) {
        if (music == null || music.getPath().isEmpty()) {
            return false;
        } else {
            File file = new File(music.getPath());
            return file.exists();
        }
    }

    public static void sortByTitle(List<Music> musicList) {
        TreeMap<String, ArrayList<Music>> indexMap = new TreeMap<>((s, s2) -> {
            if (s.equals(s2)) {
                return 0;
            } else if (s.equals("#")) {
                return 1;
            } else if (s2.equals("#")) {
                return -1;
            } else {
                return s.compareTo(s2);
            }
        });

        for (Music music : musicList) {
            String initLetter = CharacterParser.getInitLetter(music.getTitle());
            ArrayList<Music> letterList = indexMap.get(initLetter);
            if (letterList == null) {
                letterList = new ArrayList<>();
            }
            letterList.add(music);
            indexMap.put(initLetter, letterList);
        }

        musicList.clear();
        for (Map.Entry<String, ArrayList<Music>> entry : indexMap.entrySet()) {
            ArrayList<Music> values = entry.getValue();
            musicList.addAll(values);
        }
    }

    public static TreeMap<String, ArrayList<Music>> getIndexMap() {
        return new TreeMap<>((s, s2) -> {
            if (s.equals(s2)) {
                return 0;
            } else if (s.equals("#")) {
                return 1;
            } else if (s2.equals("#")) {
                return -1;
            } else {
                return s.compareTo(s2);
            }
        });
    }

    public static List<Music> getAllMusics() {
        List<Music> musicList = new ArrayList<>();
        String directory = Environment.getExternalStorageDirectory() + "/Music";
        Context context = MusicApplication.getContext();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            new String[] {
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
            }, MediaStore.Audio.Media.DATA + " LIKE ?", new String[] { directory + "%" },
            null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                musicList.add(getMusicFromCursor(cursor));
            }
            cursor.close();
        }
        return musicList;
    }

    public static Music getCurrentMusic() {
        AppPreference preference = AppPreference.getAppPreference();
        String musicPath = preference.getCurrentMusicPath();
        if (TextUtils.isEmpty(musicPath)) {
            return null;
        }

        Music music = null;
        File file = new File(musicPath);
        if (file.exists()) {
            ContentResolver contentResolver = MusicApplication.getContext().getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION
                }, MediaStore.Audio.Media.DATA + " = ?", new String[] { musicPath }, null);

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    music = getMusicFromCursor(cursor);
                }
                cursor.close();
            }
        }
        return music;
    }

    private static Music getMusicFromCursor(Cursor cursor) {
        Music music = new Music();
        music.setTitle(getStringFromCursor(cursor, MediaStore.Audio.Media.TITLE));
        music.setArtist(getStringFromCursor(cursor, MediaStore.Audio.Media.ARTIST));
        music.setDuration(getIntFromCursor(cursor, MediaStore.Audio.Media.DURATION) / 1000);
        music.setPath(getStringFromCursor(cursor, MediaStore.Audio.Media.DATA));
        return music;
    }

    private static String getStringFromCursor(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private static int getIntFromCursor(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }
}