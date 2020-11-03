package com.function.karaoke.hardware.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.RecordingService;

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
        recyclerView = (RecyclerView) songsView.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(mAdapter);
        addSearchListener();
        addSearchListener();
        this.databaseDriver = new DatabaseDriver();
        this.recordingService = new RecordingService();
        setClickListeners(songsView);
        view = songsView;
        addGenres();
        return songsView;
    }

    private void addGenres() {
        final Observer<Genres> searchObserver = products -> {
            genres = products;
            if (genres != null)
                addGenresToScreen();
        };
        this.databaseDriver.getAllGenresInCollection().observe(getViewLifecycleOwner(), searchObserver);
    }

    private void addGenresToScreen() {
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.genres);
        List<String> currentLanguageGenres;
        if (((SongsActivity) getActivity()).language.equals("English"))
            currentLanguageGenres = genres.getEnglishGenres();
        else
            currentLanguageGenres = genres.getHebrewGenres();
        for (int i = 0; i < currentLanguageGenres.size(); i++) {
            TextView genre = setGenreBar(currentLanguageGenres, i);

            linearLayout.addView(genre);
        }
    }

    private TextView setGenreBar(List<String> currentLanguageGenres, int i) {
        String genre = currentLanguageGenres.get(i);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ArialUnicodeMS.ttf");
        TextView textView = (TextView) inflater.inflate(R.layout.genre_layout, null);
        textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textView.setTextColor(Color.BLACK);
        String textToDisplay = "   " + genre + "   |";
        textView.setText(textToDisplay);
//        textView.setTypeface(tf);
        if (i == 0) {
            setGenreClicked(textView);
            allSongsTextView = textView;
        }
        int finalI = i;
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (genreClicked == textView) {
                    getAllSongsFromGenre("כל השירים");
                    setTextOfClickedToBlack();
                    setGenreClicked(allSongsTextView);
                } else {
                    setTextOfClickedToBlack();
                    setGenreClicked(textView);
                    getAllSongsFromGenre(genres.getHebrewGenres().get(finalI));
                }

            }
        });
        return textView;
    }

    private void setTextOfClickedToBlack() {
        genreClicked.setTextColor(Color.BLACK);
    }

    private void setGenreClicked(TextView textView) {
        textView.setTextColor(getResources().getColor(R.color.gold, getContext().getTheme()));
        genreClicked = textView;
    }

    private void getAllSongsFromGenre(String genre) {
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
                openSearch();
            }
        });

        songsView.findViewById(R.id.settings_button).setOnClickListener(view -> {
            openSettingsPopup(view);
        });
    }

    private void openSearch() {
        if (searchOpened)
            getView().findViewById(R.id.search_input).setVisibility(View.GONE);

        else
            getView().findViewById(R.id.search_input).setVisibility(View.VISIBLE);
        searchOpened = !searchOpened;
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
                displayPersonalSongs();
                view.findViewById(R.id.no_recordings_text).setVisibility(View.INVISIBLE);
            } else {
                updateUINoRecordings();
            }

        };
        this.recordingService.getRecordingFromUID().observe(getViewLifecycleOwner(), personalRecordingObserver);
    }

    private void updateUINoRecordings() {
        view.findViewById(R.id.no_recordings_text).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.no_recordings_text)).setText("Sorry, you have no recording");
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

    private void displayPersonalSongs() {
        RecordingRecycleViewAdapter recordAdapter = new RecordingRecycleViewAdapter(recordingDB.getRecordings(), mListener, ((SongsActivity) requireActivity()).language);
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
        RelativeLayout viewGroup = view.findViewById(R.id.settings_popup);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.settings_popup, viewGroup);
        addPopupListeners();
        //todo set the button that is pressed either the recordings or home
        if (contentsDisplayed == PERSONAL_RECORDING_DISPLAYED) {
            ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(getResources().getColor(R.color.gold, getContext().getTheme()));
        } else
            ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(getResources().getColor(R.color.gold, getContext().getTheme()));
        setSignInOrOut();

        placePopupOnScreen();
        //todo get dim to work with view that returns correct object
        popup.setOnDismissListener(() -> undimBackground());
        applyDim();

    }

    private void setSignInOrOut() {
        authenticationDriver = new AuthenticationDriver();
        TextView signInOrOutButton = ((TextView) popupView.findViewById(R.id.sign_in_button));
        if (authenticationDriver.getUserUid() != null)
            signInOrOutButton.setText(getResources().getText(R.string.sign_out));

        signInOrOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (authenticationDriver.getUserUid() != null) {
                    mListener.openSignUp();
                    signInOrOutButton.setText(getResources().getText(R.string.sign_in));
                } else {
                    mListener.openSignUp();
                    signInOrOutButton.setText(getResources().getText(R.string.sign_out));
                }
            }
        });
    }

    private void addPopupListeners() {
        popupView.findViewById(R.id.language_changer).setOnClickListener(view -> mListener.changeLanguage());
        popupView.findViewById(R.id.close_popup).setOnClickListener(view -> popup.dismiss());
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
                }
            } else {
                mListener.alertUserToSignIn();
            }
        });

        popupView.findViewById(R.id.home_button).setOnClickListener(view -> {
            if (contentsDisplayed == PERSONAL_RECORDING_DISPLAYED) {
                contentsDisplayed = ALL_SONGS_DISPLAYED;
                displayAllSongs();
                ((TextView) view).setTextColor(getResources().getColor(R.color.gold, getContext().getTheme()));
                ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(getResources().getColor(R.color.sing_up_hover, getContext().getTheme()));
            }
        });
    }

    private void placePopupOnScreen() {
        popup = new PopupWindow(getActivity());
        setPopupAttributes(popup, popupView);
        popup.showAtLocation(popupView, Gravity.START, 0, 0);
    }

    private void applyDim() {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.5));
        ViewOverlay overlay = view.getOverlay();
//        ViewOverlay headerOverlay = headerView.getOverlay();
//        headerOverlay.add(dim);
        overlay.add(dim);
    }

    public void undimBackground() {
        ViewOverlay overlay = view.getOverlay();
//        ViewOverlay headerOverlay = headerView.getOverlay();
        overlay.clear();
//        headerOverlay.clear();
    }

    private void setPopupAttributes(PopupWindow popup, View layout) {
        int width = (int) (this.getResources().getDisplayMetrics().widthPixels * 0.719);
        int height = (int) (this.getResources().getDisplayMetrics().heightPixels);
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(DatabaseSong item);

        void onListFragmentInteraction(Recording item);

        DatabaseSongsDB getSongs();

        void changeLanguage();

        void openSignUp();

        void alertUserToSignIn();
    }
}
