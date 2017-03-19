package com.newsapp.maximka.newsapp.background_tasks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.newsapp.maximka.newsapp.models.NewsFeed;
import com.newsapp.maximka.newsapp.models.SortItem;
import com.vincentbrison.openlibraries.android.dualcache.Builder;
import com.vincentbrison.openlibraries.android.dualcache.DualCache;
import com.vincentbrison.openlibraries.android.dualcache.JsonSerializer;
import com.vincentbrison.openlibraries.android.dualcache.SizeOf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NewsContentLoader extends AsyncTaskLoader<String> {
    public static final int MAX_RAM_SIZE_BYTES = 4 * 1024 * 1024;
    public static final int MAX_DISK_SIZE_BYTES = 8 * 1024 * 1024;
    private static final String CACHE_ID = "ARTICLE_TEXT";
    private static final int CACHE_VERSION = 1;
    public static final int EXECUTOR_TIMEOUT = 5;

    private static DualCache<String> mCache = null;
    private String newsUrl;

    public NewsContentLoader(Context context, Bundle args) {
        super(context);

        if (mCache == null) {
            mCache = new Builder<>(CACHE_ID, CACHE_VERSION, String.class)
                    .useReferenceInRam(MAX_RAM_SIZE_BYTES, new SizeOf<String>() {
                        @Override
                        public int sizeOf(String object) {
                            return object.length() * 2 + 16;
                        }
                    })
                    .useSerializerInDisk(MAX_DISK_SIZE_BYTES, true,
                            new JsonSerializer<>(String.class), context)
                    .build();
        }
        if (args != null) {
            newsUrl = args.getString("url");
        }
    }

    @Override
    public String loadInBackground() {
        return getNewsContent(newsUrl);
    }

    public List<SortItem> getItemsForSort(List<NewsFeed> news, String searchWord) {
        final List<SortItem> newsContent = Collections.synchronizedList(new ArrayList<SortItem>());
        ExecutorService executor = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        final String regex = "(?i)" + searchWord;

        try {
            for (final NewsFeed item : news) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        SortItem touple = new SortItem(item, getNewsContent(item.getLink()), 0);
                        countMatches(touple, regex);
                        newsContent.add(touple);
                    }
                });
            }
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return  newsContent;
    }

    private void countMatches(SortItem item, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(item.text);
        for (item.wordCount = 0; matcher.find(); ++item.wordCount);
    }

    private String parsePage(Document document) {
        StringBuilder builder = new StringBuilder();

        Elements paragraphs = document.select("div#article_body")
                .first()
                .select("p");
        for (Element paragraph : paragraphs) {
            String text = paragraph.text();
            if (!text.isEmpty()) {
                builder.append(text);
                builder.append("\n\n");
            }
        }

        return builder.toString();
    }

    private String getNewsContent(String url) {
        String newsText = mCache.get(String.valueOf(url.hashCode()));
        if (newsText == null) {
            try {
                newsText = parsePage(Jsoup.connect(url).get());
                mCache.put(String.valueOf(url.hashCode()), newsText);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return newsText;
    }
}
