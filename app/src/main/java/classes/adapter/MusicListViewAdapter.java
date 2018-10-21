package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antianyu.mymusic.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import classes.model.Music;
import classes.utils.CharacterParser;
import classes.utils.MusicUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;
import lombok.Getter;
import lombok.Setter;

public class MusicListViewAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_MUSIC = 1;

    private LayoutInflater layoutInflater;
    private List<Music> musicList;
    @Getter private HashMap<String, Integer> selector = new HashMap<>();
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

    public View getView(int position, View convertView, ViewGroup parent) {
        Music music = getItem(position);
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
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
                viewHolder.artistTextView = (TextView) convertView.findViewById(R.id.artistTextView);
                viewHolder.durationTextView = (TextView) convertView.findViewById(R.id.durationTextView);

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

    public int getCount() {
        return musicList.size();
    }

    public Music getItem(int position) {
        return musicList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean isItemViewTypePinned(int viewType) {
        return viewType == VIEW_TYPE_HEADER;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int position) {
        return indexList.contains(position) ? VIEW_TYPE_HEADER : VIEW_TYPE_MUSIC;
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

    private void initData() {
        TreeMap<String, ArrayList<Music>> indexMap = MusicUtils.getIndexMap();

        for (Music music : musicList) {
            String initLetter = CharacterParser.getInitLetter(music.getTitle());
            ArrayList<Music> letterList = indexMap.get(initLetter);
            if (letterList == null) {
                letterList = new ArrayList<>();
            }
            letterList.add(music);
            indexMap.put(initLetter, letterList);
        }

        int count = 0;
        selector.clear();
        indexList.clear();
        musicList.clear();
        for (Map.Entry<String, ArrayList<Music>> entry : indexMap.entrySet()) {
            String key = entry.getKey();
            Music music = new Music();
            music.setTitle(key);

            ArrayList<Music> values = entry.getValue();
            selector.put(key, count);
            indexList.add(count);
            musicList.add(music);
            musicList.addAll(values);
            count += values.size() + 1;
        }
    }

    private static class HeaderViewHolder {

        private TextView headerTextView;

        HeaderViewHolder(View convertView) {
            headerTextView = (TextView) convertView.findViewById(R.id.headerTextView);
        }
    }

    private static class MusicViewHolder {

        TextView nameTextView;
        TextView artistTextView;
        TextView durationTextView;

        MusicViewHolder(View convertView) {
            nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            artistTextView = (TextView) convertView.findViewById(R.id.artistTextView);
            durationTextView = (TextView) convertView.findViewById(R.id.durationTextView);
        }
    }
}