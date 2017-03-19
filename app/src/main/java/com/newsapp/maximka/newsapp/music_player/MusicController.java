package com.newsapp.maximka.newsapp.music_player;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;


public class MusicController extends MediaController {
    private final static float HEIGHT_DP = 88;
    private MusicService musicService;
    private float translationLength;
    
    public class PlayerControl implements MediaController.MediaPlayerControl {
        public static final int AUDIO_SESSION_ID = 42 * 2;
        
        @Override
        public boolean canPause() {
            return true;
        }

        @Override
        public boolean canSeekBackward() {
            return true;
        }

        @Override
        public boolean canSeekForward() {
            return true;
        }

        @Override
        public int getAudioSessionId() {
            return AUDIO_SESSION_ID;
        }

        @Override
        public int getBufferPercentage() {
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            return musicService.getPosition();
        }

        @Override
        public int getDuration() {
            return musicService.getDuration();
        }

        @Override
        public boolean isPlaying() {
            return musicService.isPlaying();
        }

        @Override
        public void pause() {
            musicService.pause();
        }

        @Override
        public void seekTo(int pos) {
            musicService.seek(pos);
        }

        @Override
        public void start() {
            musicService.start();
        }
    }

    public MusicController(Context context, MusicService service) {
        super(context);
        musicService = service;
        setMediaPlayer(new PlayerControl());
        translationLength = HEIGHT_DP * getResources().getDisplayMetrics().density;
    }

    @Override
    public void show() {
        super.show(0);
    }

    @Override
    public void show(int timeout) {
        super.show(0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            ((Activity) getContext()).finish();
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            return ((Activity) getContext()).dispatchKeyEvent(event);
        }

        return super.dispatchKeyEvent(event);
    }

    public void startHideAnimation() {
        animate().cancel();
        setPositionOnBottom();
        animate().translationYBy(translationLength);
        getRootView().animate().alpha(0);
    }

    public void startShowAnimation() {
        if (!isShowing()) {
            show();
        }
        animate().cancel();
        setPositionUnderScreen();
        animate().translationYBy(-translationLength);
        getRootView().animate().alpha(1);
    }

    public void setPositionUnderScreen() {
        setY(translationLength);
        getRootView().setAlpha(0);
    }

    public void setPositionOnBottom() {
        setY(0);
        getRootView().setAlpha(1);
    }
}
