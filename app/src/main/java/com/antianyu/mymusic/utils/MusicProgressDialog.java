package com.antianyu.mymusic.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.antianyu.mymusic.R;

public class MusicProgressDialog extends Dialog {

    private ImageView imageView;
    private Animation animation;

    public MusicProgressDialog(@NonNull Context context) {
        super(context, R.style.ProgressDialog);

        View dialogView = View.inflate(context, R.layout.progress_dialog, null);
        imageView = dialogView.findViewById(R.id.imageView);
        animation = AnimationUtils.loadAnimation(context, R.anim.progress_dialog);

        setContentView(dialogView);
    }

    @Override
    public void show() {
        super.show();
        imageView.startAnimation(animation);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        imageView.clearAnimation();
    }
}