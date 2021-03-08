package com.function.karaoke.interaction;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.DatabaseSongsDB;
import com.function.karaoke.interaction.activities.Model.Genres;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.activities.Model.Reocording;
import com.function.karaoke.interaction.activities.Model.SaveItems;
import com.function.karaoke.interaction.activities.Model.SignInViewModel;
import com.function.karaoke.interaction.activities.Model.UserInfo;
import com.function.karaoke.interaction.fragments.SongsListFragment;
import com.function.karaoke.interaction.storage.AuthenticationDriver;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.storage.StorageAdder;
import com.function.karaoke.interaction.storage.UserService;
import com.function.karaoke.interaction.tasks.NetworkTasks;
import com.function.karaoke.interaction.utils.Billing;
import com.function.karaoke.interaction.utils.Checks;
import com.function.karaoke.interaction.utils.JsonHandler;
import com.function.karaoke.interaction.utils.SignIn;
import com.function.karaoke.interaction.utils.static_classes.Converter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SongsActivity
        extends FragmentActivity
        implements SongsListFragment.OnListFragmentInteractionListener, DialogBox.CallbackListener {

    private static final int AUDIO_CODE = 101;
    private static final String JSON_DIRECTORY_NAME = "jsonFile";
    private static final String PRIVACY_POLICY = "privacy policy";
    private static final String TERMS_OF_USE = "terms of use";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final String FEEDBACK_EMAIL = "ashira.jewishkaraoke@gmail.com";
    private static final String SONG_SUGGEST_EMAIL = "ashira.songs@gmail.com";
    private static final String USER_INFO = "User";
    private static final String GENRES = "genres";
    private static final String WEBSITE = "website";
    public String language;
    Locale myLocale;
    private Billing billingSession;
    private DatabaseSongsDB dbSongs;
    private AuthenticationDriver authenticationDriver;
    private UserInfo user;
    private SignInViewModel signInViewModel;
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent intent = result.getData();
                        if (intent.getExtras() != null) {
                            if (intent.getExtras().containsKey(USER_INFO))
                                user = (UserInfo) intent.getSerializableExtra(USER_INFO);
                            else if (intent.getExtras().containsKey("genre")) {
                                SongsListFragment fragment = getFragment();
                                fragment.getAllSongsFromGenre(intent.getExtras().getInt("genre"));
                            } else if (intent.getExtras().containsKey("suggestion")) {
                                SongsListFragment fragment = getFragment();
                                fragment.showSongSuggestionBox();
                            } else if (intent.getExtras().containsKey("open menu")) {
                                getFragment().openSettingsPopup(findViewById(android.R.id.content).getRootView());
                            } else if (intent.getExtras().containsKey("coupon")) {
                                startCouponActivity();
                            } else if (intent.getExtras().containsKey(PRIVACY_POLICY)) {
                                openPolicy(PRIVACY_POLICY);
                            } else if (intent.getExtras().containsKey(TERMS_OF_USE)) {
                                openPolicy(TERMS_OF_USE);
                            } else if (intent.getExtras().containsKey(WEBSITE)) {
                                openWebsite();
                            } else {
                                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                                handleSignInResult(task);
                            }

                        }
                    }
                }
            });
    private View songUploadedView;
    private final List<Recording> recordingsBeingUploaded = new ArrayList<>();
    private DatabaseSong songClicked;
    private LinearLayout loadingText;
    private CountDownTimer cTimer = null;
    private Recording recordingsDisplayed;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            double content = intent.getDoubleExtra("content", 0.0);
            Recording recording = (Recording) intent.getSerializableExtra("recording");
            addRecordingToScreen(content, recording);

        }
    };

    private SongsListFragment getFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        return (SongsListFragment) fragments.get(0);
    }

    private void updateUI() {
//        findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
//        findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
//        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }

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
        if (!recording.getImageResourceFile().equals("") && !recording.getImageResourceFile().equals("no image resource")) {
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
        checkForFilesToUpload();
        dbSongs = new DatabaseSongsDB();
        checkForSignedInUser();
        setContentView(R.layout.activity_songs);
        loadingText = (LinearLayout) (findViewById(R.id.loading_percent));
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        if (date.compareTo("20210403_111111") > 0)
            findViewById(android.R.id.content).getRootView().post(this::checkForBillingPurposes);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("changed"));
//        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void checkForBillingPurposes() {
        if (Checks.checkForInternetConnection(findViewById(android.R.id.content).getRootView(), this)) {
            billingSession = new Billing(SongsActivity.this, (billingResult, purchases) -> {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            billingSession.handlePurchase(purchase);
                        }
                        // the credit card is taking time
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Toast.makeText(getBaseContext(), "Purchase was cancelled", Toast.LENGTH_SHORT).show();
                    // the user pressed back
                    // Handle an error caused by a user cancelling the purchase flow.
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_TIMEOUT) {
                    Toast.makeText(getBaseContext(), "Service Timed out", Toast.LENGTH_SHORT).show();
                    // if the credit card was cancelled
                    // Handle any other error codes.
                } else {
                    Toast.makeText(getBaseContext(), "Credit card was declined", Toast.LENGTH_SHORT).show();
                    int k = 0;
                }
            }, false, () -> {
                if (billingSession.isSubscribed()) {
                    File file = renamePendingFiles();
                    if (file != null)

                        checkForFilesToUpload();
                }
            });
            billingSession.subscribeListener(new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    File file = renamePendingFiles();
                    if (file != null)
                        checkForFilesToUpload();
                }
            });
        }
    }

    private File renamePendingFiles() {
        return JsonHandler.renameJsonPendingFile(this.getFilesDir());
    }

    private void checkForFilesToUpload() {
        File folder = new File(this.getFilesDir(), JSON_DIRECTORY_NAME);
        if (folder.exists()) {
            try {
                File[] listOfAllFiles = Objects.requireNonNull(folder.listFiles());
                for (File child : listOfAllFiles) {
                    if (child.getName().contains("Pending"))
                        continue;
                    if (child.getName().contains("artist")) {
                    } else {
                        SaveItems saveItems = JsonHandler.getDatabaseFromInputStream(getFileInputStream(child));
//
                        StorageAdder storageAdder = new StorageAdder(new File(saveItems.getFile()));
                        uploadRecording(storageAdder, saveItems, folder);
//                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            deleteSongsFolder();
        }

    }

    private void deleteVideo(File file) {
        file.delete();
    }


    @Override
    public void openAdminSide() {
        Intent intent = new Intent(this, Admin.class);
        startActivity(intent);
    }


    private void deleteSongsFolder() {
        File dir = new File(this.getFilesDir(), DIRECTORY_NAME);
        if (dir.exists()) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                f.delete();
            }
        }
    }

    private void uploadRecording(StorageAdder storageAdder, SaveItems saveItems, File folder) {
        if (Checks.checkForInternetConnection(findViewById(android.R.id.content).getRootView(), this)) {
            storageAdder.uploadRecording(saveItems.getRecording(), new StorageAdder.UploadListener() {
                @Override
                public void onSuccess() {
                    NetworkTasks.uploadToWasabi(storageAdder, new NetworkTasks.UploadToWasabiListener() {
                        @Override
                        public void onSuccess() {
                            storageAdder.updateRecordingUrl(saveItems.getRecording(), new StorageAdder.UploadListener() {
                                @Override
                                public void onSuccess() {
                                    File recordingFile = (new File(saveItems.getFile()));
                                    recordingFile.delete();
                                    deleteJsonFile(folder, recordingFile.getName());
//                        deleteJsonFolder(folder);
//                    parentView.findViewById(R.id.upload_progress_wheel).setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onFailure() {
//                    ((ProgressBar) parentView.findViewById(R.id.upload_progress_wheel)).setBackgroundColor(Color.BLACK);
                                }

                                @Override
                                public void progressUpdate(double progress) {
                                    Intent intent = new Intent();
                                    intent.setAction("changed");
                                    intent.putExtra("content", progress);
                                    intent.putExtra("recording", saveItems.getRecording());
                                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
//                                if (findViewById(R.id.loading_percent) != null) {
//                                    findViewById(R.id.loading_percent).setVisibility(View.VISIBLE);
//                                    ((TextView) (findViewById(R.id.loading_percent))).setText((int) progress + "%");
//                                    if (progress == 100.0) {
//                                        findViewById(R.id.loading_percent).setVisibility(View.INVISIBLE);
//                                    }
//                                }
                                }
                            });
                        }

                        //
                        @Override
                        public void onFail() {
                            int k = 0;
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onProgress(int percent) {
//                        if (findViewById(R.id.loading_percent) != null)
//                            loadingText.setText(percent + "%");
//                        if (percent >= 100) {
//                            startTimerForThreeSecondsToShow();
//                        }
                            Intent intent = new Intent();
                            intent.setAction("changed");
                            intent.putExtra("content", (double) percent);
                            intent.putExtra("recording", saveItems.getRecording());
                            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
//                        addRecordingToScreen(percent, saveItems.getRecording());
//                        loadingText.addView(createViewForLoading(percent, saveItems.getRecording()));
                        }
                    });
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void progressUpdate(double progress) {

                }
            });
        }
    }

    private void startTimerForThreeSecondsToShow() {
        if (cTimer == null) {
            cTimer = new CountDownTimer(2000, 500) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    cTimer.cancel();
//                    loadingText.setText("");
                    cTimer = null;
                }
            };
            cTimer.start();
        }
    }

    private void deleteJsonFile(File folder, String name) {
        (new File(folder, name + ".json")).delete();
        File jsonFileFolder = new File(folder, JSON_DIRECTORY_NAME);
        if (jsonFileFolder.list() == null || Objects.requireNonNull(jsonFileFolder.list()).length == 0)
            jsonFileFolder.delete();
    }

    private InputStream getFileInputStream(File file) throws IOException {
        return new FileInputStream(file);
    }


    private void launchSignIn() {
        Intent intent = new Intent(this, PromoActivity.class);
        intent.putExtra("callback", true);
        intent.putExtra("language", Locale.getDefault().getLanguage());
        mGetContent.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File folder = new File(this.getFilesDir(), JSON_DIRECTORY_NAME);
        if (!folder.exists())
            deleteSongsFolder();
    }

    private void checkForSignedInUser() {
        authenticationDriver = new AuthenticationDriver();
        UserService userService = new UserService(new DatabaseDriver(), authenticationDriver);
        if (authenticationDriver.getUserUid() == null) {
            updateUI();
        } else if (authenticationDriver.isSignIn()
                && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("")) {
            userService.getUser(new UserService.GetUserListener() {
                @Override
                public void user(UserInfo userInfo) {
                    user = userInfo;
                }
            });
        }
    }

    @Override
    public void alertUserToSignIn() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.sign_in_title);
        alertBuilder.setMessage(R.string.recording_sign_in_text);
        alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> {

        });
        AlertDialog alert = alertBuilder.create();
        alert.show();

    }

    @Override
    public void signOut() {
        authenticationDriver.signOut();
        launchSignIn();

    }

    @Override
    public void onListFragmentInteractionPlay(Reocording item) {
        songClicked = (DatabaseSong) item;
        askForAudioRecordPermission();
    }

    @Override
    public void openEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));
        intent.setData(Uri.parse("mailto:"));
        if (deviceHasGoogleAccount())
            intent.setPackage("com.google.android.gm");
        mGetContent.launch(intent);
    }

    @Override
    public void startRecordingsActivity(Genres genres) {
        Intent intent = new Intent(this, RecordingsActivity.class);
        intent.putExtra(USER_INFO, user);
        intent.putExtra(GENRES, genres);
        mGetContent.launch(intent);
    }

    @Override
    public void onBackPressed() {
        if (!getFragment().backPressed())
            finish();
    }

    @Override
    public UserInfo getUser() {
        return user;
    }

    @Override
    public void signIn() {
        SignIn signIn = new SignIn(this, this, this, mGetContent);
        signIn.openSignIn();
    }

    @Override
    public void startCouponActivity() {
        Intent intent = new Intent(this, CouponActivity.class);
        intent.putExtra(USER_INFO, user);
        mGetContent.launch(intent);
    }

    @Override
    public void openPolicy(String policy) {
        Intent intent = new Intent(this, PolicyActivity.class);
        intent.putExtra(policy, true);
        mGetContent.launch(intent);
    }

    private boolean deviceHasGoogleAccount() {
        AccountManager accMan = AccountManager.get(this);
        Account[] accArray = accMan.getAccountsByType("com.google");
        return accArray.length >= 1;
    }

    private void askForAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {


            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle(R.string.mic_access_title);
            alertBuilder.setMessage(R.string.mic_access_text);
            alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) ->
                    ActivityCompat.requestPermissions(SongsActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            AUDIO_CODE));
            AlertDialog alert = alertBuilder.create();
            alert.show();


        } else
            openNewIntent();
    }

    @Override
    public DatabaseSongsDB getSongs() {
        return dbSongs;
    }


    @Override
    public void changeLanguage() {
        if (language.equals("en")) {
            setLocale("iw");
            language = "iw";
        } else {
            setLocale("en");
            language = "en";
        }
        Intent refresh = new Intent(this, SongsActivity.class);
        startActivity(refresh);
    }

    @Override
    public void openSignUp() {
        launchSignIn();
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

    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        if (prefs != null) {
            String language = prefs.getString(langPref, "");
            if (language != null && !language.equalsIgnoreCase("")) {
                this.language = language;

            }
        }
        if (language == null)
            language = Locale.getDefault().getLanguage();
        setLocale(language);
    }


    @Override
    protected void onPause() {
        saveCurrentState();
        super.onPause();
    }

    private void saveCurrentState() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                openNewIntent();
        }
    }

    private void openNewIntent() {
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, songClicked);
        if (authenticationDriver.isSignIn() && user != null) {
            intent.putExtra(USER_INFO, user);
        }
        mGetContent.launch(intent);
    }

    public void openWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ashira-music.com/"));
        startActivity(browserIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            signInViewModel = ViewModelProviders.of(this).get(SignInViewModel.class);
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            signInViewModel.firebaseAuthWithGoogle(account.getIdToken(), new SignInViewModel.SuccessFailListener() {
                @Override
                public void onSuccess(FirebaseUser firebaseUser) {
                    signInViewModel.isUserInDatabase(new SignInViewModel.DatabaseListener() {

                        @Override
                        public void isInDatabase(boolean inDatabase) {
                            if (inDatabase) {
                                user = signInViewModel.getUser();
                            } else {
                                user = new UserInfo(firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), firebaseUser.getUid(), 0, 0);

                                signInViewModel.addNewUserToDatabase(user);
                            }
                        }

                        @Override
                        public void failedToSearchDatabase() {
                            if (checkInternet()) {
                                user = new UserInfo(firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), firebaseUser.getUid(), 0, 0);

                                signInViewModel.addNewUserToDatabase(user);
                            }
                        }
                    });
                    SongsListFragment fragment = getFragment();
                    fragment.showSuccessSignIn();
                }


                @Override
                public void onFailure() {
                    showFaileure();
                }
            });
        } catch (Exception e) {
//                Toast.makeText(this, context.getResources().getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkInternet() {
        return Checks.checkForInternetConnection(findViewById(R.id.song_list_fragment), this);
    }

    private void showFaileure() {
        Toast.makeText(this, getResources().getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
    }


    @Override
    public void callback(String result) {

    }
}
