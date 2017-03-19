package com.newsapp.maximka.newsapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by maximka on 31.10.16.
 */

public class Preferences {
    private static final String PREFERENCES = "app_preferences";
    private static final String SELECTED_TRACK = "selected_track";
    private static final String TRACK_POSITION = "track_position";
    private static final String TEXT_SIZE = "text_size";
    private static final String NEWS_TITLE = "last_news";

    private SharedPreferences preferences;

    public Preferences(Application context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public int getTrackPosition() {
        return preferences.getInt(TRACK_POSITION, 0);
    }

    public int getSelectedTrack() {
        return preferences.getInt(SELECTED_TRACK, 0);
    }

    public float getTextSize() { return preferences.getFloat(TEXT_SIZE, 16); }

    public String getLastNewsTitle() {
        return preferences.getString(NEWS_TITLE, null);
    }

    public void saveTrackPosition(int position) {
        preferences.edit().putInt(TRACK_POSITION, position).apply();
    }

    public void saveSelectedTrack(int selection) {
        preferences.edit().putInt(SELECTED_TRACK, selection).apply();
    }

    public void saveTextSize(float size) {
        preferences.edit().putFloat(TEXT_SIZE, size).apply();
    }

    public void saveLastNewsTitle(String title) {
        preferences.edit().putString(NEWS_TITLE, title).apply();
    }
}
