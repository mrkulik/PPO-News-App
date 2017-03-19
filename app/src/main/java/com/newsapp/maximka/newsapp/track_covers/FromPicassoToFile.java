package com.newsapp.maximka.newsapp.track_covers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;


public class FromPicassoToFile implements Target {
    public final static String COVERS_FOLDER = "covers";
    private Context context;
    private String url;
    private static HashSet<Target> activeTargets = new HashSet<>();
    private static HashSet<String> loadedUrls = new HashSet<>();

    public static boolean isUrlLoaded(String url) {
        return loadedUrls.contains(url);
    }

    public static String getFilePath(Context context, String url) {
        return getFileFolder(context) + "/" + getFileName(url);
    }

    private static String getFileName(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private static String getFileFolder(Context context) {
        return context.getFilesDir() + "/" + COVERS_FOLDER;
    }

    public FromPicassoToFile(Context context, String url) {
        this.context = context;
        this.url = url;
        loadedUrls.add(url);
        activeTargets.add(this);
    }

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom arg1) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(getFileFolder(context), getFileName(url));
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        activeTargets.remove(this);
    }

    @Override
    public void onBitmapFailed(Drawable arg0) {
        activeTargets.remove(this);
    }

    @Override
    public void onPrepareLoad(Drawable arg0) {
    }
}
