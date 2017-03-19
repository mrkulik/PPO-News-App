package com.newsapp.maximka.newsapp;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.newsapp.maximka.newsapp.datetimeformatter.DateTimeFormatter;
import com.newsapp.maximka.newsapp.models.DaoMaster;
import com.newsapp.maximka.newsapp.models.DaoSession;
import com.newsapp.maximka.newsapp.models.DatabaseHelper;
import com.newsapp.maximka.newsapp.models.NewsFeed;
import com.newsapp.maximka.newsapp.track_covers.FromPicassoToFile;
import com.newsapp.maximka.newsapp.track_covers.LastFmService;

import org.greenrobot.greendao.async.AsyncSession;

import java.io.File;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import teaspoon.TeaSpoon;


public class NewsApp extends Application {
    public static Preferences preferences;
    public static List<NewsFeed> feed;
    public static DateTimeFormatter dateTimeFormatter = new DateTimeFormatter();
    public static LastFmService lastFmService;

    private DaoSession daoSession;
    private AsyncSession daoAsyncSession;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DatabaseHelper(this, "newsapp-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        daoSession = new DaoMaster(db).newSession();
        daoAsyncSession = daoSession.startAsyncSession();

        preferences = new Preferences(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ws.audioscrobbler.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        lastFmService = retrofit.create(LastFmService.class);

        createCoversFolder();

        TeaSpoon.initialize();
    }

    public DaoSession getSession() {
        return daoSession;
    }

    public AsyncSession getAsyncSession() {
        return daoAsyncSession;
    }

    private void createCoversFolder() {
        File folder = new File(getFilesDir(), FromPicassoToFile.COVERS_FOLDER);
        folder.mkdirs();
    }
}
