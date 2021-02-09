package com.function.karaoke.hardware.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultCaller;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.DatabaseSongsDB;
import com.function.karaoke.hardware.activities.Model.Genres;
import com.function.karaoke.hardware.activities.Model.RecordingDB;
import com.function.karaoke.hardware.activities.Model.Reocording;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.adapters.SongRecyclerViewAdapter;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.RecordingService;
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

    private static final int ALL_SONGS_DISPLAYED = 1;
    private static final int SONG_SUGGESTION = 102;

    private static final int GENRE = -1;
    private final GenreListener genreListener = new GenreListener();
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
    private Genres genres;
    private View view;
    private DatabaseSongsDB allSongsDatabase = new DatabaseSongsDB();
    private int genreClicked = 0;
    private AuthenticationDriver authenticationDriver;
    private SongsActivityUI songsActivityUI;
    private int clicked = 0;
    private final boolean differentSongsDisplayed = true;
    private final int contentDisplayed = ALL_SONGS_DISPLAYED;
    private OpenSignUp openSignUpListener = new OpenSignUp();
    private String currentGenre;
    private CountDownTimer cTimer;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        loadLocale();
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
        songsActivityUI = new SongsActivityUI(view, this, loadLocale(), getContext());
        addGenres();
        view.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()));
        addGenreListeners();
        return songsView;
    }

    public String loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getContext().getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        if (prefs != null) {
            String language = prefs.getString(langPref, "");
            if (language != null && !language.equalsIgnoreCase(""))
                return language;
        }
        return Locale.getDefault().getLanguage();
    }


    private void addGenreListeners() {
        view.findViewById(R.id.genre_button).setOnClickListener(genreListener);
        view.findViewById(R.id.genre_holder).setOnClickListener(genreListener);
        view.findViewById(R.id.genre).setOnClickListener(genreListener);
    }

    private void addGenres() {
        final Observer<Genres> searchObserver = products -> {
            addToGenres(products);
            if (genres != null)
//                songsActivityUI.addGenresToScreen(genres);
                setupInitialGenreState();
        };
        this.databaseDriver.getAllGenresInCollection().observe(getViewLifecycleOwner(), searchObserver);

    }

    private void setupInitialGenreState() {
        if (currentGenre == null) {
            currentGenre = genres.getGenres().get(0);
            songsActivityUI.addGenreToScreen(genres.getGenres().get(0));
        }
    }

    private void addToGenres(Genres products) {
        if (genres == null) {
            genres = products;
        } else {
            genres.add(products);
        }
    }

    @Override
    public void getAllSongsFromGenre(int i) {
        songsActivityUI.closePopup();
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
        mAdapter = new SongRecyclerViewAdapter(currentDatabaseSongs.getSongs(), mListener);
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
            double averageSongsPlayed = songsDb.getAverageSongsPlayed();
            recyclerView.setAdapter(mAdapter);
            mAdapter.setAverage(averageSongsPlayed);
            mAdapter.setData(songsDb.getSongs());
            mAdapter.notifyDataSetChanged();
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
        boolean userIsSignedIn = authenticationDriver.isSignIn()
                && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("");
        settingUI.openSettingsPopup(userIsSignedIn, mListener.getUser()
        );
        popupView = settingUI.getPopupView();
        popup = settingUI.getPopup();
        addPopupListeners();
        if (!userIsSignedIn)
            setOpenGmailClickers();
        popupView.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()) {
            public void onSwipeTop() {
                popup.dismiss();

            }

            public void onSwipeRight() {
            }

            public void onSwipeLeft() {
            }

            public void onSwipeBottom() {
            }

        });

    }

    private void setOpenGmailClickers() {
        popupView.findViewById(R.id.sign_in_invite).setOnClickListener(openSignUpListener);
        popupView.findViewById(R.id.user_picture).setOnClickListener(openSignUpListener);
    }

    private void addPopupListeners() {
        languageChangeListener();
        myRecordingsToDisplayListener();
        signInButtonListener();
        contactUsListener();
        policyListener();
        couponListener();
    }

    private void couponListener() {
        if (userIsSignedIn())
            popupView.findViewById(R.id.coupons).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userIsSignedIn())
                        mListener.startCouponActivity();
                }
            });
        else
            mListener.alertUserToSignIn();
    }

    public void showSongSuggestionBox() {
        songsActivityUI.addGenreToScreen(getActivity().getResources().getString(R.string.song_suggestion));
        View suggestionView = songsActivityUI.openSongSuggestionsPopup();
        String previouseGenre = currentGenre;
        currentGenre = getActivity().getResources().getString(R.string.song_suggestion);
        suggestionView.findViewById(R.id.send_suggestion).setOnClickListener((View.OnClickListener) view -> {
            String songName = (String) ((EditText) suggestionView.findViewById(R.id.song_name)).getText().toString();
            String artistName = (String) ((EditText) suggestionView.findViewById(R.id.artist_name)).getText().toString();
            String comments = (String) ((EditText) suggestionView.findViewById(R.id.comments)).getText().toString();
            if (!allSongsDatabase.containsSong(songName, artistName)) {
                if (!songName.equalsIgnoreCase("") && !artistName.equalsIgnoreCase(""))
                    mListener.sendEmailWithSongSuggestion(songName, artistName, comments);
            } else {
                songsActivityUI.showSongInSystem();
                startTimerToCloseWindow();
            }

        });
        songsActivityUI.getSuggestPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                currentGenre = previouseGenre;
                songsActivityUI.addGenreToScreen(currentGenre);
            }
        });
    }

    private void startTimerToCloseWindow() {
        cTimer = new CountDownTimer(2000, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                songsActivityUI.makeTextInvisible();
                cancelTimer();
            }
        };
        cTimer.start();

    }

    private void cancelTimer() {
        cTimer.cancel();
        cTimer = null;
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
        popupView.findViewById(R.id.sign_out_button).setOnClickListener(view -> {
            if (authenticationDriver.getUserUid() != null) {
                mListener.signOut();
            } else {
                mListener.openSignUp();
            }
            popup.dismiss();
        });
    }

    private void myRecordingsToDisplayListener() {
        popupView.findViewById(R.id.my_recordings).setOnClickListener(view -> {
//            songsActivityUI.removeTouch();
            openMyRecordings();
        });
    }

    public void openMyRecordings() {
        if (userIsSignedIn()) {
            AuthenticationDriver authenticationDriver = new AuthenticationDriver();
            if (popup != null)
                popup.dismiss();
            restoreScreenToSongsState();
            mListener.startRecordingsActivity(genres);
//                ((TextView) view).setTextColor(getResources().getColor(R.color.gold));
        } else {
            mListener.alertUserToSignIn();

        }
    }

    private boolean userIsSignedIn() {
        return authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("");
    }

    private void languageChangeListener() {
        popupView.findViewById(R.id.language_changer).setOnClickListener(view -> mListener.changeLanguage());
    }

    public boolean backPressed() {
        return restoreScreenToSongsState();
    }

    private boolean restoreScreenToSongsState() {
        if (songsActivityUI.closePopup()) {
            setupInitialGenreState();
            getAllSongsFromGenre(0);
            return true;
        }
        return false;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteractionPlay(Reocording item);

        DatabaseSongsDB getSongs();

        void changeLanguage();

        void openSignUp();

        void alertUserToSignIn();

        void signOut();

        void openEmailIntent();

        void openAdminSide();

        void sendEmailWithSongSuggestion(String songName, String artistName, String comments);

        void startRecordingsActivity(Genres genres);

        UserInfo getUser();

        void signIn();

        void startCouponActivity();
    }

    private class GenreListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (genres != null)
                songsActivityUI.addGenresToScreen(genres, currentGenre, genres.getGenres().contains(currentGenre) ? GENRE : SONG_SUGGESTION);
        }
    }

    public class OpenSignUp implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mListener.signIn();
            if (popup != null)
                popup.dismiss();
        }
    }


}
