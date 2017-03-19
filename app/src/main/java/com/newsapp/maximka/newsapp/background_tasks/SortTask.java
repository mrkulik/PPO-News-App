package com.newsapp.maximka.newsapp.background_tasks;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;

import com.newsapp.maximka.newsapp.NewsApp;
import com.newsapp.maximka.newsapp.models.SortItem;
import com.newsapp.maximka.newsapp.sorting.SortingMethod;

import org.neotech.library.retainabletasks.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class SortTask extends Task<Void, double[]> {
    private Context ctx;
    private SortingMethod<SortItem> method;
    private String word;

    /**
     * Creates a new Task. This constructor must be invoked on the UI thread.
     *
     * @param tag A unique tag, which is used for retaining and identifying tasks across
     *            configuration changes. The tag needs to be unique on Activity level if you use
     *            an Activity bounded TaskManager to execute this task. If you use an Application
     *            bounded TaskManager the tag needs to be unique across the complete Application.
     */
    public SortTask(String tag, Activity activity, SortingMethod<SortItem> _method, String _word) {
        super(tag);
        ctx = activity.getApplicationContext();
        method = _method;
        word = _word;
    }

    @Override
    protected double[] doInBackground() {
        double[] elapsedTime = {0, 0};

        if (NewsApp.feed != null) {
            SortItem[] news = prepareDataToSort(elapsedTime);
            elapsedTime[1] = method.sort(news, new Comparator<SortItem>() {
                @Override
                public int compare(SortItem o1, SortItem o2) {
                    return o2.wordCount - o1.wordCount;
                }
            });
            copySortedData(news);
        }

        return elapsedTime;
    }

    private void copySortedData(SortItem[] items) {
        NewsApp.feed = new ArrayList<>(items.length);
        for (final SortItem item : items) {
            NewsApp.feed.add(item.news);
        }
    }

    private SortItem[] prepareDataToSort(double[] elapsedTime) {
        long start = SystemClock.elapsedRealtimeNanos();
        List<SortItem> list = new NewsContentLoader(ctx, null)
                .getItemsForSort(NewsApp.feed, word);
        SortItem[] news = list.toArray(new SortItem[list.size()]);
        elapsedTime[0] = (SystemClock.elapsedRealtimeNanos() - start) / 1000 / 1000.0;
        return news;
    }
}
