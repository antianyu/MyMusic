package com.antianyu.mymusic;

import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.adapter.MusicListViewAdapter;
import classes.model.Music;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.MusicProgressDialog;
import classes.utils.MusicService;
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
    private PopupWindow deletePopupWindow;

    // Data;
    private static String[] indexLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    private AppPreference appPreference;
    private List<Music> musicList = new ArrayList<>();
    private Music currentMusic;
    private Music chosenMusic;
    private MusicService musicService;
    private ServiceConnection connection;

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
                scanMusic();
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
                if (adapter.isMusic(position) && (deletePopupWindow == null || !deletePopupWindow.isShowing()))
                {
                    currentMusic = adapter.getItem(position);
                    setActionView(currentMusic, 0);
                    Intent intent = getServiceIntent(Constant.ACTION_CHANGE_MUSIC);
                    intent.putExtra("music", currentMusic);
                    intent.putExtra("progress", progressSeekBar.getProgress());
                    startPlaying(intent);

                    adapter.setChosenPosition(position);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        musicListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (adapter.isMusic(position))
                {
                    chosenMusic = adapter.getItem(position);
                    showDeleteWindow();
                }
                return false;
            }
        });

        promptTextView = (TextView) findViewById(R.id.promptTextView);

        indexLayout = (LinearLayout) findViewById(R.id.indexLayout);
        centralTextView = (TextView) findViewById(R.id.centralTextView);

        initIndexLayout();
        refreshPrompt();
        initExitWindow();
        initDeleteWindow();
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
                            musicListView.setSelection(position + musicListView.getHeaderViewsCount(), true);
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
                appPreference.setProgress(seekBar.getProgress());
                appPreference.saveAppPreference();

                if (musicService != null)
                {
                    Intent intent = getServiceIntent(Constant.ACTION_CHANGE_PROGRESS);
                    intent.putExtra("progress", seekBar.getProgress());
                    startService(intent);
                }
            }
        });

        progressTextView = (TextView) findViewById(R.id.progressTextView);
        durationTextView = (TextView) findViewById(R.id.durationTextView);

        ImageView previousImageView = (ImageView) findViewById(R.id.previousImageView);
        previousImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                currentMusic = previousMusic();
                adapter.setChosenPosition(currentMusic);
                adapter.notifyDataSetChanged();
                choseMusic();
            }
        });

        playImageView = (ImageView) findViewById(R.id.playImageView);
        playImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (Music.exists(currentMusic))
                {
                    startPlaying(getServiceIntent(Constant.ACTION_PLAY));
                }
            }
        });

        pauseImageView = (ImageView) findViewById(R.id.pauseImageView);
        pauseImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                stopPlaying();
                saveMusicStatus(currentMusic, progressSeekBar.getProgress());
            }
        });

        ImageView nextImageView = (ImageView) findViewById(R.id.nextImageView);
        nextImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                currentMusic = nextMusic();
                adapter.setChosenPosition(currentMusic);
                adapter.notifyDataSetChanged();
                choseMusic();
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

        Button cancelButton = (Button) exitView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                exitPopupWindow.dismiss();
            }
        });

        exitPopupWindow = ViewUtils.buildBottomPopupWindow(this, exitView);
    }

    private void initDeleteWindow()
    {
        View deleteView = View.inflate(this, R.layout.window_delete, null);

        Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                deletePopupWindow.dismiss();
                Builder builder = new Builder(MainActivity.this);
                builder.setTitle(R.string.warning);
                builder.setMessage(String.format(ViewUtils.getString(R.string.delete_confirm), chosenMusic.getTitle()));
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        File file = new File(chosenMusic.getPath());
                        if (file.delete())
                        {
                            ViewUtils.showToast(MainActivity.this, R.string.succeed_in_deleting);
                            if (chosenMusic.equals(currentMusic))
                            {
                                currentMusic = musicList.size() > 1 ? nextMusic() : new Music();
                                choseMusic();
                            }
                            musicList.remove(chosenMusic);
                            refreshView();

                            Intent intent = getServiceIntent(Constant.ACTION_CHANGE_MUSIC_LIST);
                            intent.putExtra("musicList", (Serializable) musicList);
                            startService(intent);
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();
            }
        });

        Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                deletePopupWindow.dismiss();
            }
        });

        deletePopupWindow = ViewUtils.buildBottomPopupWindow(this, deleteView);
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

    private void showDeleteWindow()
    {
        if (!deletePopupWindow.isShowing())
        {
            deletePopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
            deletePopupWindow.update();

            ViewUtils.dimBackground(this);
        }
    }

    private void setActionView(Music music, int progress)
    {
        saveMusicStatus(music, progress);
        titleTextView.setText(music.getTitle());
        artistTextView.setText(music.getArtist());
        progressSeekBar.setMax(music.getDuration());
        progressSeekBar.setProgress(progress);
        progressTextView.setText(Utils.formatTime(progress));
        durationTextView.setText(Utils.formatTime(music.getDuration()));
    }

    private void refreshView()
    {
        if (!Music.exists(currentMusic))
        {
            stopPlaying();
        }
        adapter.setList(musicList);
        adapter.setChosenPosition(currentMusic);
        adapter.notifyDataSetChanged();
        initIndexLayout();
        refreshPrompt();
        toolbar.setTitle(String.format(ViewUtils.getString(R.string.title), musicList.size()));
        setListViewSelection();
    }

    private void refreshPrompt()
    {
        int visibility = musicList.isEmpty() ? View.VISIBLE : View.GONE;
        promptTextView.setVisibility(visibility);
    }

    private void setListViewSelection()
    {
        if (adapter.getChosenPosition() <= musicListView.getFirstVisiblePosition() || adapter.getChosenPosition() >= musicListView.getLastVisiblePosition())
        {
            musicListView.setSelection(adapter.getChosenPosition(), false);
        }
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        scanMusic();
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
                                                           MediaStore.Audio.Media.DATA + " = ?",
                                                           new String[]{appPreference.getCurrentMusicPath()},
                                                           null);

                if(cursor != null)
                {
                    if (cursor.moveToNext())
                    {
                        currentMusic = getMusicFromCursor(cursor);
                    }
                    cursor.close();
                }
            }
        }
    }

    private void scanMusic()
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
                                                           MediaStore.Audio.Media.DATA + " LIKE ?",
                                                           new String[]{appPreference.getMusicDirectory() + "%"},
                                                           null);

                if(cursor != null)
                {
                    while (cursor.moveToNext())
                    {
                        musicList.add(getMusicFromCursor(cursor));
                    }
                    cursor.close();
                }

                initCurrentMusic();
                Music.sortByTitle(musicList);

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (!Music.exists(currentMusic) && !musicList.isEmpty())
                        {
                            currentMusic = musicList.get(0);
                            setActionView(currentMusic, 0);
                        }
                        else if (Music.exists(currentMusic))
                        {
                            setActionView(currentMusic, appPreference.getProgress());
                        }
                        else
                        {
                            currentMusic = new Music();
                            setActionView(currentMusic, 0);
                        }

                        refreshView();
                        MusicProgressDialog.dismiss();

                        // connect to MusicService
                        if (connection != null)
                        {
                            unbindService(connection);
                            connection = null;
                        }

                        connection = new ServiceConnection()
                        {
                            public void onServiceConnected(ComponentName name, IBinder service)
                            {
                                musicService = ((MusicService.MusicBinder) service).getServices();
                            }

                            public void onServiceDisconnected(ComponentName name)
                            {
                                musicService = null;
                            }
                        };

                        Intent bindIntent = new Intent(MainActivity.this, MusicService.class);
                        bindIntent.putExtra("musicList", (Serializable) musicList);
                        bindIntent.putExtra("music", currentMusic);
                        bindIntent.putExtra("progress", progressSeekBar.getProgress());
                        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
                    }
                });
            }
        }).start();
    }

    private void saveMusicStatus(Music music, int position)
    {
        appPreference.setCurrentMusicPath(music.getPath());
        appPreference.setProgress(position);
        appPreference.saveAppPreference();
    }

    // Music
    private void startPlaying(Intent intent)
    {
        playImageView.setVisibility(View.INVISIBLE);
        pauseImageView.setVisibility(View.VISIBLE);
        startService(intent);
    }

    private void stopPlaying()
    {
        pauseImageView.setVisibility(View.INVISIBLE);
        playImageView.setVisibility(View.VISIBLE);
        startService(getServiceIntent(Constant.ACTION_PAUSE));
    }

    public Music previousMusic()
    {
        if (musicList.isEmpty())
        {
            return new Music();
        }
        else
        {
            int chosenPosition = musicList.indexOf(currentMusic);
            chosenPosition--;
            if (chosenPosition < 0)
            {
                chosenPosition += musicList.size();
            }
            return musicList.get(chosenPosition);
        }
    }

    public Music nextMusic()
    {
        if (musicList.isEmpty())
        {
            return new Music();
        }
        else
        {
            int chosenPosition = musicList.indexOf(currentMusic);
            chosenPosition++;
            chosenPosition = chosenPosition % musicList.size();
            return musicList.get(chosenPosition);
        }
    }

    private void choseMusic()
    {
        setListViewSelection();
        setActionView(currentMusic, 0);
        Intent intent = getServiceIntent(Constant.ACTION_CHANGE_MUSIC);
        intent.putExtra("music", currentMusic);
        intent.putExtra("progress", progressSeekBar.getProgress());
        startPlaying(intent);
    }

    // Auxiliaries
    private Intent getServiceIntent(String action)
    {
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.setAction(action);
        return intent;
    }

    private Music getMusicFromCursor(Cursor cursor)
    {
        Music music = new Music();
        music.setTitle(getStringFromCursor(cursor, MediaStore.Audio.Media.TITLE));
        music.setArtist(getStringFromCursor(cursor, MediaStore.Audio.Media.ARTIST));
        music.setDuration(getIntFromCursor(cursor, MediaStore.Audio.Media.DURATION) / 1000);
        music.setPath(getStringFromCursor(cursor, MediaStore.Audio.Media.DATA));
        return music;
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