package com.newsapp.maximka.newsapp.gestures;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class CustomClick implements View.OnTouchListener {
    private View.OnClickListener listener;
    private float startX;
    private float startY;
    private final float CLICK_ACTION_THRESHOLD = 5f;
    private final String LOG_TAG = " --- CustomClick --- ";

    public CustomClick(View.OnClickListener l) {
        listener = l;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                Log.d(LOG_TAG, "down");
                break;
            case MotionEvent.ACTION_UP: {
                float endX = event.getX();
                float endY = event.getY();
                if (isClick(startX, endX, startY, endY)) {
                    if (listener != null) {
                        listener.onClick(v);
                    }
                }
                Log.d(LOG_TAG, "up");
                break;
            }
        }

        return true;
    }

    private boolean isClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD) {
            return false;
        }
        return true;
    }
}
