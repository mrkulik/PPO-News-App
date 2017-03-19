package com.newsapp.maximka.newsapp.fragments;


import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.newsapp.maximka.newsapp.NewsApp;
import com.newsapp.maximka.newsapp.R;
import com.newsapp.maximka.newsapp.adapters.MusicAdapter;
import com.newsapp.maximka.newsapp.models.Track;
import com.newsapp.maximka.newsapp.models.TrackCover;
import com.newsapp.maximka.newsapp.music_player.MusicController;
import com.newsapp.maximka.newsapp.music_player.MusicService;

import org.greenrobot.greendao.async.AsyncSession;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import teaspoon.annotations.OnBackground;
import teaspoon.annotations.OnUi;


@RuntimePermissions
public class MusicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static Uri SOURCE = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private final static String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private final static String[] PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA};

    private ListView musicList;
    // music
    private boolean stopPlayback = true;
    private MusicService musicService;
    private Intent playIntent;
    private MusicController controller;
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            if (musicService.getTracks() == null) {
                getLoaderManager().initLoader(1, null, MusicFragment.this);
            } else {
                prepareMusicListView();
            }
            if (controller == null) {
                prepareController();
            }
            musicService.setMusicController(controller);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("SERVICE", "Disconnect");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        playIntent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(playIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!musicService.isPlaying()) {
            getActivity().stopService(playIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(playIntent, musicConnection, 0);
        if (controller != null && !controller.isShowing()) {
            controller.show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (controller != null && controller.isShowing()) {
            controller.hide();
        }
        getActivity().unbindService(musicConnection);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        musicList = (ListView) view.findViewById(R.id.musicList);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicService.play(position);
            }
        });
        musicList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private HashSet<Integer> positionsToRemove;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    positionsToRemove.add(position);
                } else {
                    positionsToRemove.remove(position);
                }
                mode.setSubtitle(positionsToRemove.size() + " selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                positionsToRemove = new HashSet<>();
                mode.getMenuInflater().inflate(R.menu.player_menu, menu);
                mode.setTitle("Delete tracks");
                mode.setSubtitle(positionsToRemove.size() + " selected");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                MusicFragmentPermissionsDispatcher
                        .removeWithCheck(MusicFragment.this, item, positionsToRemove);
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (controller != null) {
            if (isVisibleToUser) {
                controller.startShowAnimation();
            } else {
                controller.startHideAnimation();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        scrollToSelectedTrack();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), SOURCE, PROJECTION, SELECTION, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        prepareTracksAsync(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void remove(MenuItem item, HashSet<Integer> positionsToRemove) {
        switch (item.getItemId()) {
            case R.id.removeFromDB:
                removeFromDbAsync(positionsToRemove);
                break;
            case R.id.removeFromDBandSD:
                removeFromSdAsync(positionsToRemove);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MusicFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnBackground
    public void removeFromDbAsync(HashSet<Integer> positionsToRemove) {
        removeFromDb(positionsToRemove);
    }

    @OnBackground
    public void removeFromSdAsync(HashSet<Integer> positionsToRemove) {
        List<Track> tracks = musicService.getTracks();
        for (Integer position : positionsToRemove) {
            if (!new File(tracks.get(position).getData()).delete()) {
                showSdFailDialog();
                return;
            }

            if (position == musicService.getSelectedTrack()) {
                musicService.pauseAndRefresh();
            }
        }

        removeFromDb(positionsToRemove);
    }

    private void prepareMusicListView() {
        MusicAdapter musicAdapter = new MusicAdapter(getActivity(), musicService);
        musicList.setAdapter(musicAdapter);
        musicService.setStateChangedListener(musicAdapter);
        scrollToSelectedTrack();
    }

    private void prepareController() {
        controller = new MusicController(getActivity(), musicService);
        controller.setAnchorView(musicList);
        controller.setEnabled(true);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playPrevious();
            }
        });

        if (!getUserVisibleHint()) {
            controller.setPositionUnderScreen();
        } else {
            controller.show();
        }
    }

    private void scrollToSelectedTrack() {
        musicList.clearFocus();
        musicList.setSelection(musicService.getSelectedTrack());
    }

    private void removeFromDb(HashSet<Integer> positionsToRemove) {
        List<Track> tracks = musicService.getTracks();
        int selectedTrack = musicService.getSelectedTrack();
        int itemsBeforeSelected = 0;
        stopPlayback = false;

        for (Integer position : positionsToRemove) {
            if (position == selectedTrack) {
                stopPlayback = true;
            }
            if (position < selectedTrack) {
                ++itemsBeforeSelected;
            }
        }
        musicService.setSelectedTrack(stopPlayback ? 0 : selectedTrack - itemsBeforeSelected);

        getActivity().getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                buildWhereClause(positionsToRemove, tracks), null);
        removeCovers(positionsToRemove, tracks);
    }

    private void removeCovers(HashSet<Integer> positionsToRemove, List<Track> tracks) {
        List<TrackCover> coversToDelete = new ArrayList<>();
        for (Integer position : positionsToRemove) {
            Track track = tracks.get(position);
            coversToDelete.add(new TrackCover(track.getId(), track.getCoverFilePath()));
        }
        ((NewsApp) getActivity().getApplication()).getAsyncSession()
                .deleteInTx(TrackCover.class, coversToDelete);
    }

    private String buildWhereClause(HashSet<Integer> positionsToRemove, List<Track> tracks) {
        StringBuilder where = new StringBuilder(MediaStore.Audio.Media._ID).append(" in (");
        for (Integer position : positionsToRemove) {
            where.append(tracks.get(position).getId()).append(",");
        }
        where.deleteCharAt(where.length() - 1).append(")");

        return where.toString();
    }

    @OnUi
    private void showSdFailDialog() {
        new MaterialDialog.Builder(getActivity())
                .autoDismiss(false)
                .title("Fail :(")
                .content("It seems like you can't delete file. The file does not exist or you use android 4.4 and greater. If last you may install from play market app like SDFix (required root) to make you SD card writable or use your SD card as internal storage (Android 6.0+).")
                .positiveText("got it")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .cancelable(false)
                .show();
    }

    @OnBackground
    private void prepareTracksAsync(Cursor data) {
        List<Track> tracks = new ArrayList<>();
        if (data.getCount() > 0) {
            HashMap<Long, String> existedCovers = new HashMap<>();
            NewsApp app = (NewsApp) getActivity().getApplication();
            AsyncSession session = app.getSession().startAsyncSession();
            for (TrackCover cover : app.getSession().getTrackCoverDao().loadAll()) {
                existedCovers.put(cover.getId(), cover.getPath());
            }

            Track.setColumnIndices(data);
            data.moveToFirst();
            do {
                tracks.add(new Track(data)
                        .loadCoverFilePath(NewsApp.lastFmService, session, getActivity(),
                                existedCovers));
            } while (data.moveToNext());
        }

        deliverPreparedTracks(tracks);
    }

    @OnUi
    private void deliverPreparedTracks(List<Track> tracks) {
        musicService.setTracks(tracks, stopPlayback);
        prepareMusicListView();
        stopPlayback = true;
    }
}
