package com.antianyu.mymusic;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.antianyu.mymusic.adapter.MusicListViewAdapter;
import com.antianyu.mymusic.model.Music;
import com.antianyu.mymusic.utils.ActionListener;
import com.antianyu.mymusic.utils.AppPreference;
import com.antianyu.mymusic.utils.Constant;
import com.antianyu.mymusic.utils.MusicProgressDialog;
import com.antianyu.mymusic.utils.MusicService;
import com.antianyu.mymusic.utils.MusicUtils;
import com.antianyu.mymusic.utils.NotificationUtils;
import com.antianyu.mymusic.utils.ServiceLauncher;
import com.antianyu.mymusic.utils.Utils;
import com.antianyu.mymusic.utils.ViewUtils;
import com.antianyu.mymusic.widget.PinnedSectionListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

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

    private long exitTime;
    private NotificationManager manager;
    private Notification notification;
    private ActionBroadcastReceiver receiver;

    private MusicListViewAdapter adapter;
    private MusicProgressDialog progressDialog;

    private AppPreference appPreference = AppPreference.getAppPreference();
    private List<Music> musicList = new ArrayList<>();
    private Music currentMusic;
    private Music chosenMusic;

    // View
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        stopService(new Intent(this, MusicService.class));
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                ViewUtils.showToast(R.string.press_back_to_exit);
                exitTime = System.currentTimeMillis();
            } else {
                close();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    private void initView() {
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        initActionView();

        adapter = new MusicListViewAdapter(this, musicList);
        musicListView.setAdapter(adapter);
        musicListView.setOnItemClickListener((parent, view, position, id) -> {
            if (adapter.isMusic(position)) {
                currentMusic = (Music) adapter.getItem(position);
                setMusicView(currentMusic, 0);

                int progress = progressSeekBar.getProgress();
                ServiceLauncher.launch(MainActivity.this, Constant.ACTION_UPDATE_MUSIC, currentMusic, progress);
                startPlaying();

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

        notification = NotificationUtils.buildNotification(this);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        initIndexLayout();
        refreshPrompt();
    }

    private void initIndexLayout() {
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

                ServiceLauncher.launch(MainActivity.this, Constant.ACTION_UPDATE_PROGRESS, seekBar.getProgress());

                startPlaying();
            }
        });
        findViewById(R.id.previousImageView).setOnClickListener(v -> previous());
        playImageView.setOnClickListener(v -> play());
        pauseImageView.setOnClickListener(v -> pause());
        findViewById(R.id.nextImageView).setOnClickListener(v -> next());
    }

    private void showDeleteWindow() {
        new AlertDialog.Builder(this)
            .setMessage(ViewUtils.getString(R.string.delete_confirm, chosenMusic.getTitle()))
            .setPositiveButton(R.string.confirm, (dialog, which) -> {
                File file = new File(chosenMusic.getPath());
                if (file.delete()) {
                    ViewUtils.showToast(R.string.succeed_in_deleting);
                    if (chosenMusic.equals(currentMusic)) {
                        currentMusic = MusicUtils.findNextMusic(musicList, currentMusic);
                        choseMusic();
                    }
                    musicList.remove(chosenMusic);
                    refreshView();

                    ServiceLauncher.launch(MainActivity.this, Constant.ACTION_UPDATE_MUSIC_LIST, musicList);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
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

        updateNotification();
    }

    private void setListViewSelection() {
        if (adapter.getChosenPosition() <= musicListView.getFirstVisiblePosition() ||
            adapter.getChosenPosition() >= musicListView.getLastVisiblePosition()) {
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
        toolbar.setTitle(ViewUtils.getString(R.string.title, musicList.size()));
        setListViewSelection();
    }

    private void refreshPrompt() {
        int visibility = musicList.isEmpty() ? View.VISIBLE : View.GONE;
        promptTextView.setVisibility(visibility);
    }

    private void updateNotification() {
        if (manager == null) {
            return;
        }

        if (musicList.isEmpty()) {
            manager.cancel(Constant.NOTIFICATION_ID);
        } else {
            manager.notify(Constant.NOTIFICATION_ID, notification);
        }
    }

    // Data
    private void initData() {
        receiver = new ActionBroadcastReceiver(new ActionListener() {
            @Override
            public void onMusicUpdate(Music music) {
                currentMusic = music;
                adapter.setChosenMusic(currentMusic);
                adapter.notifyDataSetChanged();
                setMusicView(currentMusic, 0);
            }

            @Override
            public void onProgressUpdate(int progress) {
                setMusicView(currentMusic, progress);
            }

            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onNext() {
                next();
            }

            @Override
            public void onPrevious() {
                previous();
            }

            @Override
            public void onClose() {
                close();
            }
        });
        registerReceiver(receiver, ActionBroadcastReceiver.getIntentFilter());
        scanMusic();
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

                updateNotification();

                refreshView();
                progressDialog.dismiss();

                int progress = progressSeekBar.getProgress();
                ServiceLauncher.launch(MainActivity.this, Constant.ACTION_CREATE, currentMusic, progress, musicList);
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
            ServiceLauncher.launch(this, Constant.ACTION_PLAY);
            startPlaying();
        }
    }

    private void pause() {
        stopPlaying();
        saveMusicStatus(currentMusic, progressSeekBar.getProgress());
    }

    private void next() {
        currentMusic = MusicUtils.findNextMusic(musicList, currentMusic);
        adapter.setChosenMusic(currentMusic);
        adapter.notifyDataSetChanged();
        choseMusic();
    }

    private void previous() {
        currentMusic = MusicUtils.findPreviousMusic(musicList, currentMusic);
        adapter.setChosenMusic(currentMusic);
        adapter.notifyDataSetChanged();
        choseMusic();
    }

    private void close() {
        if (manager != null) {
            manager.cancel(Constant.NOTIFICATION_ID);
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void startPlaying() {
        playImageView.setVisibility(View.INVISIBLE);
        pauseImageView.setVisibility(View.VISIBLE);
        notification.contentView.setViewVisibility(R.id.playImageView, View.INVISIBLE);
        notification.contentView.setViewVisibility(R.id.pauseImageView, View.VISIBLE);
        updateNotification();
    }

    private void stopPlaying() {
        pauseImageView.setVisibility(View.INVISIBLE);
        playImageView.setVisibility(View.VISIBLE);
        notification.contentView.setViewVisibility(R.id.pauseImageView, View.INVISIBLE);
        notification.contentView.setViewVisibility(R.id.playImageView, View.VISIBLE);
        updateNotification();
        ServiceLauncher.launch(MainActivity.this, Constant.ACTION_PAUSE);
    }

    private void choseMusic() {
        setListViewSelection();
        setMusicView(currentMusic, 0);
        ServiceLauncher.launch(this, Constant.ACTION_UPDATE_MUSIC, currentMusic, 0);
        startPlaying();
    }
}