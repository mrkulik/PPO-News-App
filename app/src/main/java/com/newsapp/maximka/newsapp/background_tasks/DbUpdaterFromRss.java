package com.newsapp.maximka.newsapp.background_tasks;

import com.crazyhitty.chdev.ks.rssmanager.RssItem;
import com.newsapp.maximka.newsapp.NewsApp;
import com.newsapp.maximka.newsapp.models.DaoSession;
import com.newsapp.maximka.newsapp.models.LastUpdateTime;
import com.newsapp.maximka.newsapp.models.LastUpdateTimeDao;
import com.newsapp.maximka.newsapp.models.NewsFeed;

import org.neotech.library.retainabletasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DbUpdaterFromRss extends Task<Void, Boolean> {
    private DaoSession session;
    private long categoryId;
    private List<RssItem> items;
    private LastUpdateTime lastUpdateTime;
    private boolean isFeedUpdated = false;

    /**
     * Creates a new Task. This constructor must be invoked on the UI thread.
     *
     * @param tag A unique tag, which is used for retaining and identifying tasks across
     *            configuration changes. The tag needs to be unique on Activity level if you use
     *            an Activity bounded TaskManager to execute this task. If you use an Application
     *            bounded TaskManager the tag needs to be unique across the complete Application.
     */
    public DbUpdaterFromRss(String tag, DaoSession session, long categoryId,
                            List<RssItem> items) {
        super(tag);
        this.session = session;
        this.categoryId = categoryId;
        this.items = items;
    }

    private Date getLastUpdate() {
        lastUpdateTime = session.getLastUpdateTimeDao()
                .queryBuilder()
                .where(LastUpdateTimeDao.Properties.CategoryId.eq(categoryId))
                .unique();
        Date lastUpdate = null;
        if (lastUpdateTime != null) {
            lastUpdate = lastUpdateTime.getLastUpdate();
            lastUpdateTime.setLastUpdate(new Date());
        } else {
            lastUpdateTime = new LastUpdateTime(null, new Date(), categoryId);
        }

        return lastUpdate;
    }

    private void extractNewFeed(Date lastUpdate, List<NewsFeed> feedToAdd) {
        for (RssItem item : items) {
            Date itemDate = NewsApp.dateTimeFormatter.toDate(item.getPubDate());
            if (lastUpdate == null || lastUpdate.before(itemDate)) {
                feedToAdd.add(
                        new NewsFeed(null, item.getTitle(),
                                item.getDescription().replaceAll("<.*?>", ""),
                                item.getImageUrl(), itemDate,
                                item.getLink(), categoryId)
                );
            } else {
                break;
            }
        }
    }

    @Override
    protected Boolean doInBackground() {
        List<NewsFeed> feedToAdd = new ArrayList<>();
        extractNewFeed(getLastUpdate(), feedToAdd);

        if (feedToAdd.size() != 0) {
            session.getNewsFeedDao().insertInTx(feedToAdd);
            session.getNewsCategoryDao().load(categoryId).resetNews();
            isFeedUpdated = true;
        }
        NewsApp.feed = session.getNewsCategoryDao()
                .load(categoryId)
                .getNews();
        session.getLastUpdateTimeDao().insertOrReplace(lastUpdateTime);

        return isFeedUpdated;
    }
}
