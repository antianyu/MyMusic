package com.antianyu.mymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.antianyu.mymusic.model.Music;
import com.antianyu.mymusic.utils.ActionListener;
import com.antianyu.mymusic.utils.Constant;
import com.antianyu.mymusic.utils.MusicService;

/**
 * @author TianyuAn
 */
public class ActionBroadcastReceiver extends BroadcastReceiver {

    private ActionListener actionListener;

    public ActionBroadcastReceiver(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_UPDATE_MUSIC);
        filter.addAction(Constant.ACTION_UPDATE_PROGRESS);
        filter.addAction(Constant.ACTION_PLAY);
        filter.addAction(Constant.ACTION_PAUSE);
        filter.addAction(Constant.ACTION_PREVIOUS);
        filter.addAction(Constant.ACTION_NEXT);
        filter.addAction(Constant.ACTION_CLOSE);
        return filter;
    }

    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(intent.getAction())) {
            return;
        }

        switch (intent.getAction()) {
            case Constant.ACTION_UPDATE_MUSIC: {
                if (actionListener != null) {
                    actionListener.onMusicUpdate((Music) intent.getSerializableExtra(MusicService.KEY_MUSIC));
                }
                break;
            }
            case Constant.ACTION_UPDATE_PROGRESS: {
                if (actionListener != null) {
                    actionListener.onProgressUpdate(intent.getIntExtra(MusicService.KEY_PROGRESS, 0));
                }
                break;
            }
            case Constant.ACTION_PLAY: {
                if (actionListener != null) {
                    actionListener.onPlay();
                }
                break;
            }
            case Constant.ACTION_PAUSE: {
                if (actionListener != null) {
                    actionListener.onPause();
                }
                break;
            }
            case Constant.ACTION_NEXT: {
                if (actionListener != null) {
                    actionListener.onNext();
                }
                break;
            }
            case Constant.ACTION_PREVIOUS: {
                if (actionListener != null) {
                    actionListener.onPrevious();
                }
                break;
            }
            case Constant.ACTION_CLOSE: {
                if (actionListener != null) {
                    actionListener.onClose();
                }
                break;
            }
        }
    }
}
