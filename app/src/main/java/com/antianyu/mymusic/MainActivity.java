package com.antianyu.mymusic;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import classes.adapter.MusicListViewAdapter;
import classes.model.Music;
import classes.utils.AppPreference;
import classes.utils.MusicProgressDialog;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;

public class MainActivity extends AppCompatActivity
{
    // Widget
    private Toolbar toolbar;
    private MusicListViewAdapter adapter;
    private PinnedSectionListView musicListView;
    private TextView promptTextView;
    private LinearLayout indexLayout;
    private TextView centralTextView;
    private ImageView playImageView;
    private ImageView pauseImageView;
    private TextView titleTextView;
    private TextView artistTextView;
    private SeekBar progressSeekBar;
    private TextView progressTextView;
    private TextView durationTextView;
    private PopupWindow exitPopupWindow;

    // Data;
    private static String[] indexLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    private AppPreference appPreference;
    private List<Music> musicList = new ArrayList<>();
    private Music currentMusic;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(false);
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_refresh:
            {
                scanMusic(false);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onMenuOpened(int featureId, Menu menu)
    {
        showExitWindow();
        return true;
    }

    private void initView()
    {
        MusicProgressDialog.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initActionView();

        adapter = new MusicListViewAdapter(this, musicList);
        musicListView = (PinnedSectionListView) findViewById(R.id.musicListView);
        musicListView.setAdapter(adapter);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (adapter.isMusic(position))
                {
                    currentMusic = adapter.getItem(position);
                    setActionView(currentMusic, 0);
                    saveMusicStatus(currentMusic, 0);

                    adapter.setChosenPosition(position);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        promptTextView = (TextView) findViewById(R.id.promptTextView);

        indexLayout = (LinearLayout) findViewById(R.id.indexLayout);
        centralTextView = (TextView) findViewById(R.id.centralTextView);

        initIndexLayout();
        refreshPrompt();

        initExitWindow();
    }

    private void initIndexLayout()
    {
        indexLayout.removeAllViews();
        final int height = (ViewUtils.getPhoneWindowHeight() - ViewUtils.dpToPixel(110) - ViewUtils.getStatusBarHeight() - ViewUtils.getActionBarHeight()) / indexLetters.length;

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        for (String string : indexLetters)
        {
            TextView textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setTextColor(ViewUtils.getColor(R.color.text_dark_major));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            textView.setText(string);
            textView.setGravity(Gravity.CENTER);

            indexLayout.addView(textView);
            indexLayout.setOnTouchListener(new View.OnTouchListener()
            {
                public boolean onTouch(View v, MotionEvent event)
                {
                    float y = event.getY();
                    int index = (int) (y / height);
                    if (index > -1 && index < indexLetters.length)
                    {
                        String key = indexLetters[index];
                        centralTextView.setVisibility(View.VISIBLE);
                        centralTextView.setText(key);
                        if (adapter.getSelector().containsKey(key))
                        {
                            int position = adapter.getSelector().get(key);
                            musicListView.setSelection(position + musicListView.getHeaderViewsCount());
                        }
                    }
                    switch (event.getAction())
                    {
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
                }
            });
        }
    }

    private void initActionView()
    {
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        artistTextView = (TextView) findViewById(R.id.artistTextView);

        progressSeekBar = (SeekBar) findViewById(R.id.progressSeekBar);
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                progressTextView.setText(Utils.formatTime(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        progressTextView = (TextView) findViewById(R.id.progressTextView);
        durationTextView = (TextView) findViewById(R.id.durationTextView);

        ImageView previousImageView = (ImageView) findViewById(R.id.previousImageView);
        previousImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

            }
        });

        playImageView = (ImageView) findViewById(R.id.playImageView);
        playImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playImageView.setVisibility(View.INVISIBLE);
                pauseImageView.setVisibility(View.VISIBLE);
            }
        });

