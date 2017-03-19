package com.newsapp.maximka.newsapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.newsapp.maximka.newsapp.NewsApp;
import com.newsapp.maximka.newsapp.R;
import com.newsapp.maximka.newsapp.background_tasks.NewsContentLoader;
import com.newsapp.maximka.newsapp.models.NewsFeed;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<String> {
    public static final String URL_KEY = "url";
    public static final int LOADER_ID = 1;
    final static String LOG_TAG = "News fragment";

    private int newsId;

    @BindView(R.id.newsText) TextView newsText;
    @BindView(R.id.newsTextLoadingProgress) ProgressBar loadingBar;
    @BindView(R.id.newsImageFragment) ImageView newsImage;
    @BindView(R.id.newsNameFragment) TextView newsName;
    @BindView(R.id.newsDateTimeFragment) TextView newsDateTime;

    @OnClick(R.id.nextNews)
    public void nextNews() {
        switchNews(newsId + 1);
    }

    @OnClick(R.id.previousNews)
    public void previousNews() {
        switchNews(newsId - 1);
    }

    private void switchNews(int newNewsId) {
        if (newNewsId >= 0 && newNewsId < NewsApp.feed.size()) {
            NewsFragment newsFragment = new NewsFragment();
            Bundle args = new Bundle();
            args.putInt("news_id", newNewsId);
            newsFragment.setArguments(args);

            int enterAnimation = R.anim.enter_from_left, exitAnimation = R.anim.exit_to_right;
            if (newNewsId > newsId) {
                enterAnimation = R.anim.enter_from_right;
                exitAnimation = R.anim.exit_to_left;
            }

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(enterAnimation, exitAnimation)
                    .replace(R.id.newsFragment, newsFragment)
                    .commit();
        } else {
            Toast.makeText(getActivity(), "There is no news", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void setArguments(Bundle args) {
        newsId = args.getInt("news_id");
        super.setArguments(args);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("news_id", newsId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            newsId = savedInstanceState.getInt("news_id");
        }
        NewsFeed news = NewsApp.feed.get(newsId);

        newsName.setText(news.getTitle());
        newsDateTime.setText(NewsApp.dateTimeFormatter.toString(news.getPublicationDateTime()));
        Picasso.with(getActivity())
                .load(news.getImageUrl())
                .into(newsImage);

        Bundle args = new Bundle();
        args.putString(URL_KEY, news.getLink());
        getLoaderManager().initLoader(LOADER_ID, args, this).forceLoad();

        Log.d(LOG_TAG, "onActivityCreated");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");

        float scaledDensity = getActivity().getResources().getDisplayMetrics().scaledDensity;
        NewsApp.preferences.saveTextSize(newsText.getTextSize() / scaledDensity);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");

        NewsApp.preferences.saveLastNewsTitle(newsName.getText().toString());
        newsText.setTextSize(NewsApp.preferences.getTextSize());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LOG_TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "onDetach");
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new NewsContentLoader(getActivity(), args);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        loadingBar.setVisibility(View.GONE);
        newsText.setText(data != null ? data : "No content. Try to reload.");
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
