package com.newsapp.maximka.newsapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.newsapp.maximka.newsapp.adapters.MainActivityPagerAdapter;
import com.newsapp.maximka.newsapp.fragments.CategoriesFragment;
import com.newsapp.maximka.newsapp.fragments.MusicFragment;
import com.newsapp.maximka.newsapp.music_player.MusicService;

import java.util.ArrayList;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private final static String LOG_TAG = "MainActivity";
    private ViewPager viewPager;
    private boolean showMenuFlag = true;
    private ArrayList<Fragment> pages = new ArrayList<>();
    MainActivityPagerAdapter pagerAdapter;

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void addPage(Fragment fragment) {
        pages.add(fragment);
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPages();

        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showMenuFlag = position == 0;
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        selectCurrentPage(getIntent());

        Log.d(LOG_TAG,  "onCreate");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        selectCurrentPage(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return showMenuFlag;
    }

    public void showAllCategories(MenuItem item) {
        ((CategoriesFragment) pages.get(0)).restoreAllCategories();
        Toast.makeText(this, "Categories were restored", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");

        String lastNewsName = NewsApp.preferences.getLastNewsTitle();
        if (lastNewsName != null) {
            getSupportActionBar().setTitle(lastNewsName);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
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

    private void selectCurrentPage(Intent intent) {
        Bundle bundleWithSelection = intent.getExtras();
        if (bundleWithSelection != null) {
            viewPager.setCurrentItem(bundleWithSelection.getInt(MusicService.SELECTED_TAB, 0), true);
        }
    }

    private void initPages() {
        pagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), pages);
        pages.add(new CategoriesFragment());
        //MainActivityPermissionsDispatcher.addPageWithCheck(this, new MusicFragment());
    }
}
