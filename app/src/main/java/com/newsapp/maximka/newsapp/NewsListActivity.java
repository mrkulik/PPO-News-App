package com.newsapp.maximka.newsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crazyhitty.chdev.ks.rssmanager.OnRssLoadListener;
import com.crazyhitty.chdev.ks.rssmanager.RssItem;
import com.crazyhitty.chdev.ks.rssmanager.RssReader;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.newsapp.maximka.newsapp.adapters.NewsAdapter;
import com.newsapp.maximka.newsapp.background_tasks.DbUpdaterFromRss;
import com.newsapp.maximka.newsapp.background_tasks.SortTask;
import com.newsapp.maximka.newsapp.models.NewsCategory;
import com.newsapp.maximka.newsapp.models.NewsFeed;
import com.newsapp.maximka.newsapp.models.NewsFeedDao;
import com.newsapp.maximka.newsapp.sorting.SortingMethodFactory;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.providers.TaskActivityCompat;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NewsListActivity extends TaskActivityCompat implements
        OnRssLoadListener, Task.Callback, AsyncOperationListener, ObservableScrollViewCallbacks {
    static final String SORT_TAG = "Sort tag";
    static final String UPDATE_FEED_TAG = "Update feed tag";
    static final String LOG_TAG = "News list activity";

    @BindView(R.id.newsFeedLoadingProgress) ProgressBar loadingBar;
    @BindView(R.id.sortProgress) ProgressBar sortBar;
    @BindView(R.id.newsListView) ObservableRecyclerView newsFeed;
    private NewsAdapter adapter;
    private String categoryTitle;
    private long categoryId;
    private int daoSequenceNumber;

    static class DialogResultsParser {
        @BindView(R.id.sortWord) TextView word;
        @BindView(R.id.methodRadioGroup) RadioGroup radio;

        public DialogResultsParser(View view) {
            ButterKnife.bind(this, view);
        }

        public String getWord() {
            return word.getText().toString();
        }

        public int getMethodId() {
            return radio.getCheckedRadioButtonId();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        ButterKnife.bind(this);
        extractParameters(savedInstanceState);

        ((NewsApp) getApplication()).getAsyncSession().setListenerMainThread(this);

        if (savedInstanceState == null || NewsApp.feed == null) {
            NewsApp.feed = null;
            categoryId = getIntent().getLongExtra("category_id", -1);
            NewsCategory category = ((NewsApp) getApplication())
                    .getSession()
                    .getNewsCategoryDao()
                    .load(categoryId);
            categoryTitle = category.getName();
            new RssReader(this)
                    .urls(new String[] { category.getUrl() })
                    .showDialog(false)
                    .parse(this);
        } else {
            prepareNewsFeed();
        }

        getSupportActionBar().setTitle(categoryTitle);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("category_title", categoryTitle);
        outState.putLong("category_id", categoryId);
        outState.putInt("daoSequenceNumber", daoSequenceNumber);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.news_list_menu, menu);
        return true;
    }

    public void sortNews(MenuItem item) {
        if (!getTaskManager().isRunning(SORT_TAG)) {
            showSortDialog();
        } else {
            Toast.makeText(this, "I'm busy.", Toast.LENGTH_SHORT).show();
        }
    }

    public void getAllFeed(MenuItem item) {
        daoSequenceNumber = ((NewsApp) getApplication()).getAsyncSession()
                .queryList(getQuery(item.getItemId())).getSequenceNumber();
    }

    public void sortInDb(MenuItem item) {
        ((NewsApp) getApplication()).getAsyncSession().queryList(getQuery(item.getItemId()));
    }

    private Query<NewsFeed> getQuery(int id) {
        QueryBuilder<NewsFeed> builder = ((NewsApp) getApplication())
                .getSession()
                .getNewsFeedDao()
                .queryBuilder();
        if (id != R.id.getAllFeed) {
            builder.where(NewsFeedDao.Properties.CategoryId.eq(categoryId));
        }
        switch (id) {
            case R.id.sortByTitle:
                builder.orderAsc(NewsFeedDao.Properties.Title);
                break;
            case R.id.sortByDateAsc:
                builder.orderAsc(NewsFeedDao.Properties.PublicationDateTime);
                break;
            case R.id.sortByDateDesc:
                builder.orderDesc(NewsFeedDao.Properties.PublicationDateTime);
                break;
            case R.id.getAllFeed:
                builder.join(NewsFeedDao.Properties.CategoryId, NewsCategory.class);
                break;
        }

        return builder.build();
    }

    // Observable scroll view callbacks
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            if (scrollState == ScrollState.UP) {
                if (ab.isShowing()) {
                    ab.hide();
                }
            } else if (scrollState == ScrollState.DOWN) {
                if (!ab.isShowing()) {
                    ab.show();
                }
            }
        }
    }

    // Rss manager callbacks
    @Override
    public void onSuccess(List<RssItem> rssItems) {
        getTaskManager().execute(
                new DbUpdaterFromRss(UPDATE_FEED_TAG, ((NewsApp) getApplication()).getSession(),
                        categoryId, rssItems),
                this);
    }

    @Override
    public void onFailure(String message) {
        loadingBar.setVisibility(View.GONE);
        Snackbar.make(newsFeed, "Error: " + message, Snackbar.LENGTH_SHORT)
                .setDuration(100000)
                .show();
    }

    // Greendao callback
    @Override
    public void onAsyncOperationCompleted(AsyncOperation operation) {
        if (operation.isCompletedSucessfully()) {
            NewsApp.feed = (List<NewsFeed>) operation.getResult();
            if (operation.getSequenceNumber() == daoSequenceNumber) {
                // Add category to Title
                for (NewsFeed news : NewsApp.feed) {
                    news.setTitle(String.format("<< %s >> : %s",
                            news.getCategory().getName(), news.getTitle()));
                }
                ((NewsApp) getApplication()).getSession().clear();
            }
            prepareNewsFeed();
        } else if (operation.isFailed()) {
            Snackbar.make(newsFeed, "Internal DB error", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Async task callbacks
    @Override
    public void onPreExecute(Task<?, ?> task) {
        if (task.getTag().equals(SORT_TAG)) {
            sortBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        switch (task.getTag()) {
            case SORT_TAG:
                finishSortActions((double[]) task.getResult());
                break;
            case UPDATE_FEED_TAG:
                finishUpdateFeedActions((Boolean) task.getResult());
                break;
            default:
                break;
        }

    }

    @Override
    public Task.Callback onPreAttach(Task<?, ?> task) {
        if (task.getTag().equals(SORT_TAG)) {
            sortBar.setVisibility(View.VISIBLE);
        }
        return this;
    }

    private void extractParameters(Bundle savedState) {
        if (savedState != null) {
            categoryTitle = savedState.getString("category_title");
            categoryId = savedState.getLong("category_id");
            daoSequenceNumber = savedState.getInt("daoSequenceNumber");
        }
    }

    private void prepareNewsFeed() {
        adapter = new NewsAdapter(this, NewsApp.feed);
        newsFeed.setAdapter(adapter);
        newsFeed.setLayoutManager(new LinearLayoutManager(this));
        newsFeed.setHasFixedSize(true);
        newsFeed.setScrollViewCallbacks(this);
        newsFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final Picasso picasso = Picasso.with(NewsListActivity.this);

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                    picasso.resumeTag(NewsListActivity.this);
                } else {
                    picasso.pauseTag(NewsListActivity.this);
                }
            }
        });
        loadingBar.setVisibility(View.GONE);
    }

    private void finishSortActions(double[] elapsedTime) {
        prepareNewsFeed();
        sortBar.setVisibility(View.GONE);
        Snackbar.make(newsFeed,
                String.format("Prepare data: %.3f ms. Sort data: %.3f ms.",
                        elapsedTime[0], elapsedTime[1]),
                Snackbar.LENGTH_LONG)
                .setDuration(15000)
                .show();
    }

    private void finishUpdateFeedActions(boolean isUpdated) {
        prepareNewsFeed();
        if (isUpdated) {
            Snackbar.make(newsFeed, "New items in feed", Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSortDialog() {
        new MaterialDialog.Builder(this)
                .autoDismiss(false)
                .title("Sort parameters")
                .customView(R.layout.sort_dialog, true)
                .positiveText("start sort")
                .negativeText("cancel")
                .cancelable(false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DialogResultsParser parser = new DialogResultsParser(dialog.getCustomView());
                        SortTask task = new SortTask(SORT_TAG,
                                NewsListActivity.this,
                                SortingMethodFactory.getMethod(parser.getMethodId()),
                                parser.getWord());
                        getTaskManager().execute(task, NewsListActivity.this);

                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        Picasso.with(this).cancelTag(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }
}
