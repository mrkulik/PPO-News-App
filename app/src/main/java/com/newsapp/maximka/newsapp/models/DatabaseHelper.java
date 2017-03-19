package com.newsapp.maximka.newsapp.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends DaoMaster.DevOpenHelper {
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);

        List<NewsCategory> categories = new ArrayList<NewsCategory>() {{
            add(new NewsCategory("Новости", "http://news.tut.by/rss/index.rss", false));
            add(new NewsCategory("Экономика", "http://news.tut.by/rss/economics.rss", false));
            add(new NewsCategory("Общество", "http://news.tut.by/rss/society.rss", false));
            add(new NewsCategory("В мире", "http://news.tut.by/rss/world.rss", false));
            add(new NewsCategory("Культура", "http://news.tut.by/rss/culture.rss", false));
            add(new NewsCategory("Происшествия", "http://news.tut.by/rss/accidents.rss", false));
            add(new NewsCategory("Финансы", "http://news.tut.by/rss/finance.rss", false));
            add(new NewsCategory("Недвижимость", "http://news.tut.by/rss/realty.rss", false));
            add(new NewsCategory("Авто", "http://news.tut.by/rss/auto.rss", false));
            add(new NewsCategory("Спорт", "http://news.tut.by/rss/sport.rss", false));
            add(new NewsCategory("Леди", "http://news.tut.by/rss/lady.rss", false));
            add(new NewsCategory("42", "http://news.tut.by/rss/it.rss", false));
            add(new NewsCategory("Афиша", "http://news.tut.by/rss/afisha.rss", false));
            add(new NewsCategory("Новости компаний", "http://news.tut.by/rss/press.rss", false));
        }};

        int id = 0;
        for (NewsCategory category : categories) {
            db.execSQL("insert into Categories values " + categoryToStringTouple(category, ++id));
        }
    }

    private String categoryToStringTouple(NewsCategory category, int id) {
        return String.format("(%d, '%s', '%s', %d)",
                id, category.getName(), category.getUrl(), category.getIsHidden() ? 1 : 0);
    }
}
