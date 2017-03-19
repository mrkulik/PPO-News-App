package com.newsapp.maximka.newsapp.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;


@Entity(
        indexes = {
                @Index(value = "categoryId", unique = true)
        }
)
public class LastUpdateTime {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private Date lastUpdate;

    @NotNull
    @Unique
    private Long categoryId;

    @Generated(hash = 1849855057)
    public LastUpdateTime(Long id, @NotNull Date lastUpdate,
                          @NotNull Long categoryId) {
        this.id = id;
        this.lastUpdate = lastUpdate;
        this.categoryId = categoryId;
    }

    @Generated(hash = 1171008286)
    public LastUpdateTime() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
