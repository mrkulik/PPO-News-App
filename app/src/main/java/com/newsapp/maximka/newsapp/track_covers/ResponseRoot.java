package com.newsapp.maximka.newsapp.track_covers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by maximka on 3.11.16.
 */

public class ResponseRoot {
    private Track track;

    public Track getTrack() {
        return track;
    }

    public class Track {
        private Album album;

        public Album getAlbum() {
            return album;
        }

        public class Album {
            private List<Image> image;

            public Image getLargestCover() {
                return image.get(image.size() - 1);
            }

            public class Image {
                private String size;
                @SerializedName("#text")
                private String url;

                public String getSize() {
                    return size;
                }

                public String getUrl() {
                    return url;
                }
            }
        }
    }
}
