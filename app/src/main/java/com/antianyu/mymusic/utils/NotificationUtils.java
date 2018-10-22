package com.antianyu.mymusic.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.antianyu.mymusic.MainActivity;
import com.antianyu.mymusic.R;

/**
 * @author TianyuAn
 */

public class NotificationUtils {

    public static Notification buildNotification(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.view_notification);
        views.setImageViewResource(R.id.previousImageView, R.drawable.notification_previous);
        views.setImageViewResource(R.id.playImageView, R.drawable.notification_play);
        views.setImageViewResource(R.id.pauseImageView, R.drawable.notification_pause);
        views.setImageViewResource(R.id.nextImageView, R.drawable.notification_next);
        views.setTextViewText(R.id.titleTextView, ViewUtils.getString(R.string.app_name));
        views.setTextViewText(R.id.artistTextView, ViewUtils.getString(R.string.app_name));

        Intent intent = new Intent(Constant.ACTION_PREVIOUS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.previousImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_PLAY);
        pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.playImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_PAUSE);
        pendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.pauseImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_NEXT);
        pendingIntent = PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nextImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_CLOSE);
        pendingIntent = PendingIntent.getBroadcast(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.closeImageView, pendingIntent);

        intent = new Intent(context, MainActivity.class);
        pendingIntent = PendingIntent.getBroadcast(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, "MyMusic")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(ViewUtils.getString(R.string.app_name))
            .setOngoing(true)
            .setContent(views)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build();
        notification.bigContentView = views;
        return notification;
    }
}
