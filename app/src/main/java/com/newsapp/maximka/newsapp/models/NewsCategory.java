package com.newsapp.maximka.newsapp.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;


@Entity(
        nameInDb = "Categories",
        active = true,
        indexes = {
                @Index(value = "id", unique = true)
        }
)
public class NewsCategory {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    @Unique
    private String name;

    @NotNull
    @Unique
    private String url;

    @NotNull
    private boolean isHidden;

    @ToMany(referencedJoinProperty = "categoryId")
    @OrderBy("publicationDateTime DESC")
    private List<NewsFeed> news;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 2744165)
    private transient NewsCategoryDao myDao;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getIsHidden() {
        return this.isHidden;
    }

    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 511358159)
    public List<NewsFeed> getNews() {
        if (news == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            NewsFeedDao targetDao = daoSession.getNewsFeedDao();
            List<NewsFeed> newsNew = targetDao._queryNewsCategory_News(id);
            synchronized (this) {
                if (news == null) {
                    news = newsNew;
                }
            }
        }
        return news;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1792236479)
    public synchronized void resetNews() {
        news = null;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2142458083)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getNewsCategoryDao() : null;
    }

    public NewsCategory(String _name, String _url, boolean _isHidden) {
        name = _name;
        url = _url;
        isHidden = _isHidden;
    }

    @Generated(hash = 47755597)
    public NewsCategory(Long id, @NotNull String name, @NotNull String url,
            boolean isHidden) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.isHidden = isHidden;
    }

    @Generated(hash = 1655632563)
    public NewsCategory() {
    }
}
