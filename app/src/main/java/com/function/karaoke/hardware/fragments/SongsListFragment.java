package com.function.karaoke.hardware.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCaller;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.RecordingRecycleViewAdapter;
import com.function.karaoke.hardware.SongRecyclerViewAdapter;
import com.function.karaoke.hardware.SongsActivity;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.DatabaseSongsDB;
import com.function.karaoke.hardware.activities.Model.Genres;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.RecordingDB;
import com.function.karaoke.hardware.activities.Model.RecordingDisplay;
import com.function.karaoke.hardware.activities.Model.SongDisplay;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.ui.SongsActivityUI;
import com.function.karaoke.hardware.utils.Converter;
import com.function.karaoke.hardware.utils.OnSwipeTouchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Songs.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SongsListFragment extends Fragment implements DatabaseSongsDB.IListener, ActivityResultCaller, SongsActivityUI.SongsUIListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int ALL_SONGS_DISPLAYED = 1;
    private static final int PERSONAL_RECORDING_DISPLAYED = 2;


    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private SongRecyclerViewAdapter mAdapter;
    private View songsView;
    private DatabaseSongsDB currentDatabaseSongs;
    private RecyclerView recyclerView;
    private List<DatabaseSongsDB> previousSongs = new ArrayList<>();
    private String previousQuery = "";
    private DatabaseDriver databaseDriver;
    private RecordingService recordingService;
    private RecordingDB recordingDB;

    private int contentsDisplayed = ALL_SONGS_DISPLAYED;
    private DatabaseSongsDB songsDb;
    private View popupView;
    private PopupWindow popup;
    private boolean searchOpened = false;
    private Genres genres;
    private View view;
    private DatabaseSongsDB allSongsDatabase = new DatabaseSongsDB();
    private TextView genreClicked;
    private TextView allSongsTextView;
    private AuthenticationDriver authenticationDriver;
    private SongsActivityUI songsActivityUI;
    private String currentLanguage;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongsListFragment() {
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

        Context context = songsView.getContext();
        recyclerView = songsView.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(mAdapter);
        addSearchListener();
        this.databaseDriver = new DatabaseDriver();
        this.recordingService = new RecordingService();
        setClickListeners(songsView);
        view = songsView;
        songsActivityUI = new SongsActivityUI(view, this, getCurrentLanguage(), getContext());
        addGenres();
        return songsView;
    }

    private String getCurrentLanguage() {
        return getResources().getConfiguration().getLocales().get(0).getLanguage();
    }

    private void addGenres() {
        final Observer<Genres> searchObserver = products -> {
            genres = products;
            if (genres != null)
                songsActivityUI.addGenresToScreen(genres);
        };
        this.databaseDriver.getAllGenresInCollection().observe(getViewLifecycleOwner(), searchObserver);
    }

    @Override
    public void getAllSongsFromGenre(String genre) {
        List<DatabaseSong> searchedSongs = new ArrayList<>();
        if (genre.equals("כל השירים"))
            searchedSongs = allSongsDatabase.getSongs();
        else {
            for (DatabaseSong song : allSongsDatabase.getSongs()) {
                if (song.getGenre().toLowerCase().equals(genre)) {
                    searchedSongs.add(song);
                }
            }
        }
        currentDatabaseSongs.updateSongs(searchedSongs);
        mAdapter.notifyDataSetChanged();
    }


    private void setClickListeners(View songsView) {

        songsView.findViewById(R.id.open_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsActivityUI.openSearchBar(searchOpened);
                searchOpened = !searchOpened;
            }
        });

        songsView.findViewById(R.id.settings_button).setOnClickListener(this::openSettingsPopup);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseSongsDB songsDB = mListener.getSongs();
        songsDB.subscribe(this);

        getAllSongs();
    }

    private void getAllSongs() {
        final Observer<List<DatabaseSong>> searchObserver = products -> {
            view.findViewById(R.id.loading_songs_progress_bar).setVisibility(View.INVISIBLE);
            currentDatabaseSongs.updateSongs(products);
            allSongsDatabase = new DatabaseSongsDB(currentDatabaseSongs);
            allSongsDatabase.updateSongs(currentDatabaseSongs.getSongs());
        };
        this.databaseDriver.getAllSongsInCollection(DatabaseSong.class).observe(this, searchObserver);
    }

    private void getAllPersonalSongs() {
        final Observer<List<Recording>> personalRecordingObserver = personalRecordings -> {
            if (personalRecordings != null) {
                recordingDB = new RecordingDB(personalRecordings);
                view.findViewById(R.id.no_recordings_text).setVisibility(View.INVISIBLE);
                displayPersonalSongs();
            } else {
                currentDatabaseSongs.updateSongs(new ArrayList<>());
                mAdapter.notifyDataSetChanged();
                updateUINoRecordings();
            }

        };
        this.recordingService.getRecordingFromUID().observe(getViewLifecycleOwner(), personalRecordingObserver);
    }

    private void updateUINoRecordings() {
        view.findViewById(R.id.no_recordings_text).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.no_recordings_text)).setText(R.string.no_recordings);
    }

    @Override
    public void onPause() {
        super.onPause();
//        mListener.getSongs().unsubscribe(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        currentDatabaseSongs = mListener.getSongs();
        mAdapter = new SongRecyclerViewAdapter(currentDatabaseSongs.getSongs(), mListener,
                ((SongsActivity) requireActivity()).language, getActivity().getResources().getString(R.string.play));
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
        mAdapter.setData(songsDb.getSongs(), getActivity().getResources().getString(R.string.play));
        mAdapter.notifyDataSetChanged();
    }

    private void displayPersonalSongs() {
//        mAdapter.setData(makeListOfRecordingDisplay(), getActivity().getResources().getString(R.string.open));
//        mAdapter.notifyDataSetChanged();
        RecordingRecycleViewAdapter recordAdapter = new RecordingRecycleViewAdapter(recordingDB.getRecordings(), mListener, ((SongsActivity) requireActivity()).language);
        recyclerView.setAdapter(recordAdapter);
    }

    private List<RecordingDisplay> makeListOfRecordingDisplay() {
        List<RecordingDisplay> recordingDisplays = new ArrayList<>();
        for (Recording recording : recordingDB.getRecordings()) {
            if (recordingDisplays.stream().noneMatch(o -> o.getTitle().equals(recording.getTitle())))
                recordingDisplays.add(new RecordingDisplay(recording.getImageResourceFile(),
                        recording.getArtist(), recording.getTitle()));
        }
        return recordingDisplays;
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
                        addCopyOfSongsDBToList(currentDatabaseSongs);
                        getSongsSearchedFor(query.toLowerCase());
                    } else {
                        currentDatabaseSongs.updateSongs(previousSongs.get(previousSongs.size() - 1).getSongs());
                        previousSongs.remove(previousSongs.size() - 1);
                    }
                    mAdapter.notifyDataSetChanged();
                    previousQuery = query;
                } else {
                    if (previousSongs.size() != 0) {
                        currentDatabaseSongs.updateSongs(previousSongs.get(0).getSongs());
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
        for (DatabaseSong song : currentDatabaseSongs.getSongs()) {
            if (song.getTitle().toLowerCase().contains(query) || song.getArtist().toLowerCase().contains(query)) {
                searchedSongs.add(song);

            }
        }
        currentDatabaseSongs.updateSongs(searchedSongs);
    }

    public void openSettingsPopup(View view) {
        authenticationDriver = new AuthenticationDriver();
        songsActivityUI.openSettingsPopup(authenticationDriver.isSignIn()
                        && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals(""),
                contentsDisplayed);
        popupView = songsActivityUI.getPopupView();
        popup = songsActivityUI.getPopup();
        addPopupListeners();
        songsActivityUI.getPopup().setOnDismissListener(this::undimBackground);
        popupView.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()) {
            public void onSwipeTop() {
            }

            public void onSwipeRight() {
                if (currentLanguage.equals("iw"))
                    popup.dismiss();
            }

            public void onSwipeLeft() {
                if (!currentLanguage.equals("iw"))
                    popup.dismiss();
            }

            public void onSwipeBottom() {
            }

        });

    }

    private void addPopupListeners() {
        languageChangeListener();
        dismissButtonListener();
        myRecordingsToDisplayListener();
        homeButtonListener();
        signInButtonListener();


    }

    private void signInButtonListener() {
        popupView.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (authenticationDriver.getUserUid() != null) {
                    mListener.signOut();
                } else {
                    mListener.openSignUp();
                }
                popup.dismiss();
            }
        });
    }

    private void homeButtonListener() {
        popupView.findViewById(R.id.home_button).setOnClickListener(view -> {
            if (contentsDisplayed == PERSONAL_RECORDING_DISPLAYED) {
                contentsDisplayed = ALL_SONGS_DISPLAYED;
                displayAllSongs();
                ((TextView) view).setTextColor(getResources().getColor(R.color.gold, getContext().getTheme()));
                ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(getResources().getColor(R.color.sing_up_hover, getContext().getTheme()));
                ((TextView) this.view.findViewById(R.id.display_text)).setText(R.string.all_songs);
            }
        });
    }

    private void myRecordingsToDisplayListener() {
        popupView.findViewById(R.id.my_recordings).setOnClickListener(view -> {
            if (authenticationDriver.isSignIn()) {
                if (contentsDisplayed == ALL_SONGS_DISPLAYED) {
                    contentsDisplayed = PERSONAL_RECORDING_DISPLAYED;
                    AuthenticationDriver authenticationDriver = new AuthenticationDriver();
                    if (recordingDB == null || (!recordingDB.getRecorderId().equals(authenticationDriver.getUserUid()))) {
                        recordingDB = null;
                        getAllPersonalSongs();
                    } else {
                        displayPersonalSongs();
                    }
                    ((TextView) view).setTextColor(getResources().getColor(R.color.gold, getContext().getTheme()));
                    ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(getResources().getColor(R.color.sing_up_hover, getContext().getTheme()));
                    ((TextView) this.view.findViewById(R.id.display_text)).setText(R.string.My_recordings);
                }
            } else {
                mListener.alertUserToSignIn();
            }
        });
    }

    private void dismissButtonListener() {
//        popupView.findViewById(R.id.close_popup).setOnClickListener(view -> popup.dismiss());
    }

    private void languageChangeListener() {
        popupView.findViewById(R.id.language_changer).setOnClickListener(view -> mListener.changeLanguage());
    }


    public void undimBackground() {
        ViewOverlay overlay = view.getOverlay();
        overlay.clear();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteractionPlay(SongDisplay item);

        void onListFragmentInteractionPlay(Recording item);

        void onListFragmentInteractionShare(Recording item);

        DatabaseSongsDB getSongs();

        void changeLanguage();

        void openSignUp();

        void alertUserToSignIn();

        void signOut();
    }
}