        pauseImageView = (ImageView) findViewById(R.id.pauseImageView);
        pauseImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                pauseImageView.setVisibility(View.INVISIBLE);
                playImageView.setVisibility(View.VISIBLE);
                saveMusicStatus(currentMusic, progressSeekBar.getProgress());
            }
        });

        ImageView nextImageView = (ImageView) findViewById(R.id.nextImageView);
        nextImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

            }
        });
    }

    private void initExitWindow()
    {
        View exitView = View.inflate(this, R.layout.window_exit, null);

        Button exitButton = (Button) exitView.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                exitPopupWindow.dismiss();
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        exitPopupWindow = ViewUtils.buildBottomPopupWindow(this, exitView);
    }

    private void showExitWindow()
    {
        if (!exitPopupWindow.isShowing())
        {
            exitPopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
            exitPopupWindow.update();

            ViewUtils.dimBackground(this);
        }
    }

    private void setActionView(Music music, int progress)
    {
        titleTextView.setText(music.getTitle());
        artistTextView.setText(music.getArtist());
        progressSeekBar.setMax(music.getDuration());
        progressSeekBar.setProgress(progress);
        progressTextView.setText(Utils.formatTime(progress));
        durationTextView.setText(Utils.formatTime(music.getDuration()));
    }

    private void refreshPrompt()
    {
        int visibility = musicList.isEmpty() ? View.VISIBLE : View.GONE;
        promptTextView.setVisibility(visibility);
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        scanMusic(true);
    }

    private void initCurrentMusic()
    {
        if (!appPreference.getCurrentMusicPath().isEmpty())
        {
            File file = new File(appPreference.getCurrentMusicPath());
            if (file.exists())
            {
                Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                           new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                                                                   MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION},
                                                           MediaStore.Audio.Media.DATA + " = '" + appPreference.getCurrentMusicPath() + "'",
                                                           null,
                                                           MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

                if(cursor != null)
                {
                    if (cursor.moveToNext())
                    {
                        currentMusic = new Music();
                        currentMusic.setTitle(getStringFromCursor(cursor, MediaStore.Audio.Media.TITLE));
                        currentMusic.setArtist(getStringFromCursor(cursor, MediaStore.Audio.Media.ARTIST));
                        currentMusic.setDuration(getIntFromCursor(cursor, MediaStore.Audio.Media.DURATION) / 1000);
                        currentMusic.setPath(getStringFromCursor(cursor, MediaStore.Audio.Media.DATA));
                    }
                    cursor.close();
                }
            }
        }
    }

    private void scanMusic(final boolean init)
    {
        musicList.clear();
        MusicProgressDialog.show();

        new Thread(new Runnable()
        {
            public void run()
            {
                Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                           new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                                                                   MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION},
                                                           MediaStore.Audio.Media.DATA + " LIKE '" + appPreference.getMusicDirectory() + "%'",
                                                           null,
                                                           MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

                if(cursor != null)
                {
                    while (cursor.moveToNext())
                    {
                        Music music = new Music();
                        music.setTitle(getStringFromCursor(cursor, MediaStore.Audio.Media.TITLE));
                        music.setArtist(getStringFromCursor(cursor, MediaStore.Audio.Media.ARTIST));
                        music.setDuration(getIntFromCursor(cursor, MediaStore.Audio.Media.DURATION) / 1000);
                        music.setPath(getStringFromCursor(cursor, MediaStore.Audio.Media.DATA));
                        musicList.add(music);
                    }
                    cursor.close();
                }

                if (init)
                {
                    initCurrentMusic();
                }

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if ((currentMusic == null || currentMusic.getPath().isEmpty()) && !musicList.isEmpty())
                        {
                            saveMusicStatus(musicList.get(0), 0);
                            setActionView(musicList.get(0), 0);
                        }
                        else if (init && currentMusic != null && !currentMusic.getPath().isEmpty())
                        {
                            setActionView(currentMusic, appPreference.getCurrentPosition());
                        }

                        adapter.setList(musicList);
                        initIndexLayout();
                        refreshPrompt();

                        toolbar.setTitle(String.format(ViewUtils.getString(R.string.title), musicList.size()));
                        MusicProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    private void saveMusicStatus(Music music, int position)
    {
        appPreference.setCurrentMusicPath(music.getPath());
        appPreference.setCurrentPosition(position);
        appPreference.saveAppPreference();
    }

    private String getStringFromCursor(Cursor cursor, String columnName)
    {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private int getIntFromCursor(Cursor cursor, String columnName)
    {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }
}