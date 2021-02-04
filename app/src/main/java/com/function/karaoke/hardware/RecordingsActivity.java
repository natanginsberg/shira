package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.core.utility.BlurBuilder;
import com.function.karaoke.hardware.activities.Model.Genres;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.RecordingDB;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.activities.Model.enums.RecordingsScreenState;
import com.function.karaoke.hardware.adapters.RecordingCategoryAdapter;
import com.function.karaoke.hardware.adapters.RecordingRecycleViewAdapter;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.RecordingDelete;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.ui.GenresUI;
import com.function.karaoke.hardware.ui.SettingUI;
import com.function.karaoke.hardware.ui.ShareOptionsUI;
import com.function.karaoke.hardware.ui.SingActivityUI;
import com.function.karaoke.hardware.utils.static_classes.Converter;
import com.function.karaoke.hardware.utils.static_classes.GenerateRandomId;
import com.function.karaoke.hardware.utils.static_classes.OnSwipeTouchListener;
import com.function.karaoke.hardware.utils.static_classes.ShareLink;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class RecordingsActivity extends AppCompatActivity implements
        RecordingCategoryAdapter.RecordingSongListener, RecordingRecycleViewAdapter.RecordingListener, GenresUI.GenreUIListener {

    private static final String FEEDBACK_EMAIL = "ashira.jewishkaraoke@gmail.com";
    private static final int MY_RECORDINGS = 101;

    private static final int NUM_COLUMNS = 2;
    private static final String USER_INFO = "User";
    private static final String GENRES = "genres";
    private final List<Recording> deleteRecordingList = new ArrayList<Recording>() {
        @Override
        public boolean contains(@Nullable Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public int indexOf(@Nullable Object o) {
            if (o != null) {
                if (o instanceof Recording) {
                    Recording rec = (Recording) o;
                    for (int i = 0; i < deleteRecordingList.size(); i++)
                        if (rec.getDate().equals(deleteRecordingList.get(i).getDate()))
                            return i;
                }
            }
            return -1;
        }
    };
    private final GenreListener genreListener = new GenreListener();
    private RecordingService recordingService;
    private RecordingCategoryAdapter recordingCategoryAdapter;
    private RecyclerView recyclerView;
    private RecordingsScreenState recordingState;
    private RecordingDelete recordingDelete;
    private RecordingRecycleViewAdapter recordAdapter;
    private String previousQuery = "";
    private ArrayList<RecordingDB> previousRecordings = new ArrayList<>();
    private RecordingDB currentDatabaseRecordings;
    private boolean deleteOpen = false;
    private List<Recording> currentRecordings = new ArrayList<Recording>() {
        @Override
        public boolean contains(@Nullable Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public int indexOf(@Nullable Object o) {
            if (o != null) {
                if (o instanceof Recording) {
                    Recording rec = (Recording) o;
                    for (int i = 0; i < currentRecordings.size(); i++)
                        if (rec.getDate().equals(currentRecordings.get(i).getDate()))
                            return i;
                }
            }
            return -1;
        }
    };
    private View popupView;
    private PopupWindow popup;
    private AuthenticationDriver authenticationDriver;
    private Locale myLocale;
    private String language;
    private UserInfo user;
    private Genres genres;
    private GenresUI genresUI;
    private ShareOptionsUI sharingUI;
    private Recording recordingToShare;
    private String password;
    private String link1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        getUser();
        getGenres();
        setContentView(R.layout.activity_recordings);
        genresUI = new GenresUI(findViewById(android.R.id.content).getRootView(), this, language, this);
        sharingUI = new ShareOptionsUI(findViewById(android.R.id.content).getRootView(), user);
        recordingService = new RecordingService();
        recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
        recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
        setRecordingsObserver();
        addSearchListener();
        addGenres();
        addGenreListeners();
    }

    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        if (prefs != null) {
            String language = prefs.getString(langPref, "");
            if (language != null && !language.equalsIgnoreCase("")) {
                this.language = language;
                setLocale(language);
            }
        }
    }

    private void addGenreListeners() {
        findViewById(R.id.genre_button).setOnClickListener(genreListener);
        findViewById(R.id.genre_holder).setOnClickListener(genreListener);
        findViewById(R.id.genre).setOnClickListener(genreListener);
    }

    private void addGenres() {
        genresUI.addGenreToScreen(getResources().getString(R.string.my_recordings));
    }

    private void getGenres() {
        if (getIntent().getExtras().containsKey(GENRES)) {
            genres = (Genres) getIntent().getSerializableExtra(GENRES);
        }
    }

    private void addProfilePic() {
        ImageView profilePic = findViewById(R.id.user_picture);
        if (user.getPicUrl() != null && !user.getPicUrl().equalsIgnoreCase(""))
            Picasso.get()
                    .load(user.getPicUrl())
                    .placeholder(R.drawable.circle)
                    .fit()
                    .transform(new CropCircleTransformation())
                    .into(profilePic);
    }

    private void getUser() {
        if (getIntent().getExtras().containsKey(USER_INFO)) {
            user = (UserInfo) getIntent().getSerializableExtra(USER_INFO);
        }
    }

    private void getExtras(Bundle savedInstanceState) {

    }

    private void addSearchListener() {
        SearchView searchView = findViewById(R.id.search_input);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (recordingState == RecordingsScreenState.SINGLE_SONG_RECORDINGS)
                    onBackPressed();
                if (query.length() >= 1) {
                    if (query.length() > previousQuery.length()) {
                        addCopyOfSongsDBToList(currentDatabaseRecordings);
                        getSongsSearchedFor(query.toLowerCase());
                    } else {
                        currentDatabaseRecordings.updateRecordings(previousRecordings.get(previousRecordings.size() - 1).getRecordings());
                        previousRecordings.remove(previousRecordings.size() - 1);
                    }
                    recordingCategoryAdapter.setData(currentDatabaseRecordings.getRecordingsPerSong());
                    recordingCategoryAdapter.notifyDataSetChanged();
                    previousQuery = query;
                } else {
                    if (previousRecordings.size() != 0) {
                        currentDatabaseRecordings.updateRecordings(previousRecordings.get(0).getRecordings());
                        recordingCategoryAdapter.notifyDataSetChanged();
                        previousRecordings = new ArrayList<>();
                        previousQuery = "";
                    }
                }
                return false;
            }
        });
    }

    private void getSongsSearchedFor(String query) {
        List<Recording> searchedRecordings = new ArrayList<>();
        for (Recording recording : currentDatabaseRecordings.getRecordings()) {
            if (recording.getTitle().toLowerCase().contains(query) || recording.getArtist().toLowerCase().contains(query)) {
                searchedRecordings.add(recording);

            }
        }
        currentDatabaseRecordings.updateRecordings(searchedRecordings);
    }

    private void addCopyOfSongsDBToList(RecordingDB recordings) {
        previousRecordings.add(new RecordingDB(recordings.getRecordings()));
    }

    private void setRecordingsObserver() {
        final Observer<List<Recording>> personalRecordingObserver = personalRecordings -> {
            if (personalRecordings != null) {
                currentDatabaseRecordings = new RecordingDB(personalRecordings);
                findViewById(R.id.loading_songs_progress_bar).setVisibility(View.INVISIBLE);
                findViewById(R.id.no_recordings_text).setVisibility(View.INVISIBLE);
                displayRecordingSongs();
                addProfilePic();
            } else
                findViewById(R.id.no_recordings_text).setVisibility(View.VISIBLE);
        };
        this.recordingService.getRecordingFromUID().observe(this, personalRecordingObserver);

    }

    private void displayRecordingSongs() {
        findViewById(R.id.song_info).setVisibility(View.GONE);
        recordingCategoryAdapter = new RecordingCategoryAdapter(currentDatabaseRecordings.getRecordingsPerSong(), this, getString(R.string.recording_tag_display_constant));
        recyclerView.setAdapter(recordingCategoryAdapter);
    }


    public void back(View view) {
        if (recordingState == RecordingsScreenState.RECORDING_SONGS_DISPLAYED)
            finish();
        else {
            recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
            recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
            displayRecordingSongs();
        }
    }

    @Override
    public void onBackPressed() {
        if (recordingState == RecordingsScreenState.RECORDING_SONGS_DISPLAYED)
            finish();
        else {
            currentDatabaseRecordings.updateRecordings(currentRecordings);
            recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
            recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
            displayRecordingSongs();
        }
    }

    @Override
    public void onListFragmentInteractionPlay(List<Recording> recordings) {
        recordingState = RecordingsScreenState.SINGLE_SONG_RECORDINGS;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currentRecordings = recordings;
        recordAdapter = new RecordingRecycleViewAdapter(recordings, this, this);
        recyclerView.setAdapter(recordAdapter);
        setSongInfo(recordings.get(0).getArtist(), recordings.get(0).getTitle(), recordings.get(0).getImageResourceFile());
        resetFields();

    }

    private void resetFields() {
        if (deleteRecordingList != null)
            deleteRecordingList.clear();
        changeMainIconBackToGray();
        deleteOpen = false;
    }

    private void setSongInfo(String artist, String title, String albumResource) {
        findViewById(R.id.song_info).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.artist_name)).setText(artist);
        ((TextView) findViewById(R.id.song_name)).setText(title);
        ShapeableImageView mCover = findViewById(R.id.recording_album_pic);
        if (!albumResource.equals("")) {
            Picasso.get()
                    .load(albumResource)
                    .placeholder(R.drawable.plain_rec)
                    .fit()
                    .into(mCover);
        }
        mCover.setShapeAppearanceModel(mCover.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, Converter.convertDpToPx(10))
                .build());
    }

    @Override
    public void onListFragmentInteractionRecordingClick(Recording mItem, View mView) {
        if (deleteOpen)
            deletePressed(mItem, mView);
        else {
            Intent intent = new Intent(this, Playback.class);
            intent.putExtra(SingActivity.RECORDING, mItem);
            startActivity(intent);
        }
    }

    @Override
    public void onListFragmentInteractionShare(Recording item) {
        recordingToShare = item;
        sharingUI.openShareOptions(this, new SingActivityUI.ShareListener() {
            @Override
            public void createShareLink(TextView viewById) {
                Task<ShortDynamicLink> link = ShareLink.createLink(recordingToShare, password);
                link.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult().getShortLink();
                        Uri flowchartLink = task.getResult().getPreviewLink();
                        link1 = shortLink.toString();
                        viewById.setText(link1);


                    } else {
                        showFailure();
                        // Error
                        // ...
                    }
                });
//        activityUI.hideShareItems();
            }

            @Override
            public void share(View view) {
                if (link1 != null) {
                    sendDataThroughIntent(link1);
                }
            }

            @Override
            public CharSequence getLink() {
                return link1;
            }

            @Override
            public void setPassword(TextView viewById) {
                password = GenerateRandomId.generateRandomPassword();
                viewById.setText(password);
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeIconToWhite(View itemView) {
        itemView.findViewById(R.id.delete_button).setBackground(getDrawable(R.drawable.outline_circle));
        itemView.findViewById(R.id.trash).setBackground(getDrawable(R.drawable.ic_trash_icon_white));
        itemView.setBackground(getDrawable(R.drawable.unclicked_recording_background));
    }

    @Override
    public void showAllGarbagesInChildren() {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            if (recyclerView.findViewHolderForAdapterPosition(i) != null) {
                recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
                recordAdapter.notifyItemChanged(i);
            }
        }
        recordAdapter.changeDeleteOpen(true);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void changeIconToGreen(View itemView) {
        itemView.findViewById(R.id.delete_button).setBackground(getDrawable(R.drawable.full_circle));
        itemView.findViewById(R.id.trash).setBackground(getDrawable(R.drawable.ic_trash_icon_grenn));
        itemView.setBackground(getDrawable(R.drawable.clicked_recording_background));
    }

    @Override
    public void deletePressed(Recording mItem, View itemView) {
        if (deleteRecordingList.contains(mItem)) {
            deleteRecordingList.remove(mItem);
            changeIconToWhite(itemView);
        } else {
            deleteRecordingList.add(mItem);
            changeIconToGreen(itemView);
        }
        recordAdapter.updateDeleteList(deleteRecordingList);
        changeMainIconToColor();
    }

    private void deleteRecordings() {
        NetworkTasks.deleteFromWasabi(recordingDelete, new NetworkTasks.DeleteListener() {
            @Override
            public void onSuccess() {
                showSuccessToast();
                recordAdapter.removeDeletions();
            }

            @Override
            public void onFail() {
            }
        });
    }

    private void showSuccessToast() {
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
    }

    private void sendDataThroughIntent(String link) {
        String data = getString(R.string.email_prompt) + link;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(
                Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void showFailure() {
        Toast.makeText(this, getString(R.string.sharing_failed), Toast.LENGTH_SHORT).show();
    }

    public void trashClicked(View view) {
        if (deleteOpen) {
            changeMainIconBackToGray();
            closeAllOpenGarbages();
            hideDeleteButton();
            deleteRecordingList.clear();

        } else {
            showAllGarbagesInChildren();
            changeMainIconToColor();
            showDeleteButton();
        }
        deleteOpen = !deleteOpen;

    }

    private void showDeleteButton() {
        findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    private void hideDeleteButton() {
        findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeMainIconToColor() {
        findViewById(R.id.outer_trash).setBackground(getDrawable(R.drawable.full_circle_blue));
        findViewById(R.id.inner_trash).setBackground(getDrawable(R.drawable.ic_trash_icon_white));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void closeAllOpenGarbages() {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            if (recyclerView.findViewHolderForAdapterPosition(i) != null) {
                ConstraintLayout view = (ConstraintLayout) Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(i)).itemView;
                view.findViewById(R.id.delete_button).setVisibility(View.GONE);
                recordAdapter.notifyItemChanged(i);
            }
        }
        recordAdapter.changeDeleteOpen(false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeMainIconBackToGray() {
        findViewById(R.id.outer_trash).setBackground(getDrawable(R.drawable.outline_circle_grey));
        findViewById(R.id.inner_trash).setBackground(getDrawable(R.drawable.ic_trash_icon_grey));
    }

    private void deleteAllClickedRecordings() {
        if (deleteRecordingList.size() > 0) {
            recordAdapter.setRemoveInProgress();
            recordingDelete = new RecordingDelete(new RecordingDelete.SetupListener() {
                @Override
                public void setup() {
                    deleteRecordings();
                }
            }, deleteRecordingList);
            deleteRecordingsFromList();
        }
    }

    private void deleteRecordingsFromList() {
        currentRecordings.removeAll(deleteRecordingList);
        currentDatabaseRecordings.removeDeletedRecordings(deleteRecordingList);
        deleteRecordingList.clear();
    }

    public void openSettingsPopup(View view) {
        SettingUI settingUI = new SettingUI(findViewById(R.id.recordings_activty), this);
        authenticationDriver = new AuthenticationDriver();
        boolean userIsSignedIn = authenticationDriver.isSignIn()
                && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("");
        settingUI.openSettingsPopup(userIsSignedIn, user);
        popupView = settingUI.getPopupView();
        popup = settingUI.getPopup();
        addPopupListeners();
        popupView.setOnTouchListener(new OnSwipeTouchListener(this) {
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
        applyDim();

    }

    private void applyDim() {
        View view = findViewById(R.id.recordings_activty);
        ViewOverlay overlay = view.getOverlay();
        Drawable colorDim = new ColorDrawable(Color.WHITE);
        colorDim.setBounds(0, 0, view.getWidth(), view.getHeight());
        colorDim.setAlpha((int) (255 * (float) 0.7));
//
        Drawable dim = new BitmapDrawable(getResources(), BlurBuilder.blur(view));
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.7));
//        ViewOverlay headerOverlay = headerView.getOverlay();
//        headerOverlay.add(dim);
        overlay.add(colorDim);
        overlay.add(dim);
    }

    private void addPopupListeners() {
        languageChangeListener();
        myRecordingsToDisplayListener();
        signInButtonListener();
        contactUsListener();
        policyListener();
    }

    private void policyListener() {
        //todo open policy
    }

    private void contactUsListener() {
        popupView.findViewById(R.id.contact_us).setOnClickListener(view -> openEmailIntent());
    }

    public void openEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));
        intent.setData(Uri.parse("mailto:"));

        startActivity(intent);
    }

    private void signInButtonListener() {
        popupView.findViewById(R.id.sign_out_button).setOnClickListener(view -> {
            authenticationDriver.signOut();
            popup.dismiss();
            finish();
        });
    }

    private void endActivity() {

    }

    private void myRecordingsToDisplayListener() {
        popupView.findViewById(R.id.my_recordings).setOnClickListener(view -> {
            startRecordingsActivity();
        });
    }

    private void startRecordingsActivity() {
        recyclerView.scrollTo(0, 0);
        popup.dismiss();
    }

    private void dismissButtonListener() {

    }

    private void languageChangeListener() {
        popupView.findViewById(R.id.language_changer).setOnClickListener(view -> changeLanguage());
    }

    public void changeLanguage() {
        if (language.equals("en")) {
            setLocale("iw");
        } else {
            setLocale("en");
        }
        Intent refresh = new Intent(this, SongsActivity.class);
        startActivity(refresh);
    }

    private void setLocale(String lang) {
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        saveLocale(lang);
        res.updateConfiguration(conf, dm);
    }

    public void saveLocale(String lang) {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }

    @Override
    public void getAllSongsFromGenre(int i) {
        Intent intent = new Intent(this, SongsActivity.class);
        intent.putExtra("genre", i);
//            onActivityResult(0, RESULT_OK, intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void showSongSuggestionBox() {
        Intent intent = new Intent(this, SongsActivity.class);
        intent.putExtra("suggestion", true);
//            onActivityResult(0, RESULT_OK, intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void openMyRecordings() {
        //this is the recordings no need to do anything
    }

    public void deleteClicked(View view) {
        deleteAllClickedRecordings();
        trashClicked(view);
    }

    private class GenreListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (genres != null)
                genresUI.addGenresToScreen(genres, getResources().getString(R.string.my_recordings), MY_RECORDINGS);
        }
    }
}