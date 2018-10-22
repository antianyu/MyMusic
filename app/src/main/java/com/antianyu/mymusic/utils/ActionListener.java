package com.antianyu.mymusic.utils;

import com.antianyu.mymusic.model.Music;

/**
 * @author TianyuAn
 */
public interface ActionListener {

    void onMusicUpdate(Music music);

    void onProgressUpdate(int progress);

    void onPlay();

    void onPause();

    void onNext();

    void onPrevious();

    void onClose();
}
