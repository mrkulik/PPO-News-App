package com.newsapp.maximka.newsapp.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.newsapp.maximka.newsapp.R;
import com.newsapp.maximka.newsapp.models.Track;
import com.newsapp.maximka.newsapp.music_player.MusicService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MusicAdapter extends BaseAdapter implements MusicService.OnStateChangedListener {
    private Context context;
    private LayoutInflater inflater;
    private List<Track> tracks;
    private int selectedTrack;
    private boolean isPlaying = false;

    public MusicAdapter(Context ctx, MusicService service) {
        context = ctx;
        this.tracks = service.getTracks();
        this.selectedTrack = service.getSelectedTrack();
        isPlaying = service.isPlaying();
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onSelectTrack(int newTrackPosition) {
        selectedTrack = newTrackPosition;
        notifyDataSetChanged();
    }

    @Override
    public void onPlayingStateChanged(boolean isPlaying) {
        boolean oldState = this.isPlaying;
        this.isPlaying = isPlaying;
        if (oldState != isPlaying) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tracks.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.music_item, parent, false);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = ViewHolder.initHolderAndBind(convertView);
        }

        Track track = tracks.get(position);
        int textStyle = Typeface.NORMAL;
        int viewBackground = R.color.defaultBackground;
        if (position == selectedTrack) {
            textStyle = Typeface.BOLD;
            viewBackground = R.color.colorPrimaryLight;
            int cover = R.drawable.ic_play_circle_filled_white_48dp;
            if (isPlaying) {
                cover = R.drawable.ic_pause_circle_filled_white_48dp;
            }
            holder.trackCover.setImageResource(cover);
        } else {
            String path = track.getCoverFilePath();
            Picasso.with(context)
                    .load(path == null ? null : new File(path))
                    .placeholder(R.drawable.ic_album_white_48dp)
                    .into(holder.trackCover);
        }

        holder.background.setBackgroundResource(viewBackground);
        holder.trackName.setText(track.getTitle());
        holder.trackArtist.setText(track.getArtist());
        holder.trackName.setTypeface(null, textStyle);
        holder.trackDuration.setText(formatDuration(track.getDuration()));

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.soundName) TextView trackName;
        @BindView(R.id.soundDuration) TextView trackDuration;
        @BindView(R.id.trackImage) ImageView trackCover;
        @BindView(R.id.trackBackground) View background;
        @BindView(R.id.soundArtist) TextView trackArtist;

        static ViewHolder initHolderAndBind(View view) {
            ViewHolder holder = new ViewHolder();
            ButterKnife.bind(holder, view);
            view.setTag(holder);
            holder.trackCover.setColorFilter(R.color.colorAccent, PorterDuff.Mode.DST_OVER);
            return holder;
        }
    }

    private String formatDuration(long time) {
        time /= 1000;
        return String.format("%d:%02d", time / 60, time % 60);
    }
}
