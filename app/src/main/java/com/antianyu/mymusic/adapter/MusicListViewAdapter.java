package com.antianyu.mymusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antianyu.mymusic.R;
import com.antianyu.mymusic.model.Music;
import com.antianyu.mymusic.utils.MusicUtils;
import com.antianyu.mymusic.utils.Utils;
import com.antianyu.mymusic.utils.ViewUtils;
import com.antianyu.mymusic.widget.PinnedSectionListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

public class MusicListViewAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_MUSIC = 1;

    private LayoutInflater layoutInflater;
    private List<Music> musicList;
    private HashMap<String, Integer> selector = new HashMap<>();
    private ArrayList<Integer> indexList = new ArrayList<>();

    @Getter @Setter private int chosenPosition = -1;
    private int chosenMajorColor;
    private int chosenMinorColor;
    private int majorColor;
    private int minorColor;

    public MusicListViewAdapter(Context context, List<Music> musics) {
        this.layoutInflater = LayoutInflater.from(context);
        this.musicList = new ArrayList<>(musics);
        this.chosenMajorColor = ViewUtils.getColor(R.color.major_dark);
        this.chosenMinorColor = ViewUtils.getColor(R.color.minor_dark);
        this.majorColor = ViewUtils.getColor(R.color.text_dark_major);
        this.minorColor = ViewUtils.getColor(R.color.text_dark_minor);
        initData();
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == VIEW_TYPE_HEADER;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Music music = (Music) getItem(position);
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            HeaderViewHolder viewHolder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_header, parent, false);

                viewHolder = new HeaderViewHolder(convertView);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (HeaderViewHolder) convertView.getTag();
            }

            viewHolder.headerTextView.setText(music.getTitle());
        } else {
            MusicViewHolder viewHolder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_music, parent, false);

                viewHolder = new MusicViewHolder(convertView);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MusicViewHolder) convertView.getTag();
            }

            int majorTextColor = position == chosenPosition ? chosenMajorColor : majorColor;
            int minorTextColor = position == chosenPosition ? chosenMinorColor : minorColor;

            viewHolder.nameTextView.setText(music.getTitle());
            viewHolder.nameTextView.setTextColor(majorTextColor);

            viewHolder.artistTextView.setText(music.getArtist());
            viewHolder.artistTextView.setTextColor(minorTextColor);

            viewHolder.durationTextView.setText(Utils.formatTime(music.getDuration()));
            viewHolder.durationTextView.setTextColor(majorTextColor);
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return indexList.contains(position) ? VIEW_TYPE_HEADER : VIEW_TYPE_MUSIC;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void setList(List<Music> musics) {
        musicList.clear();
        musicList.addAll(musics);
        initData();
    }

    public void setChosenMusic(Music music) {
        this.chosenPosition = musicList.indexOf(music);
    }

    public boolean isMusic(int position) {
        return !indexList.contains(position);
    }

    public int getPosition(String key) {
        Integer position = selector.get(key);
        return position == null ? -1 : position;
    }

    private void initData() {
        TreeMap<String, ArrayList<Music>> indexMap = MusicUtils.getIndexMap(musicList);

        int count = 0;
        selector.clear();
        indexList.clear();
        musicList.clear();
        for (Map.Entry<String, ArrayList<Music>> entry : indexMap.entrySet()) {
            Music music = new Music();
            music.setTitle(entry.getKey());

            selector.put(entry.getKey(), count);
            indexList.add(count);
            musicList.add(music);
            musicList.addAll(entry.getValue());
            count += entry.getValue().size() + 1;
        }
    }

    public static class HeaderViewHolder {

        @BindView(R.id.headerTextView) TextView headerTextView;

        HeaderViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }

    public static class MusicViewHolder {

        @BindView(R.id.nameTextView) TextView nameTextView;
        @BindView(R.id.artistTextView) TextView artistTextView;
        @BindView(R.id.durationTextView) TextView durationTextView;

        MusicViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}