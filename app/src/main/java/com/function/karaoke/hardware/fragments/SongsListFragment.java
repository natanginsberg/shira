package com.function.karaoke.hardware.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.function.karaoke.hardware.activities.Model.Reocording;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.ui.SongsActivityUI;
import com.function.karaoke.hardware.utils.static_classes.OnSwipeTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private DatabaseSongsDB songsDb;
    private View popupView;
    private PopupWindow popup;
    private boolean searchOpened = false;
    private Genres genres;
    private View view;
    private DatabaseSongsDB allSongsDatabase = new DatabaseSongsDB();
    private int genreClicked = 0;
    private TextView allSongsTextView;
    private AuthenticationDriver authenticationDriver;
    private SongsActivityUI songsActivityUI;
    private String currentLanguage;
    private int clicked = 0;
    private boolean differentSongsDisplayed = true;
    private int contentDisplayed = ALL_SONGS_DISPLAYED;
    private RecordingRecycleViewAdapter recordAdapter;
    private float x1;
    private float y1;


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
        view.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()));
        addRecyclerViewGenreListener();
        return songsView;
    }

    private String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private void addGenres() {
        final Observer<Genres> searchObserver = products -> {
            genres = products;
            if (genres != null)
                songsActivityUI.addGenresToScreen(genres);
        };
        this.databaseDriver.getAllGenresInCollection().observe(getViewLifecycleOwner(), searchObserver);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void addRecyclerViewGenreListener() {
//        recyclerView.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
//            @Override
//            public void onSwipeRight() {
//                super.onSwipeRight();
//                mListener.colorNextGenre();
//            }
//
//            @Override
//            public void onSwipeLeft() {
//                super.onSwipeLeft();
//                mListener.colorPreviousGenre();
//            }
//        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                recyclerView.onTouchEvent(e);
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = e.getX();
                        y1 = e.getY();
                        recyclerView.onScrollStateChanged(RecyclerView.SCROLL_STATE_DRAGGING);
//                if (mListener != null)
//                {
//                    mListener.OnItemClick(v, Position);
//                }
                        break;
                    case MotionEvent.ACTION_UP:
                        recyclerView.onScrollStateChanged(RecyclerView.SCROLL_STATE_SETTLING);
                        float x2 = e.getX();
                        float y2 = e.getY();
                        float diffY = y2 - y1;
                        float diffX = x2 - x1;
                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            float deltaX = x2 - x1;
                            if (Math.abs(deltaX) > 5) {
                                // Left to Right swipe action
                                if (x2 > x1) {
                                    if (getCurrentLanguage().equalsIgnoreCase("iw"))
                                        colorPreviousGenre();
                                    else
                                        colorNextGenre();
                                }

                                // Right to left swipe action
                                else {
                                    if (getCurrentLanguage().equalsIgnoreCase("iw"))
                                        colorNextGenre();
                                    else
                                        colorPreviousGenre();
                                }
                            }
                            break;
                        }
                }
                return true;
            }
        });
    }

    public void colorPreviousGenre() {
        if (genreClicked > 0) {
            songsActivityUI.colorNextGenre(genreClicked - 1);
            getAllSongsFromGenre(genreClicked - 1);
        }
    }

    public void colorNextGenre() {
        if (genreClicked < genres.getGenres().size()) {
            songsActivityUI.colorNextGenre(genreClicked + 1);
            getAllSongsFromGenre(genreClicked + 1);
        }
    }

    @Override
    public void getAllSongsFromGenre(int i) {
        List<DatabaseSong> searchedSongs = new ArrayList<>();
        genreClicked = i;
        String genre = genres.getGenres().get(i).split(",")[1];
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

        songsView.findViewById(R.id.open_search).setOnClickListener(view -> {
            songsActivityUI.openSearchBar(searchOpened);
            searchOpened = !searchOpened;
        });

        songsView.findViewById(R.id.settings_button).setOnClickListener(this::openSettingsPopup);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (contentDisplayed == ALL_SONGS_DISPLAYED) {
            if (allSongsDatabase.getSongs().size() == 0) {
                DatabaseSongsDB songsDB = mListener.getSongs();
                songsDB.subscribe(this);
                getAllSongs();
            }
        }
    }

    private void getAllSongs() {
        final Observer<List<DatabaseSong>> searchObserver = products -> {
            view.findViewById(R.id.loading_songs_progress_bar).setVisibility(View.INVISIBLE);
            currentDatabaseSongs.updateSongs(products);
            allSongsDatabase = new DatabaseSongsDB(currentDatabaseSongs);
            allSongsDatabase.updateSongs(currentDatabaseSongs.getSongs());
//            addTouchForGenrePicking();
        };
        this.databaseDriver.getAllSongsInCollection(DatabaseSong.class).observe(this, searchObserver);
    }

    private void getAllPersonalSongs() {
        final Observer<List<Recording>> personalRecordingObserver = personalRecordings -> {
            contentDisplayed = PERSONAL_RECORDING_DISPLAYED;
            if (personalRecordings != null) {
                recordingDB = new RecordingDB(personalRecordings);
                view.findViewById(R.id.no_recordings_text).setVisibility(View.INVISIBLE);
                displayPersonalSongs();
                differentSongsDisplayed = true;
            } else {
//                currentDatabaseSongs.updateSongs(new ArrayList<>());
//                mAdapter.notifyDataSetChanged();
                differentSongsDisplayed = false;
                songsActivityUI.noRecordings();

            }
            songsActivityUI.hideGenresAndSearch();
            popup.dismiss();

        };
        this.recordingService.getRecordingFromUID().observe(getViewLifecycleOwner(), personalRecordingObserver);
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
        if (differentSongsDisplayed) {
            recyclerView.setAdapter(mAdapter);
            mAdapter.setData(songsDb.getSongs(), getActivity().getResources().getString(R.string.play));
            mAdapter.notifyDataSetChanged();
        }
    }

    private void displayPersonalSongs() {
//        mAdapter.setData(makeListOfRecordingDisplay(), getActivity().getResources().getString(R.string.open));
//        mAdapter.notifyDataSetChanged();
        recordAdapter = new RecordingRecycleViewAdapter(recordingDB.getRecordings(), mListener, ((SongsActivity) requireActivity()).language);
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
                && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals(""), contentDisplayed
        );
        if (authenticationDriver.isSignIn()
                && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals(""))
            songsActivityUI.setEmailAddressIfSignedIn(authenticationDriver.getUserEmail());
        popupView = songsActivityUI.getPopupView();
        popup = songsActivityUI.getPopup();
        addPopupListeners();
        popupView.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()) {
            public void onSwipeTop() {
            }

            public void onSwipeRight() {
                if (Locale.getDefault().getLanguage().equals("iw"))
                    popup.dismiss();
            }

            public void onSwipeLeft() {
                if (!Locale.getDefault().getLanguage().equals("iw"))
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
        contactUsListener();
        policyListener();
    }

    private void policyListener() {
        popupView.findViewById(R.id.policy).setOnClickListener(view -> {
            if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null
                    && (authenticationDriver.getUserEmail().equals("asher307901520@gmail.com") ||
                    authenticationDriver.getUserEmail().equals("natanginsberg@gmail.com") ||
                    authenticationDriver.getUserEmail().equals("yossimordehay@gmail.com"))) {
                clicked++;
                if (clicked == 5) {
                    mListener.openAdminSide();
                    clicked = 0;
                }
            }
        });

    }

    private void contactUsListener() {
        popupView.findViewById(R.id.contact_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mListener.openEmailIntent();
            }
        });
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
            contentDisplayed = ALL_SONGS_DISPLAYED;
