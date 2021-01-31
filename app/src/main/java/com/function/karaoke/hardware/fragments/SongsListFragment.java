package com.function.karaoke.hardware.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCaller;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.hardware.GridAdapter;
import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.RecordingCategoryAdapter;
import com.function.karaoke.hardware.RecordingRecycleViewAdapter;
import com.function.karaoke.hardware.SongRecyclerViewAdapter;
import com.function.karaoke.hardware.SongsActivity;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.DatabaseSongsDB;
import com.function.karaoke.hardware.activities.Model.Genres;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.RecordingDB;
import com.function.karaoke.hardware.activities.Model.Reocording;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.storage.UserService;
import com.function.karaoke.hardware.ui.GenresUI;
import com.function.karaoke.hardware.ui.SettingUI;
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
public class SongsListFragment extends Fragment implements DatabaseSongsDB.IListener,
        ActivityResultCaller, GenresUI.GenreUIListener {

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

    private GridView gridView;
    private GridAdapter gAdapter;
    private boolean noTextInQuery = true;
    private RecordingCategoryAdapter recordingCategoryAdapter;
    private UserService userService;
    private String currentGenre;
    private final GenreListener genreListener = new GenreListener();


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColumnCount = 2;
//        if (getArguments() != null) {
//            mColumnCount = 2;
//        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        songsView = inflater.inflate(R.layout.fragment_song_list, container, false);
        authenticationDriver = new AuthenticationDriver();
        Context context = songsView.getContext();
        recyclerView = songsView.findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        recyclerView.setAdapter(mAdapter);
        addSearchListener();
        this.databaseDriver = new DatabaseDriver();
        this.recordingService = new RecordingService();
        setClickListeners(songsView);
        view = songsView;
        songsActivityUI = new SongsActivityUI(view, this, getCurrentLanguage(), getContext());
        addGenres();
        view.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()));
        addGenreListeners();
        return songsView;
    }

    private void addGenreListeners() {
        view.findViewById(R.id.genre_button).setOnClickListener(genreListener);
        view.findViewById(R.id.genre_holder).setOnClickListener(genreListener);
        view.findViewById(R.id.genre).setOnClickListener(genreListener);
    }

    private String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private void addGenres() {
        final Observer<Genres> searchObserver = products -> {
            addToGenres(products);
            if (genres != null)
//                songsActivityUI.addGenresToScreen(genres);
                if (currentGenre == null) {
                    currentGenre = genres.getGenres().get(0);
                    songsActivityUI.addGenreToScreen(genres.getGenres().get(0));
                }
        };
        this.databaseDriver.getAllGenresInCollection().observe(getViewLifecycleOwner(), searchObserver);

    }

    private void addToGenres(Genres products) {
        if (genres == null) {
            genres = products;
        } else {
            genres.add(products);
        }
    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private void addRecyclerViewGenreListener() {
////        recyclerView.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
////            @Override
////            public void onSwipeRight() {
////                super.onSwipeRight();
////                mListener.colorNextGenre();
////            }
////
////            @Override
////            public void onSwipeLeft() {
////                super.onSwipeLeft();
////                mListener.colorPreviousGenre();
////            }
////        });
//        recyclerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent e) {
//                recyclerView.onTouchEvent(e);
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        x1 = e.getX();
//                        y1 = e.getY();
//                        recyclerView.onScrollStateChanged(RecyclerView.SCROLL_STATE_DRAGGING);
////                if (mListener != null)
////                {
////                    mListener.OnItemClick(v, Position);
////                }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        recyclerView.onScrollStateChanged(RecyclerView.SCROLL_STATE_SETTLING);
//                        float x2 = e.getX();
//                        float y2 = e.getY();
//                        float diffY = y2 - y1;
//                        float diffX = x2 - x1;
//                        if (Math.abs(diffX) > Math.abs(diffY)) {
//                            float deltaX = x2 - x1;
//                            if (Math.abs(deltaX) > 5) {
//                                // Left to Right swipe action
//                                if (x2 > x1) {
//                                    if (getCurrentLanguage().equalsIgnoreCase("iw"))
//                                        colorNextGenre();
//                                    else
//                                        colorPreviousGenre();
//
//                                }
//
//                                // Right to left swipe action
//                                else {
//                                    if (getCurrentLanguage().equalsIgnoreCase("iw"))
//                                        colorPreviousGenre();
//                                    else
//                                        colorNextGenre();
//
//                                }
//                            }
//                            break;
//                        }
//                }
//                return true;
//            }
//        });
//    }
//
//    public void colorPreviousGenre() {
//        if (genreClicked > 0) {
////            songsActivityUI.colorNextGenre(genreClicked - 1);
//            songsActivityUI.addGenreToScreen(genres.getGenres().get(genreClicked - 1));
//            getAllSongsFromGenre(genreClicked - 1);
//        }
//    }
//
//    public void colorNextGenre() {
//        if (genreClicked < genres.getGenres().size()) {
////            songsActivityUI.colorNextGenre(genreClicked + 1);
//            songsActivityUI.addGenreToScreen(genres.getGenres().get(genreClicked + 1));
//            getAllSongsFromGenre(genreClicked + 1);
//        }
//    }

    @Override
    public void getAllSongsFromGenre(int i) {
        List<DatabaseSong> searchedSongs = new ArrayList<>();
        genreClicked = i;
        currentGenre = genres.getGenres().get(i);
        String hebrewGenre = currentGenre.split(",")[1];
        if (hebrewGenre.equals("כל השירים"))
            searchedSongs = allSongsDatabase.getSongs();
        else {
            for (DatabaseSong song : allSongsDatabase.getSongs()) {
                if (song.getGenre().contains(",")) {
                    String[] split = song.getGenre().split(",");
                    for (String g : split) {
                        if (g.equalsIgnoreCase(hebrewGenre))
                            searchedSongs.add(song);
                    }

                } else if (song.getGenre().toLowerCase().equals(hebrewGenre)) {
                    searchedSongs.add(song);
                }
            }
        }
        songsActivityUI.addGenreToScreen(currentGenre);
        currentDatabaseSongs.updateSongs(searchedSongs);
//        mAdapter.notifyDataSetChanged();
//        gAdapter.notifyDataSetChanged();
    }


    private void setClickListeners(View songsView) {

//        songsView.findViewById(R.id.open_search).setOnClickListener(view -> {
//            songsActivityUI.openSearchBar(searchOpened);
//            searchOpened = !searchOpened;
//        });

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
//        gAdapter = new GridAdapter(getContext(), currentDatabaseSongs.getSongs(), mListener);
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
//            gridView.setAdapter(gAdapter);
//            gAdapter.add(songsDb.getSongs());
//            gAdapter.notifyDataSetChanged();
        }
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
                    view.findViewById(R.id.search_icon_and_words).setVisibility(View.INVISIBLE);
                    if (query.length() > previousQuery.length()) {
                        addCopyOfSongsDBToList(currentDatabaseSongs);
                        getSongsSearchedFor(query.toLowerCase());
                    } else {
                        currentDatabaseSongs.updateSongs(previousSongs.get(previousSongs.size() - 1).getSongs());
                        previousSongs.remove(previousSongs.size() - 1);
                    }
                    mAdapter.notifyDataSetChanged();
//                    gAdapter.notifyDataSetChanged();
                    previousQuery = query;
                } else {
                    view.findViewById(R.id.search_icon_and_words).setVisibility(View.VISIBLE);
                    if (previousSongs.size() != 0) {
                        currentDatabaseSongs.updateSongs(previousSongs.get(0).getSongs());
                        mAdapter.notifyDataSetChanged();
//                        gAdapter.notifyDataSetChanged();
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

        SettingUI settingUI = new SettingUI(this.view, getContext());
        settingUI.openSettingsPopup(authenticationDriver.isSignIn()
                && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals(""), contentDisplayed
        );
        if (authenticationDriver.isSignIn()
                && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("")) {
            settingUI.setEmailAddressIfSignedIn(authenticationDriver.getUserEmail());
            settingUI.addPicToScreen(mListener.getUser());
        }
        popupView = settingUI.getPopupView();
        popup = settingUI.getPopup();
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
        addSongListener();
    }

    private void addSongListener() {
//        popupView.findViewById(R.id.song_suggestion).setOnClickListener(view -> showSongSuggestionBox());
    }

    public void showSongSuggestionBox() {
        View suggestionView = songsActivityUI.openSongSuggestionsPopup();
        suggestionView.findViewById(R.id.send_suggestion).setOnClickListener((View.OnClickListener) view -> {
            String songName = (String) ((EditText) suggestionView.findViewById(R.id.song_name)).getText().toString();
            String artistName = (String) ((EditText) suggestionView.findViewById(R.id.artist_name)).getText().toString();
            mListener.sendEmailWithSongSuggestion(songName, artistName);
//                mListener.sendSuggestion();
        });
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
        popupView.findViewById(R.id.contact_us).setOnClickListener(view -> mListener.openEmailIntent());
    }

    private void signInButtonListener() {
        popupView.findViewById(R.id.sign_in_button).setOnClickListener(view -> {
            if (authenticationDriver.getUserUid() != null) {
                mListener.signOut();
            } else {
                mListener.openSignUp();
            }
            popup.dismiss();
        });
    }

    private void homeButtonListener() {
//        popupView.findViewById(R.id.home_button).setOnClickListener(view -> {
////            songsActivityUI.putTouchBack();
//            if (contentDisplayed == PERSONAL_RECORDING_DISPLAYED) {
//                contentDisplayed = ALL_SONGS_DISPLAYED;
////            if (!(((TextView) view).getCurrentTextColor() == getResources().getColor(R.color.gold))) {
//                displayAllSongs();
//                songsActivityUI.allSongsShow();
//                songsActivityUI.showGenresAndSearch();
//                popup.dismiss();
//            }
//        });
    }

    private void myRecordingsToDisplayListener() {
        popupView.findViewById(R.id.my_recordings).setOnClickListener(view -> {
//            songsActivityUI.removeTouch();
            openMyRecordings();
        });
    }

    public void openMyRecordings() {
        if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("")) {
            if (contentDisplayed == ALL_SONGS_DISPLAYED) {
                AuthenticationDriver authenticationDriver = new AuthenticationDriver();
                popup.dismiss();
                mListener.startRecordingsActivity(genres);
//                ((TextView) view).setTextColor(getResources().getColor(R.color.gold));
            }
        } else {
            mListener.alertUserToSignIn();

        }
    }

    private void dismissButtonListener() {
//        popupView.findViewById(R.id.close_popup).setOnClickListener(view -> popup.dismiss());
    }

    private void languageChangeListener() {
        popupView.findViewById(R.id.language_changer).setOnClickListener(view -> mListener.changeLanguage());
    }

//    public void removeRecording() {
//        recordAdapter.removeAt();
//    }
//
//    @Override
//    public void onListFragmentInteractionPlay(List<Recording> recordings) {
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
////        recordAdapter = new RecordingRecycleViewAdapter(recordings, mListener);
////        recyclerView.setAdapter(recordAdapter);
//    }

    private void showRecordings() {

    }

    private class GenreListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            songsActivityUI.addGenresToScreen(genres, currentGenre);
        }
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

        void onListFragmentInteractionDelete(Recording mItem);

        void sendEmailWithSongSuggestion(String songName, String artistName);

        void startRecordingsActivity(Genres genres);

        UserInfo getUser();
    }


}
