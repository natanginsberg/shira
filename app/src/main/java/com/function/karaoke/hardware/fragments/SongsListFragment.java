package com.function.karaoke.hardware.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.hardware.DatabaseDriver;
import com.function.karaoke.hardware.DatabaseSong;
import com.function.karaoke.hardware.DatabaseSongsDB;
import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.SongRecyclerViewAdapter;
import com.function.karaoke.hardware.SongsActivity;
import com.function.karaoke.hardware.UrlParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A fragment representing a list of Songs.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SongsListFragment extends Fragment implements DatabaseSongsDB.IListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int DOWNLOAD_WORDS = 100;
    private static final int GET_COVER_IMAGE = 101;
    private static final int GET_AUDIO = 102;


    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private SongRecyclerViewAdapter mAdapter;
    private View songsView;
    private FragmentActivity myContext;
    //    private SongsDB songs;
    private DatabaseSongsDB databaseSongs;
    private RecyclerView recyclerView;
    //    private List<SongsDB> previousSongs = new ArrayList<>();
    private List<DatabaseSongsDB> previousSongs = new ArrayList<>();
    private String previousQuery = "";
    private DatabaseDriver databaseDriver;
    private List<DatabaseSong> allSongs = new ArrayList<>();
    private NetworkFragment networkFragment;
    private UrlParser urlParser;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongsListFragment() {
    }

    @SuppressWarnings("unused")
    public static SongsListFragment newInstance(int columnCount) {
        SongsListFragment fragment = new SongsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        songsView = inflater.inflate(R.layout.fragment_song_list, container, false);

        // Set the adapter
//        if (view instanceof RecyclerView) {
        Context context = songsView.getContext();
        recyclerView = (RecyclerView) songsView.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(mAdapter);
//        }
        addSearchListener();
        this.databaseDriver = new DatabaseDriver();
        return songsView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        SongsDB songsDB = mListener.getSongs();
        DatabaseSongsDB songsDB = mListener.getSongs();
        songsDB.subscribe(this);
//        songsDB.subscribe(this);

        getAllSongs();
//        songsDB.scan();
    }

    private void getAllSongs() {
        final Observer<List<DatabaseSong>> searchObserver = products -> {
            databaseSongs.updateSongs(products);
        };
        this.databaseDriver.getAllSongsInCollection(DatabaseSong.class).observe(this, searchObserver);
    }

//    private class CreateObserver implements LifecycleObserver {
//        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//        public void connectListener() {
//            databaseSongs.updateSongs(Collections.singletonList(urlParser.getDbSong()));
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        else
//            mListener.getSongs().scan();
    }

    @Override
    public void onPause() {
        super.onPause();
//        mListener.getSongs().unsubscribe(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
//        songs = mListener.getSongs();
//        mAdapter = new SongRecyclerViewAdapter(songs.getSongs(), mListener, ((SongsActivity) Objects.requireNonNull(getActivity())).language);
        databaseSongs = mListener.getSongs();
        mAdapter = new SongRecyclerViewAdapter(databaseSongs.getSongs(), mListener,
                ((SongsActivity) Objects.requireNonNull(getActivity())).language);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


//    @Override
//    public void onListUpdated() {
//        SongsDB songs = mListener.getSongs();
//        mAdapter.setData(songs.getSongs());
//        mAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onListUpdated() {
        DatabaseSongsDB songsDb = mListener.getSongs();
        mAdapter.setData(songsDb.getSongs());
        mAdapter.notifyDataSetChanged();
    }

//    private void addSearchListener() {
//        SearchView searchView = songsView.findViewById(R.id.search_input);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                if (query.length() >= 1) {
//                    if (query.length() > previousQuery.length()) {
//                        addCopyOfSongsDBToList(songs);
//                        getSongsSearchedFor(query.toLowerCase());
//                    } else {
//                        songs.updateSongs(previousSongs.get(previousSongs.size() - 1).getSongs());
//                        previousSongs.remove(previousSongs.size() - 1);
//                    }
//                    mAdapter.notifyDataSetChanged();
//                    previousQuery = query;
//                } else {
//                    if (previousSongs.size() != 0) {
//                        songs.updateSongs(previousSongs.get(0).getSongs());
//                        mAdapter.notifyDataSetChanged();
//                        previousSongs = new ArrayList<>();
//                        previousQuery = "";
//                    }
//
//                }
//                return false;
//            }
//        });
//    }

    private void addSearchListener() {
        SearchView searchView = songsView.findViewById(R.id.search_input);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() >= 1) {
                    if (query.length() > previousQuery.length()) {
                        addCopyOfSongsDBToList(databaseSongs);
                        getSongsSearchedFor(query.toLowerCase());
                    } else {
                        databaseSongs.updateSongs(previousSongs.get(previousSongs.size() - 1).getSongs());
                        previousSongs.remove(previousSongs.size() - 1);
                    }
                    mAdapter.notifyDataSetChanged();
                    previousQuery = query;
                } else {
                    if (previousSongs.size() != 0) {
                        databaseSongs.updateSongs(previousSongs.get(0).getSongs());
                        mAdapter.notifyDataSetChanged();
                        previousSongs = new ArrayList<>();
                        previousQuery = "";
                    }

                }
                return false;
            }
        });
    }

//    private void addCopyOfSongsDBToList(SongsDB songs) {
//        SongsDB preSongs = new SongsDB(songs);
//        preSongs.updateSongs(songs.getSongs());
//        previousSongs.add(preSongs);
//    }

    private void addCopyOfSongsDBToList(DatabaseSongsDB songs) {
        DatabaseSongsDB preSongs = new DatabaseSongsDB(songs);
        preSongs.updateSongs(songs.getSongs());
        previousSongs.add(preSongs);
    }

//    private void getSongsSearchedFor(String query) {
//        List<Song> searchedSongs = new ArrayList<>();
//        for (Song song : songs.getSongs()) {
//            if (song.title.toLowerCase().contains(query) || song.artist.toLowerCase().contains(query)) {
//                searchedSongs.add(song);
//
//            }
//        }
//        songs.updateSongs(searchedSongs);
//    }

    private void getSongsSearchedFor(String query) {
        List<DatabaseSong> searchedSongs = new ArrayList<>();
        for (DatabaseSong song : databaseSongs.getSongs()) {
            if (song.getTitle().toLowerCase().contains(query) || song.getArtist().toLowerCase().contains(query)) {
                searchedSongs.add(song);

            }
        }
        databaseSongs.updateSongs(searchedSongs);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        //        void onListFragmentInteraction(Song item);
        void onListFragmentInteraction(DatabaseSong item);

        //        SongsDB getSongs();
        DatabaseSongsDB getSongs();
    }
}
