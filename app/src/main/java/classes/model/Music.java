package classes.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import classes.utils.CharacterParser;

public class Music implements Serializable
{
    private String title = "";
    private String artist = "";
    private int duration = 0;
    private String path = "";

    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getArtist()
    {
        return artist;
    }
    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public String getPath()
    {
        return path;
    }
    public void setPath(String path)
    {
        this.path = path;
    }

    public int getDuration()
    {
        return duration;
    }
    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o instanceof Music)
        {
            Music music = (Music) o;
            return getPath().equals(music.getPath());
        }
        return super.equals(o);
    }

    public static boolean exists(Music music)
    {
        if (music == null || music.getPath().isEmpty())
        {
            return false;
        }
        else
        {
            File file = new File(music.getPath());
            return file.exists();
        }
    }

    public static void sortByTitle(List<Music> musicList)
    {
        TreeMap<String, ArrayList<Music>> indexMap = new TreeMap<>(new Comparator<String>()
        {
            public int compare(String s, String s2)
            {
                if (s.equals(s2))
                {
                    return 0;
                }
                else if (s.equals("#"))
                {
                    return 1;
                }
                else if (s2.equals("#"))
                {
                    return -1;
                }
                else
                {
                    return s.compareTo(s2);
                }
            }
        });

        for (Music music : musicList)
        {
            String initLetter = CharacterParser.getInitLetter(music.getTitle());
            ArrayList<Music> letterList = indexMap.get(initLetter);
            if (letterList == null)
            {
                letterList = new ArrayList<>();
            }
            letterList.add(music);
            indexMap.put(initLetter, letterList);
        }

        musicList.clear();
        for (Map.Entry<String, ArrayList<Music>> entry: indexMap.entrySet())
        {
            ArrayList<Music> values = entry.getValue();
            musicList.addAll(values);
        }
    }
}
