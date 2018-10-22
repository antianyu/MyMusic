package com.antianyu.mymusic;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import classes.adapter.MusicListViewAdapter;
import classes.model.Music;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.MusicProgressDialog;
import classes.utils.MusicService;
import classes.utils.MusicUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;

public class MainActivity extends AppCompatActivity {

    // Widget
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.musicListView) PinnedSectionListView musicListView;
    @BindView(R.id.promptTextView) TextView promptTextView;
    @BindView(R.id.indexLayout) LinearLayout indexLayout;
    @BindView(R.id.centralTextView) TextView centralTextView;
    @BindView(R.id.playImageView) ImageView playImageView;
    @BindView(R.id.pauseImageView) ImageView pauseImageView;
    @BindView(R.id.titleTextView) TextView titleTextView;
    @BindView(R.id.artistTextView) TextView artistTextView;
    @BindView(R.id.progressSeekBar) SeekBar progressSeekBar;
    @BindView(R.id.progressTextView) TextView progressTextView;
    @BindView(R.id.durationTextView) TextView durationTextView;

    private PopupWindow exitPopupWindow;
    private PopupWindow deletePopupWindow;
    private Notification notification;
    private MusicListViewAdapter adapter;
    private MusicProgressDialog progressDialog;

    private AppPreference appPreference;
    private List<Music> musicList = new ArrayList<>();
    private Music currentMusic;
    private Music chosenMusic;
    private MusicService musicService;
    private ServiceConnection connection;
    private ActionBroadcastReceiver actionBroadcastReceiver;
    private NotificationManager manager;

    // View
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(actionBroadcastReceiver);
        unbindService(connection);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                scanMusic();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        showExitWindow();
        return true;
    }

    private void initView() {
        setSupportActionBar(toolbar);

        initActionView();

        adapter = new MusicListViewAdapter(this, musicList);
        musicListView.setAdapter(adapter);
        musicListView.setOnItemClickListener((parent, view, position, id) -> {
            if (adapter.isMusic(position) && (deletePopupWindow == null || !deletePopupWindow.isShowing())) {
                currentMusic = (Music) adapter.getItem(position);
                setMusicView(currentMusic, 0);
                Intent intent = getServiceIntent(Constant.ACTION_UPDATE_MUSIC);
                intent.putExtra(MusicService.KEY_MUSIC, currentMusic);
                intent.putExtra(MusicService.KEY_PROGRESS, progressSeekBar.getProgress());
                startPlaying(intent);

                adapter.setChosenPosition(position);
                adapter.notifyDataSetChanged();
            }
        });
        musicListView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (adapter.isMusic(position)) {
                chosenMusic = (Music) adapter.getItem(position);
                showDeleteWindow();
            }
            return false;
        });

        initIndexLayout();
        refreshPrompt();
        initExitWindow();
        initDeleteWindow();
        initNotification();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initIndexLayout() {
        indexLayout.removeAllViews();
        final int height = (ViewUtils.getPhoneWindowHeight() - ViewUtils.dpToPixel(110) - ViewUtils.getStatusBarHeight()
            - ViewUtils.getActionBarHeight()) / MusicUtils.INDEX_LETTERS.length;

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        for (String string : MusicUtils.INDEX_LETTERS) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setTextColor(ViewUtils.getColor(R.color.text_dark_major));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            textView.setText(string);
            textView.setGravity(Gravity.CENTER);

            indexLayout.addView(textView);
            indexLayout.setOnTouchListener((v, event) -> {
                float y = event.getY();
                int index = (int) (y / height);
                if (index > -1 && index < MusicUtils.INDEX_LETTERS.length) {
                    String key = MusicUtils.INDEX_LETTERS[index];
                    centralTextView.setVisibility(View.VISIBLE);
                    centralTextView.setText(key);

                    int position = adapter.getPosition(key);
                    if (position >= 0) {
                        musicListView.setSelection(position + musicListView.getHeaderViewsCount(), true);
                    }
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        indexLayout.setBackgroundColor(ViewUtils.getColor(R.color.index_layout_pressed));
                        break;
                    case MotionEvent.ACTION_UP:
                        indexLayout.setBackgroundColor(ViewUtils.getColor(android.R.color.transparent));
                        centralTextView.setVisibility(View.INVISIBLE);
                        break;
                    default:
                        break;
                }
                return true;
            });
        }
    }

    private void initActionView() {
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressTextView.setText(Utils.formatTime(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {
                appPreference.setProgress(seekBar.getProgress());
                appPreference.save();

                if (musicService != null) {
                    Intent intent = getServiceIntent(Constant.ACTION_UPDATE_PROGRESS);
                    intent.putExtra(MusicService.KEY_PROGRESS, seekBar.getProgress());
                    startPlaying(intent);
                }
            }
        });

        ImageView previousImageView = findViewById(R.id.previousImageView);
        previousImageView.setOnClickListener(v -> previous());

        playImageView.setOnClickListener(v -> play());

        pauseImageView.setOnClickListener(v -> pause());

        ImageView nextImageView = findViewById(R.id.nextImageView);
        nextImageView.setOnClickListener(v -> next());
    }

    private void initExitWindow() {
        View exitView = View.inflate(this, R.layout.window_exit, null);

        Button exitButton = exitView.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> close());

        Button cancelButton = exitView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> exitPopupWindow.dismiss());

        exitPopupWindow = ViewUtils.buildBottomPopupWindow(this, exitView);
    }

    private void initDeleteWindow() {
        View deleteView = View.inflate(this, R.layout.window_delete, null);

        Button deleteButton = deleteView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            deletePopupWindow.dismiss();
            new Builder(MainActivity.this)
                .setTitle(R.string.warning)
                .setMessage(String.format(ViewUtils.getString(R.string.delete_confirm), chosenMusic.getTitle()))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    File file = new File(chosenMusic.getPath());
                    if (file.delete()) {
                        ViewUtils.showToast(R.string.succeed_in_deleting);
                        if (chosenMusic.equals(currentMusic)) {
                            currentMusic = musicList.size() > 1 ? findNextMusic() : new Music();
                            choseMusic();
                        }
                        musicList.remove(chosenMusic);
                        refreshView();

                        Intent intent = getServiceIntent(Constant.ACTION_UPDATE_MUSIC_LIST);
                        intent.putExtra(MusicService.KEY_MUSIC_LIST, (Serializable) musicList);
                        startService(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        });

        Button cancelButton = deleteView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> deletePopupWindow.dismiss());

        deletePopupWindow = ViewUtils.buildBottomPopupWindow(this, deleteView);
    }

    private void initNotification() {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.view_notification);
        views.setImageViewResource(R.id.previousImageView, R.drawable.notification_previous);
        views.setImageViewResource(R.id.playImageView, R.drawable.notification_play);
        views.setImageViewResource(R.id.pauseImageView, R.drawable.notification_pause);
        views.setImageViewResource(R.id.nextImageView, R.drawable.notification_next);
        views.setTextViewText(R.id.titleTextView, getString(R.string.app_name));
        views.setTextViewText(R.id.artistTextView, getString(R.string.app_name));

        Intent intent = new Intent(Constant.ACTION_PREVIOUS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.previousImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_PLAY);
        pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.playImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_PAUSE);
        pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.pauseImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_NEXT);
        pendingIntent = PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nextImageView, pendingIntent);

        intent = new Intent(Constant.ACTION_CLOSE);
        pendingIntent = PendingIntent.getBroadcast(this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.closeImageView, pendingIntent);

        intent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getBroadcast(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(this, "MyMusic")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setOngoing(true)
            .setContent(views)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build();
        notification.bigContentView = views;

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(Constant.NOTIFICATION_ID, notification);
        }
    }

    private void showExitWindow() {
        if (!exitPopupWindow.isShowing()) {
            exitPopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
            exitPopupWindow.update();

            ViewUtils.dimBackground(this);
        }
    }

    private void showDeleteWindow() {
        if (!deletePopupWindow.isShowing()) {
            deletePopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
            deletePopupWindow.update();

            ViewUtils.dimBackground(this);
        }
    }

    private void setMusicView(Music music, int progress) {
        saveMusicStatus(music, progress);

        titleTextView.setText(music.getTitle());
        artistTextView.setText(music.getArtist());
        progressSeekBar.setMax(music.getDuration());
        progressSeekBar.setProgress(progress);
        progressTextView.setText(Utils.formatTime(progress));
        durationTextView.setText(Utils.formatTime(music.getDuration()));

        notification.contentView.setTextViewText(R.id.titleTextView, music.getTitle());
        notification.contentView.setTextViewText(R.id.artistTextView, music.getArtist());
        manager.notify(Constant.NOTIFICATION_ID, notification);
    }

    private void setListViewSelection() {
        if (adapter.getChosenPosition() <= musicListView.getFirstVisiblePosition()
            || adapter.getChosenPosition() >= musicListView.getLastVisiblePosition()) {
            musicListView.setSelection(adapter.getChosenPosition(), false);
        }
    }

    private void refreshView() {
        if (!MusicUtils.exists(currentMusic)) {
            stopPlaying();
        }
        adapter.setList(musicList);
        adapter.setChosenMusic(currentMusic);
        adapter.notifyDataSetChanged();
        initIndexLayout();
        refreshPrompt();
        toolbar.setTitle(String.format(ViewUtils.getString(R.string.title), musicList.size()));
        setListViewSelection();
    }

    private void refreshPrompt() {
        int visibility = musicList.isEmpty() ? View.VISIBLE : View.GONE;
        promptTextView.setVisibility(visibility);
    }

    // Data
    private void initData() {
        appPreference = AppPreference.getAppPreference();
        scanMusic();
        initBroadcastReceiver();
    }

    private void initBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_UPDATE_MUSIC);
        filter.addAction(Constant.ACTION_UPDATE_PROGRESS);
        filter.addAction(Constant.ACTION_PLAY);
        filter.addAction(Constant.ACTION_PAUSE);
        filter.addAction(Constant.ACTION_PREVIOUS);
        filter.addAction(Constant.ACTION_NEXT);
        filter.addAction(Constant.ACTION_CLOSE);
        actionBroadcastReceiver = new ActionBroadcastReceiver();
        registerReceiver(actionBroadcastReceiver, filter);
    }

    private void scanMusic() {
        musicList.clear();
        if (progressDialog == null) {
            progressDialog = new MusicProgressDialog(this);
        }
        progressDialog.show();

        new Thread(() -> {
            musicList = MusicUtils.getAllMusics();
            currentMusic = MusicUtils.getCurrentMusic();
            MusicUtils.sortByTitle(musicList);

            runOnUiThread(() -> {
                if (!MusicUtils.exists(currentMusic) && !musicList.isEmpty()) {
                    currentMusic = musicList.get(0);
                    setMusicView(currentMusic, 0);
                } else if (MusicUtils.exists(currentMusic)) {
                    setMusicView(currentMusic, appPreference.getProgress());
                } else {
                    currentMusic = new Music();
                    setMusicView(currentMusic, 0);
                }

                refreshView();
                progressDialog.dismiss();

                // connect to MusicService
                if (connection != null) {
                    unbindService(connection);
                    connection = null;
                }

                connection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        musicService = ((MusicService.MusicBinder) service).getServices();
                    }

                    public void onServiceDisconnected(ComponentName name) {
                        musicService = null;
                    }
                };

                Intent bindIntent = new Intent(MainActivity.this, MusicService.class);
                bindIntent.putExtra(MusicService.KEY_MUSIC_LIST, (Serializable) musicList);
                bindIntent.putExtra(MusicService.KEY_MUSIC, currentMusic);
                bindIntent.putExtra(MusicService.KEY_PROGRESS, progressSeekBar.getProgress());
                bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
            });
        }).start();
    }

    private void saveMusicStatus(Music music, int position) {
        appPreference.setCurrentMusicPath(music.getPath());
        appPreference.setProgress(position);
        appPreference.save();
    }

    // Music
    private void play() {
        if (MusicUtils.exists(currentMusic)) {
            startPlaying(getServiceIntent(Constant.ACTION_PLAY));
        }
    }

    private void pause() {
        stopPlaying();
        saveMusicStatus(currentMusic, progressSeekBar.getProgress());
    }

    private void next() {
        currentMusic = findNextMusic();
        adapter.setChosenMusic(currentMusic);
        adapter.notifyDataSetChanged();
        choseMusic();
    }

    private void previous() {
        currentMusic = findPreviousMusic();
        adapter.setChosenMusic(currentMusic);
        adapter.notifyDataSetChanged();
        choseMusic();
    }

    private void close() {
        manager.cancel(Constant.NOTIFICATION_ID);
        exitPopupWindow.dismiss();
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void startPlaying(Intent intent) {
        playImageView.setVisibility(View.INVISIBLE);
        pauseImageView.setVisibility(View.VISIBLE);
        notification.contentView.setViewVisibility(R.id.playImageView, View.INVISIBLE);
        notification.contentView.setViewVisibility(R.id.pauseImageView, View.VISIBLE);
        manager.notify(Constant.NOTIFICATION_ID, notification);
        startService(intent);
    }

    private void stopPlaying() {
        pauseImageView.setVisibility(View.INVISIBLE);
        playImageView.setVisibility(View.VISIBLE);
        notification.contentView.setViewVisibility(R.id.pauseImageView, View.INVISIBLE);
        notification.contentView.setViewVisibility(R.id.playImageView, View.VISIBLE);
        manager.notify(Constant.NOTIFICATION_ID, notification);
        startService(getServiceIntent(Constant.ACTION_PAUSE));
    }

    public Music findPreviousMusic() {
        if (musicList.isEmpty()) {
            return new Music();
        } else {
            int chosenPosition = musicList.indexOf(currentMusic);
            chosenPosition--;
            if (chosenPosition < 0) {
                chosenPosition += musicList.size();
            }
            return musicList.get(chosenPosition);
        }
    }

    public Music findNextMusic() {
        if (musicList.isEmpty()) {
            return new Music();
        } else {
            int chosenPosition = musicList.indexOf(currentMusic);
            chosenPosition++;
            chosenPosition = chosenPosition % musicList.size();
            return musicList.get(chosenPosition);
        }
    }

    private void choseMusic() {
        setListViewSelection();
        setMusicView(currentMusic, 0);
        Intent intent = getServiceIntent(Constant.ACTION_UPDATE_MUSIC);
        intent.putExtra(MusicService.KEY_MUSIC, currentMusic);
        intent.putExtra(MusicService.KEY_PROGRESS, progressSeekBar.getProgress());
        startPlaying(intent);
    }

    // Auxiliaries
    private class ActionBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (TextUtils.isEmpty(intent.getAction())) {
                return;
            }

            switch (intent.getAction()) {
                case Constant.ACTION_UPDATE_MUSIC: {
                    currentMusic = (Music) intent.getSerializableExtra(MusicService.KEY_MUSIC);
                    adapter.setChosenMusic(currentMusic);
                    adapter.notifyDataSetChanged();
                    setMusicView(currentMusic, 0);
                    break;
                }
                case Constant.ACTION_UPDATE_PROGRESS: {
                    setMusicView(currentMusic, intent.getIntExtra(MusicService.KEY_PROGRESS, 0));
                    break;
                }
                case Constant.ACTION_PLAY: {
                    play();
                    break;
                }
                case Constant.ACTION_PAUSE: {
                    pause();
                    break;
                }
                case Constant.ACTION_NEXT: {
                    next();
                    break;
                }
                case Constant.ACTION_PREVIOUS: {
                    previous();
                    break;
                }
                case Constant.ACTION_CLOSE: {
                    close();
                    break;
                }
            }
        }
    }

    private Intent getServiceIntent(String action) {
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.setAction(action);
        return intent;
    }
}