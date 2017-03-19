package com.newsapp.maximka.newsapp.models;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.newsapp.maximka.newsapp.track_covers.FromPicassoToFile;
import com.newsapp.maximka.newsapp.track_covers.LastFmService;
import com.newsapp.maximka.newsapp.track_covers.ResponseRoot;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.async.AsyncSession;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by maximka on 30.10.16.
 */

public class Track {
    private static int idIndex;
    private static int durationIndex;
    private static int dataIndex;
    private static int titleIndex;
    private static int artistIndex;

    private long duration;
    private Long id;
    private String data;
    private String rawTitle;
    private String artist;
    private String coverFilePath;
    private String title;
    private boolean isParsed = false;

    public static void setColumnIndices(Cursor cursor) {
        idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
    }

    public Track(Cursor cursor) {
        id = cursor.getLong(idIndex);
        duration = cursor.getLong(durationIndex);
        data = cursor.getString(dataIndex);
        rawTitle = cursor.getString(titleIndex);
        artist = cursor.getString(artistIndex);
    }

    public Track loadCoverFilePath(LastFmService service, final AsyncSession session,
                                   final Context context, HashMap<Long, String> existedCovers) {
        if (existedCovers.containsKey(id)) {
            coverFilePath = existedCovers.get(id);
        } else {
            service.getTrackInfo(getArtist(), getTitle()).enqueue(new Callback<ResponseRoot>() {
                @Override
                public void onResponse(Call<ResponseRoot> call, Response<ResponseRoot> response) {
                    try {
                        String url = response.body()
                                .getTrack()
                                .getAlbum()
                                .getLargestCover()
                                .getUrl();
                        if (!FromPicassoToFile.isUrlLoaded(url)) {
                            Picasso.with(context)
                                    .load(url)
                                    .into(new FromPicassoToFile(context, url));
                        }
                        coverFilePath = FromPicassoToFile.getFilePath(context, url);
                    } catch (NullPointerException e) {
                        Log.e("Retrofit", "invalid track data");
                    } catch (Exception e) {
                        Log.e("Retrofit", getTitle() + " " + getArtist(), e);
                    } finally {
                        session.insert(new TrackCover(id, coverFilePath));
                    }
                }

                @Override
                public void onFailure(Call<ResponseRoot> call, Throwable t) {
                    Log.e("Retrofit", "", t);
                }
            });
        }

        return this;
    }

    public long getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public String getData() {
        return data;
    }

    public String getCoverFilePath() {
        return coverFilePath;
    }

    public String getArtist() {
        if (artist.equals("<unknown>") && !isParsed) {
            parseRawTitle();
        }
        return artist;
    }

    public String getTitle() {
        if (title == null && !isParsed) {
            parseRawTitle();
        }
        return title;
    }

    private void parseRawTitle() {
        String target = rawTitle.replace('_', ' ').replaceAll("\\(.*?\\)", "").trim();
        String[] artistAndTitle = target.split(" - ");
        if (artistAndTitle.length == 1) {
            title = artistAndTitle[0];
        } else if (artistAndTitle.length == 2) {
            artist = artistAndTitle[0];
            title = artistAndTitle[1];
        }
        if (artist.equals("<unknown>")) {
            artist = "unknown";
        }
        isParsed = true;
    }
}
