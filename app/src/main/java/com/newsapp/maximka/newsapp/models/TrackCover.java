package com.newsapp.maximka.newsapp.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;


@Entity(
        indexes = {
                @Index(value = "id", unique = true)
        }
)
public class TrackCover {
    @Id
    private Long id;
    private String path;
@Generated(hash = 1019764487)
public TrackCover(Long id, String path) {
    this.id = id;
    this.path = path;
}
@Generated(hash = 909636080)
public TrackCover() {
}
public Long getId() {
    return this.id;
}
public void setId(Long id) {
    this.id = id;
}
public String getPath() {
    return this.path;
}
public void setPath(String path) {
    this.path = path;
}
}
