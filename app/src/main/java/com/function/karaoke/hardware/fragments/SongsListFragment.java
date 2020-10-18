package com.function.karaoke.hardware.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultCaller;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.hardware.RecordingRecycleViewAdapter;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.DatabaseSongsDB;
import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.SongRecyclerViewAdapter;
import com.function.karaoke.hardware.SongsActivity;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.RecordingDB;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.utils.UrlHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Songs.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SongsListFragment extends Fragment implements DatabaseSongsDB.IListener, ActivityResultCaller {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int DOWNLOAD_WORDS = 100;
    private static final int GET_COVER_IMAGE = 101;
    private static final int GET_AUDIO = 102;
    private static final int ALL_SONGS_DISPLAYED = 1;
    private static final int PERSONAL_RECORDING_DISPLAYED = 2;


    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private SongRecyclerViewAdapter mAdapter;
    private RecordingRecycleViewAdapter recordAdapter;
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
    private UrlHolder urlParser;
    private RecordingService recordingService;
    private RecordingDB recordingDB;

    private int contentsDisplayed = ALL_SONGS_DISPLAYED;
    private DatabaseSongsDB songsDb;


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

//    private void setClickListeners(View songsView) {
//        songsView.findViewById(R.id.testButton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mGetContent.launch(new Intent(getActivity(), SignInActivity.class));
//            }
//        });
//    }

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
        this.recordingService = new RecordingService();
        setClickListeners(songsView);
        return songsView;
    }

    private void setClickListeners(View songsView) {
        songsView.findViewById(R.id.personal_library).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contentsDisplayed == PERSONAL_RECORDING_DISPLAYED){
                    contentsDisplayed = ALL_SONGS_DISPLAYED;
                    displayAllSongs();
                    ((Button)songsView.findViewById(R.id.personal_library)).setBackground(getResources().getDrawable(R.drawable.folder));
                } else if (contentsDisplayed == ALL_SONGS_DISPLAYED){
                    contentsDisplayed = PERSONAL_RECORDING_DISPLAYED;
                    if (recordingDB == null){
                        getAllPersonalSongs();
                    } else {
                        displayPersonalSongs();
                    }
                    ((Button)songsView.findViewById(R.id.personal_library)).setBackground(getResources().getDrawable(R.drawable.folder_clicked, getContext().getTheme()));
                }
            }
        });
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

    private void getAllPersonalSongs(){
        final Observer<List<Recording>> personalRecordingObserver = personalRecordings -> {
            if (personalRecordings != null) {
                recordingDB = new RecordingDB(personalRecordings);
                displayPersonalSongs();
            }
        };
        this.recordingService.getRecordingFromUID().observe(this, personalRecordingObserver);
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
                ((SongsActivity) requireActivity()).language);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListUpdated() {
        songsDb = mListener.getSongs();
        displayAllSongs();
    }

    private void displayAllSongs() {
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(songsDb.getSongs());
        mAdapter.notifyDataSetChanged();
    }

    private void displayPersonalSongs(){
        recordAdapter = new RecordingRecycleViewAdapter(recordingDB.getRecordings(), mListener, ((SongsActivity)requireActivity()).language);
        recyclerView.setAdapter(recordAdapter);
    }


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

    private void addCopyOfSongsDBToList(DatabaseSongsDB songs) {
        DatabaseSongsDB preSongs = new DatabaseSongsDB(songs);
        preSongs.updateSongs(songs.getSongs());
        previousSongs.add(preSongs);
    }


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

        void onListFragmentInteraction(Recording item);

        //        SongsDB getSongs();
        DatabaseSongsDB getSongs();
    }
}
