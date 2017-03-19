package com.newsapp.maximka.newsapp.track_covers;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by maximka on 3.11.16.
 */

public interface LastFmService {
    String API_KEY = "33d1f2a443a125285de92fea9621e3bb";

    @Headers({
            "User-Agent: Music-player"
    })
    @GET("/2.0/?method=track.getInfo&format=json&api_key=" + API_KEY)
    Call<ResponseRoot> getTrackInfo(@Query("artist") String artist, @Query("track") String title);
}
