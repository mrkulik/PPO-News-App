package com.newsapp.maximka.newsapp.music_player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.MediaController;

import com.newsapp.maximka.newsapp.MainActivity;
import com.newsapp.maximka.newsapp.NewsApp;
import com.newsapp.maximka.newsapp.R;
import com.newsapp.maximka.newsapp.models.Track;
import com.squareup.seismic.ShakeDetector;

import java.util.List;

import teaspoon.annotations.OnUi;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public final static String SELECTED_TAB = "selected_tab";
    private final static int NOTIFICATION_ID = 42;
    private final static String ACTION_PLAY = "action_play";
    private final static String ACTION_PAUSE = "action_pause";
    private final static String ACTION_NEXT = "action_next";
    private final static String ACTION_PREVIOUS = "action_previous";

    public interface OnStateChangedListener {
        void onSelectTrack(int newTrackPosition);

        void onPlayingStateChanged(boolean isPlaying);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public class HeadsetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isPrepared) {
                switch (intent.getIntExtra("state", -1)) {
                    case 0:
                        pauseAndRefresh();
                        break;
                    case 1:
                        startAndRefresh();
                        break;
                }
            }
        }
    }

    private class BitmapDecoder extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            if (params[0] == null) {
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_album_white_48dp);
            }
            return BitmapFactory.decodeFile(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            startForeground(NOTIFICATION_ID, getNotification(bitmap));
        }
    }

    private IBinder musicBind = new MusicBinder();
    private HeadsetPlugReceiver receiver;
    private OnStateChangedListener stateChangeListener;
    private MediaController musicController;
    private MediaPlayer player;
    private List<Track> tracks;
    private int selectedTrack = 0;
    private boolean isPrepared = false;
    private boolean isFirstPreparing = true;
    private boolean isAfterCreating = true;
    private boolean isAtPreparing = false;

    public void setStateChangedListener(OnStateChangedListener listener) {
        stateChangeListener = listener;
    }

    public void setMusicController(MediaController controller) {
        musicController = controller;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public int getSelectedTrack() {
        return selectedTrack;
    }

    public void setSelectedTrack(int selectedTrack) {
        this.selectedTrack = selectedTrack;
        notifySelectionChanged();
    }

    public void setTracks(List<Track> tracks, boolean stopPlayback) {
        this.tracks = tracks;
        if (stopPlayback) {
            isPrepared = false;
            if (tracks.size() > 0) {
                if (selectedTrack >= tracks.size()) {
                    setSelectedTrack(0);
                }
                isFirstPreparing = true;
                prepareToPlay();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            stopSelf();
        }
        isPrepared = false;
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        isAtPreparing = false;
        if (isAfterCreating) {
            isAfterCreating = false;
            restorePlayPosition();
        }
        mp.start();
        if (isFirstPreparing) {
            isFirstPreparing = false;
            mp.pause();
        }

        refreshControllerWidget();
        notifyPlayingStateChanged();
        showNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stateChangeListener = null;
        musicController = null;
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createPlayer();
        selectedTrack = NewsApp.preferences.getSelectedTrack();

        new ShakeDetector(new ShakeDetector.Listener() {
            @Override
            public void hearShake() {
                playNext();
            }
        }).start((SensorManager) getSystemService(SENSOR_SERVICE));

        receiver = new HeadsetPlugReceiver();
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        NewsApp.preferences.saveTrackPosition(player.getCurrentPosition());
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        handleControlIntents(intent.getAction());
        return START_STICKY;
    }

    public int getPosition() {
        if (!isPrepared) {
            return 0;
        }
        return player.getCurrentPosition();
    }

    public int getDuration() {
        if (!isPrepared) {
            return 0;
        }
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void seek(int position) {
        if (isPrepared) {
            player.seekTo(position);
        }
    }

    public void start() {
        if (isPrepared) {
            player.start();
            notifyPlayingStateChanged();
            showNotification();
        }
    }

    public void startAndRefresh() {
        start();
        refreshControllerWidget();
    }

    public void pause() {
        if (isPrepared) {
            player.pause();
            notifyPlayingStateChanged();
            showNotification();
        }
    }

    public void pauseAndRefresh() {
        pause();
        refreshControllerWidget();
    }

    public void play(int position) {
        if (!isAtPreparing) {
            if (position < 0 || position > tracks.size()) {
                throw new IndexOutOfBoundsException();
            }
            setSelectedTrack(position);
            prepareToPlay();
        }
    }

    public void playPrevious() {
        if (!isAtPreparing) {
            if (--selectedTrack < 0) {
                selectedTrack = tracks.size() - 1;
            }
            notifySelectionChanged();
            prepareToPlay();
        }
    }

    public void playNext() {
        if (!isAtPreparing) {
            if (++selectedTrack >= tracks.size()) {
                selectedTrack = 0;
            }
            notifySelectionChanged();
            prepareToPlay();
        }
    }


    private void initPlayer() {
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    private void createPlayer() {
        player = new MediaPlayer();
        initPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    private boolean setSource() {
        boolean isSourceSet = false;

        if (!tracks.isEmpty()) {
            try {
                Track trackToPlay = tracks.get(selectedTrack);
                player.setDataSource(trackToPlay.getData());
                isSourceSet = true;
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
        }

        return isSourceSet;
    }

    private void prepareToPlay() {
        isPrepared = false;
        player.reset();
        initPlayer();
        if (setSource()) {
            isAtPreparing = true;
            player.prepareAsync();
            NewsApp.preferences.saveSelectedTrack(selectedTrack);
        }
    }

    private void restorePlayPosition() {
        int position = NewsApp.preferences.getTrackPosition();
        if (getDuration() <= position) {
            position = 0;
        }
        seek(position);
    }

    @OnUi
    private void refreshControllerWidget() {
        if (musicController != null) {
            musicController.show();
        }
    }

    @OnUi
    private void notifySelectionChanged() {
        if (stateChangeListener != null) {
            stateChangeListener.onSelectTrack(selectedTrack);
        }
    }

    @OnUi
    private void notifyPlayingStateChanged() {
        if (stateChangeListener != null) {
            stateChangeListener.onPlayingStateChanged(isPlaying());
        }
    }

    private void handleControlIntents(String action) {
        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    startAndRefresh();
                    break;
                case ACTION_PAUSE:
                    pauseAndRefresh();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrevious();
                    break;
            }
        }
    }

    private void showNotification() {
        new BitmapDecoder().execute(tracks.get(selectedTrack).getCoverFilePath());
    }

    private Notification getNotification(Bitmap trackCover) {
        Intent activityIntent = new Intent(getApplication(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(SELECTED_TAB, 1);
        PendingIntent returnToActivityIntent = PendingIntent.getActivity(getApplication(), 0, activityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        return buildNotification(returnToActivityIntent, trackCover);
    }

    private Notification buildNotification(PendingIntent contentIntent, Bitmap trackCover) {
        Track track = tracks.get(selectedTrack);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                .setLargeIcon(trackCover)
                .setTicker(track.getTitle())
                .setContentTitle(track.getTitle())
                .setContentText(track.getArtist())
                .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .addAction(getAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        if (isPlaying()) {
            builder.addAction(getAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
        } else {
            builder.addAction(getAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
        }
        builder.addAction(getAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));

        return builder.build();
    }

    private NotificationCompat.Action getAction(int icon, String title, String intentAction) {
        Intent controlIntent = new Intent(this, MusicService.class).setAction(intentAction);
        return new NotificationCompat.Action.Builder(icon, title,
                PendingIntent.getService(this, 0, controlIntent, 0)).build();
    }
}
