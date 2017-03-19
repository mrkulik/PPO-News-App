package com.newsapp.maximka.newsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.newsapp.maximka.newsapp.fragments.NewsFragment;
import com.newsapp.maximka.newsapp.gestures.OnScaleTouchListener;
import com.newsapp.maximka.newsapp.gestures.OnSwipeTouchListener;

import butterknife.ButterKnife;

public class NewsActivity extends AppCompatActivity {
    private final String LOG_TAG = "News descr. activity";
    private OnSwipeTouchListener swipeListener;
    private OnScaleTouchListener scaleListener;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        scaleListener.getGestureDetector().onTouchEvent(ev);
        swipeListener.getGestureDetector().onTouchEvent(ev);

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        ButterKnife.bind(this);
        getSupportActionBar().hide();

        swipeListener = new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                ButterKnife.findById(NewsActivity.this, R.id.previousNews).callOnClick();
            }

            @Override
            public void onSwipeLeft() {
                ButterKnife.findById(NewsActivity.this, R.id.nextNews).callOnClick();
            }
        };

        scaleListener = new OnScaleTouchListener(this, 0.5f, 2f) {
            @Override
            public void scaleAction() {
                TextView text = ButterKnife.findById(NewsActivity.this, R.id.newsText);
                text.setTextSize(16 * getScaleFactor());
            }
        };

        if (savedInstanceState == null) {
            NewsFragment newsFragment = new NewsFragment();
            newsFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.newsFragment, newsFragment)
                    .commit();
        }

        Log.d(LOG_TAG,  "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }
}