//            songsActivityUI.putTouchBack();
//            if (contentsDisplayed == PERSONAL_RECORDING_DISPLAYED) {
            if (!(((TextView) view).getCurrentTextColor() == getResources().getColor(R.color.gold))) {
                displayAllSongs();
                songsActivityUI.allSongsShow();
                songsActivityUI.showGenresAndSearch();
                popup.dismiss();
            }
        });
    }

    private void myRecordingsToDisplayListener() {
        popupView.findViewById(R.id.my_recordings).setOnClickListener(view -> {
//            songsActivityUI.removeTouch();
            if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("")) {
//                if (contentsDisplayed == ALL_SONGS_DISPLAYED) {
                if (!(((TextView) view).getCurrentTextColor() == getResources().getColor(R.color.gold))) {
                    AuthenticationDriver authenticationDriver = new AuthenticationDriver();
//                    if (recordingDB == null || (!recordingDB.getRecorderId().equals(authenticationDriver.getUserUid()))) {
//                        recordingDB = null;
                    getAllPersonalSongs();
//                    } else {
//                        displayPersonalSongs();
//                    }
                    ((TextView) view).setTextColor(getResources().getColor(R.color.gold));
                    ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(getResources().getColor(R.color.sing_up_hover));
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

    public void removeRecording() {
        recordAdapter.removeAt();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteractionPlay(Reocording item);

        void onListFragmentInteractionPlay(Recording item);

        void onListFragmentInteractionShare(Recording item);

        DatabaseSongsDB getSongs();

        void changeLanguage();

        void openSignUp();

        void alertUserToSignIn();

        void signOut();

        void openEmailIntent();

        void openAdminSide();

        void colorNextGenre();

        void colorPreviousGenre();

        void onListFragmentInteractionDelete(Recording mItem);
    }
}
