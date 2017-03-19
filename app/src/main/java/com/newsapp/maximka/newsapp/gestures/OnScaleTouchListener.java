package com.newsapp.maximka.newsapp.gestures;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.newsapp.maximka.newsapp.NewsApp;


public class OnScaleTouchListener implements View.OnTouchListener {
    private float scaleFactor = 1.f;
    private final ScaleGestureDetector gestureDetector;

    public void scaleAction() {
    }

    public OnScaleTouchListener(Activity context, float minScale, float maxScale) {
        gestureDetector = new ScaleGestureDetector(context, new MyPinchListener(minScale, maxScale));
        scaleFactor = NewsApp.preferences.getTextSize() / 16f;
    }

    public ScaleGestureDetector getGestureDetector(){
        return  gestureDetector;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class MyPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private final float minScale;
        private final float maxScale;

        public MyPinchListener(float min, float max) {
            minScale = min;
            maxScale = max;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));

            scaleAction();

            return true;
        }
    }
}
