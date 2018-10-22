package com.antianyu.mymusic.model;

import com.antianyu.mymusic.MainActivity;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Music implements Serializable {

    private String title = "";
    private String artist = "";
    private int duration = 0;
    private String path = "";

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Music) {
            Music music = (Music) obj;
            return getPath().equals(music.getPath());
        }
        return super.equals(obj);
    }
}
