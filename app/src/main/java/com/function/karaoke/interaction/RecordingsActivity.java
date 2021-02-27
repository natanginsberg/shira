package com.function.karaoke.interaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.function.karaoke.interaction.activities.Model.Genres;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.activities.Model.RecordingDB;
import com.function.karaoke.interaction.activities.Model.UserInfo;
import com.function.karaoke.interaction.activities.Model.enums.RecordingsScreenState;
import com.function.karaoke.interaction.adapters.RecordingCategoryAdapter;
import com.function.karaoke.interaction.adapters.RecordingRecycleViewAdapter;
import com.function.karaoke.interaction.storage.AuthenticationDriver;
import com.function.karaoke.interaction.storage.RecordingDelete;
import com.function.karaoke.interaction.storage.RecordingService;
import com.function.karaoke.interaction.tasks.NetworkTasks;
import com.function.karaoke.interaction.ui.GenresUI;
import com.function.karaoke.interaction.ui.SettingUI;
import com.function.karaoke.interaction.ui.ShareOptionsUI;
import com.function.karaoke.interaction.ui.SingActivityUI;
import com.function.karaoke.interaction.utils.static_classes.Converter;
import com.function.karaoke.interaction.utils.static_classes.GenerateRandomId;
import com.function.karaoke.interaction.utils.static_classes.OnSwipeTouchListener;
import com.function.karaoke.interaction.utils.static_classes.ShareLink;
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
        RecordingCategoryAdapter.RecordingSongListener, RecordingRecycleViewAdapter.RecordingListener, GenresUI.GenreUIListener, RecordingDB.IListener {

    private static final String FEEDBACK_EMAIL = "ashira.jewishkaraoke@gmail.com";
    private static final int MY_RECORDINGS = 101;

    private static final int NUM_COLUMNS = 2;
    private static final String USER_INFO = "User";
    private static final String GENRES = "genres";
    private static final String PRIVACY_POLICY = "privacy policy";
    private static final String TERMS_OF_USE = "terms of use";
    private static final String COUPON_PAGE = "coupon";
    private static final String POLICY_PAGE = "policy";
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
    private String link1;
    private Recording recordingsDisplayed;
    private View songUploadedView;
    private LinearLayout loadingText;
    private final List<Recording> recordingsBeingUploaded = new ArrayList<>();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            double content = intent.getDoubleExtra("content", 0.0);
            Recording recording = (Recording) intent.getSerializableExtra("recording");
            addRecordingToScreen(content, recording);

        }
    };

    private void addRecordingToScreen(double content, Recording recording) {
        if (recordingsDisplayed == null) {
            songUploadedView = createViewForLoading(content, recording);
            loadingText.addView(songUploadedView);
            recordingsDisplayed = recording;
            if (recordingsBeingUploaded.size() > 0) {
                removeRecordingFromRecordingsBeingUploaded(recording);
            }
        } else if (recording != null && recordingsDisplayed.getRecordingId().equals(recording.getRecordingId())) {
            addPercentLoaded(songUploadedView, content);
        } else if (recording != null) {
            addOntToTheUploads(recording);
        }
//            loadingText.setText((int) content + "%");
        if (recording != null && content >= 100) {
            if (recordingsDisplayed.getRecordingId().equals(recording.getRecordingId())) {
                recordingsDisplayed = null;
                loadingText.removeAllViews();
            }
        }
    }

    private void removeRecordingFromRecordingsBeingUploaded(Recording recording) {
        List<Recording> tempList = new ArrayList<>();
        for (Recording r : recordingsBeingUploaded) {
            if (!r.getRecordingId().equals(recording.getRecordingId()))
                tempList.add(r);
        }
        recordingsBeingUploaded.clear();
        recordingsBeingUploaded.addAll(tempList);
    }

    @SuppressLint("SetTextI18n")
    private void addOntToTheUploads(Recording recording) {
        for (Recording r : recordingsBeingUploaded) {
            if (r.getRecordingId().equals(recording.getRecordingId()))
                return;
        }
        recordingsBeingUploaded.add(recording);
        ((TextView) songUploadedView.findViewById(R.id.other_recordings_being_uploaded)).setText("+" + recordingsBeingUploaded.size());
    }

    private View createViewForLoading(double content, Recording recording) {
        View to_add = LayoutInflater.from(this).inflate(R.layout.recording_upload, null);
        addValuesToView(to_add, content, recording);
        return to_add;
    }

    private void addValuesToView(View view, double content, Recording recording) {
        ((TextView) view.findViewById(R.id.artist_name)).setText(recording.getArtist());
        ((TextView) view.findViewById(R.id.song_name)).setText(recording.getTitle());
        ShapeableImageView mCover = view.findViewById(R.id.recording_album_pic);
        if (!recording.getImageResourceFile().equals("")) {
            Picasso.get()
                    .load(recording.getImageResourceFile())
                    .placeholder(R.drawable.plain_rec)
                    .fit()
                    .into(mCover);
        }
        mCover.setShapeAppearanceModel(mCover.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, Converter.convertDpToPx(10))
                .build());
        addPercentLoaded(view, content);

    }

    @SuppressLint("SetTextI18n")
    private void addPercentLoaded(View view, double content) {
        ((TextView) view.findViewById(R.id.loading_percent_upload)).setText("Loading " + content + "%");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        getUser();
        getGenres();
        setContentView(R.layout.activity_recordings);
        genresUI = new GenresUI(findViewById(android.R.id.content).getRootView(), this, language, this);
        sharingUI = new ShareOptionsUI(findViewById(android.R.id.content).getRootView(), user, true);
        recordingService = new RecordingService();
        recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
        recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
        loadingText = (LinearLayout) (findViewById(R.id.loading_percent));
        setRecordingsObserver();
        addSearchListener();
        addGenres();
        addGenreListeners();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("changed"));
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
        if (language == null)
            language = Locale.getDefault().getLanguage();
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
        profilePic.setVisibility(View.VISIBLE);
        if (profilePic != null && user.getPicUrl() != null && !user.getPicUrl().equalsIgnoreCase(""))
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
                    if (currentDatabaseRecordings != null) {
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
        RecordingDB preSongs = new RecordingDB();
        preSongs.updateRecordings(recordings.getRecordings());
        previousRecordings.add(preSongs);
    }

    private void setRecordingsObserver() {
        final Observer<List<Recording>> personalRecordingObserver = personalRecordings -> {
            if (personalRecordings != null) {
                if (currentDatabaseRecordings == null) {
                    if (personalRecordings.size() > 0) {
                        currentDatabaseRecordings = new RecordingDB(personalRecordings);
                        currentDatabaseRecordings.subscribe(this);
                    } else {
                        findViewById(R.id.loading_songs_progress_bar).setVisibility(View.INVISIBLE);
                        findViewById(R.id.no_recordings_text).setVisibility(View.VISIBLE);
                    }
                } else {
                    currentDatabaseRecordings.addRecordings(personalRecordings);
                }
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
            currentDatabaseRecordings.changeSongsAfterDelete(currentRecordings);
            recordingState = RecordingsScreenState.RECORDING_SONGS_DISPLAYED;
            recyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
            displayRecordingSongs();
        }
    }

    @Override
    public void onListFragmentInteractionPlay(List<Recording> recordings) {
        if (recordings.size() > 0) {
            recordingState = RecordingsScreenState.SINGLE_SONG_RECORDINGS;
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            currentRecordings = recordings;
            recordAdapter = new RecordingRecycleViewAdapter(recordings, this, this);
            recyclerView.setAdapter(recordAdapter);
            setSongInfo(recordings.get(0).getArtist(), recordings.get(0).getTitle(), recordings.get(0).getImageResourceFile());
            resetFields();
        }
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
            public void share(View view, boolean video, String password) {
                createShareLink(video, password);
            }

            @Override
            public void setPassword(TextView viewById) {
                String password = GenerateRandomId.generateRandomPassword();
                viewById.setText(password);
            }
        });
        sharingUI.hideRecordingBeingSaved();
    }

    private void createShareLink(boolean video, String password) {
        link1 = null;
        Task<ShortDynamicLink> link = ShareLink.createLink(recordingToShare, password, video);
        link.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                Uri flowchartLink = task.getResult().getPreviewLink();
                link1 = shortLink.toString();
                sendDataThroughIntent(link1);


            } else {
                showFailure();
                // Error
                // ...
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
                deleteRecordingsFromList();
                recordAdapter.removeDeletions();
                trashClicked(findViewById(R.id.report));
            }

            @Override
            public void onFail() {
                trashClicked(findViewById(R.id.report));
            }
        });
    }

    private void showSuccessToast() {
        Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
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
//            deleteRecordingsFromList();
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
    }

    private void addPopupListeners() {
        languageChangeListener();
        myRecordingsToDisplayListener();
        signOutButtonListener();
        contactUsListener();
        policyListener();
        couponListener();

    }

    private void couponListener() {
        popupView.findViewById(R.id.coupons).setOnClickListener(view -> {
            openPage(COUPON_PAGE);
        });
    }

    private void openPage(String pageToOpen) {
        Intent intent = new Intent(this, SongsActivity.class);
        intent.putExtra(pageToOpen, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void policyListener() {
        popupView.findViewById(R.id.privacy_policy).setOnClickListener(view -> {
            openPage(PRIVACY_POLICY);
        });
        popupView.findViewById(R.id.terms_of_use).setOnClickListener(view -> openPage(TERMS_OF_USE));
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

    private void signOutButtonListener() {
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
        openPage("suggestion");

    }

    @Override
    public void openMyRecordings() {
        //this is the recordings no need to do anything
    }

    public void deleteClicked(View view) {
        if (deleteRecordingList.size() > 0) {
            alertUserBeforeDeleting();
        }
//        deleteAllClickedRecordings();

    }

    private void alertUserBeforeDeleting() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setMessage(R.string.delete_confirmation);
        alertBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            deleteAllClickedRecordings();
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onListUpdated() {
        recordingCategoryAdapter.setData(currentDatabaseRecordings.getRecordingsPerSong());
        recordingCategoryAdapter.notifyDataSetChanged();
    }

    private class GenreListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (genres != null)
                genresUI.addGenresToScreen(genres, getResources().getString(R.string.my_recordings), MY_RECORDINGS);
        }
    }
}