package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.SaveItems;
import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.adapters.DeleteRecordingRecycler;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.CloudUpload;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.RecordingDelete;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.storage.SongService;
import com.function.karaoke.hardware.storage.UserService;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.tasks.OpenCameraAsync;
import com.function.karaoke.hardware.ui.SingActivityUI;
import com.function.karaoke.hardware.utils.Billing;
import com.function.karaoke.hardware.utils.CameraPreview;
import com.function.karaoke.hardware.utils.Checks;
import com.function.karaoke.hardware.utils.EarphoneListener;
import com.function.karaoke.hardware.utils.JsonHandler;
import com.function.karaoke.hardware.utils.static_classes.GenerateRandomId;
import com.function.karaoke.hardware.utils.static_classes.ShareLink;
import com.function.karaoke.hardware.utils.static_classes.SyncFileData;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SingActivity extends AppCompatActivity implements
        DialogBox.CallbackListener, KaraokeController.MyCustomObjectListener,
        SingActivityUI.SignInListener, SingActivityUI.FreeShareListener, SingActivityUI.ShareListener {

    public static final String EXTRA_SONG = "EXTRA_SONG";
    public static final String RECORDING = "recording";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final String DELAY = "delay";
    private static final String LENGTH = "length";
    private static final String RESULT_CODE = "code";
    private static final String SAVE_FUNC = "save";
    private static final String USER_INFO = "User";
    private static final String FREE_SHARE_USED = "freeShares";
    private static final String USER_DOWNLOADS = "shares";
    private static final String USER_VIEWS = "views";
    private static final int BACK_CODE = 101;
    private static final int MESSAGE_RESULT = 1;
    private static final int SHARING_ERROR = 100;
    private static final int UPLOAD_ERROR = 101;
    private static final int SAVE = 101;
    private static final int SHARE = 102;
    private static final int CAMERA_ERROR = 111;
    private static final String SING_ACTIVITY = "sing activity";
    private static final String CALLBACK = "callback";
    private static final int WATCH_RECORDING = 131;
    private static final String CAMERA_ON = "camera on";
    private static final int FAILED_TO_RECORD_SUB_TYPE = 2;
    private static final int YEARLY_SUB = 1;
    private static final int MONTHLY_SUB = 0;
    private static final int NOT_PAYING_MEMBER = -1;
    private final String TIMES_DOWNLOADED = "timesDownloaded";
    private final String TIMES_PLAYED = "timesPlayed";
    private final int CAMERA_CODE = 2;
    private final Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Bitmap is loaded, use image here
            float bitmapScale = 0.4f;
            int width = Math.round(bitmap.getWidth() * bitmapScale);
            int height = Math.round(bitmap.getHeight() * bitmapScale);

            Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

//            Bitmap bm = BlurBuilder.blur(getBaseContext(), bitmap);
            findViewById(R.id.initial_album_cover).setBackground((new BitmapDrawable(getResources(), outputBitmap)));
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            Toast.makeText(getBaseContext(), "failed to loadWords album", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    };
    private final int ALLOCATED_NUMBER_OF_RECORDINGS = 100;
    private final boolean timerStarted = false;
    private final boolean songStarted = false;
    private final boolean prompted = false;
    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    AuthenticationDriver authenticationDriver;
    private String recordingId;
    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
    private DatabaseSong song;
    private boolean isRunning = false;
    private boolean restart = false;
    private boolean buttonClicked = false;
    private TextureView mTextureView;
    private CameraPreview cameraPreview;
    private boolean isRecording = false;
    private boolean ending = false;
    private boolean fileSaved = false;
    private boolean cameraOn = false;
    private boolean permissionRequested = false;
    private String timeStamp;
    private long lengthOfAudioPlayed = 0;
    private SingActivityUI activityUI;
    private int delay;
    private Billing billingSession;
    private boolean itemAcquired = false;
    private boolean keepVideo = false;
    private File postParseVideoFile;
    private Recording recording;
    private boolean measured = false;
    private boolean ml;
    private UserInfo user;
    private SignInViewModel signInViewModel;
    private UserService userService;
    private String songPlayed;
    private String funcToCall;
    private boolean cancelled;
    private String language;
    private Handler hdlr;
    private boolean cameraClosed = false;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (!cameraClosed) {
                cameraPreview.setTextureView(mTextureView);
                cameraPreview.setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                cameraPreview.connectCamera();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    private boolean startTimerStarted = false;
    private boolean resumeTimer = false;
    private BroadcastReceiver bReceiver;
    private File compressedFile;
    private File jsonFile;
    private int type = -1;
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null) {
                    if (result.getResultCode() == SAVE) {
//                        saveRecordingToTheCloud(this.getCurrentFocus());
                    } else if (result.getResultCode() == SHARE) {
                        saveAndShare(this.getCurrentFocus());
                    } else if (result.getResultCode() == WATCH_RECORDING) {
                        openNewIntent(Uri.fromFile(postParseVideoFile));
                    } else {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
            });
    private RecordingDelete recordingDelete;
    private EarphoneListener earphonesListener;
    private String link1;
    private boolean earphonesUsed;
    private SongService songService;
    private boolean songUpdated = false;
    private boolean userUpdated = false;
    private String password;
    private File mVideoFolder;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        authenticationDriver = new AuthenticationDriver();
        createServices();
        getUser();
        createCameraAndRecorderInstance();
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        activityUI = new SingActivityUI(findViewById(android.R.id.content).getRootView(), song, Util.SDK_INT);
        setContentView(R.layout.activity_sing);
        findViewById(android.R.id.content).getRootView().post(this::continueWithSetup);


        mTextureView = findViewById(R.id.surface_camera);
        recordingId = GenerateRandomId.generateRandomId();
//        createEarphoneReceivers();
        earphonesListener = new EarphoneListener(this);
        checkForPermissionAndOpenCamera();

        if (Util.SDK_INT < 24)
            alertUserThatHeCanNotPause();

    }

    private void continueWithSetup() {
        if (checkForInternet()) {
            setKaraokeController();
            loadSong();
            activityUI.setSongInfo();
            blurAlbumInBackground();
            if (song.hasDifferentTones()) {
                activityUI.openTonePopup(song, SingActivity.this);
            } else {
                songPlayed = song.getSongResourceFile();

                mKaraokeKonroller.loadAudio(songPlayed);
//            createEarphoneReceivers();
            }
        } else
            startTimerToFinish();
    }

    private void startTimerToFinish() {
        cTimer = new CountDownTimer(1500, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                finishActivity();
            }

        };
        cTimer.start();
    }

    private void createServices() {
        userService = new UserService(new DatabaseDriver(), authenticationDriver);
        songService = new SongService();
    }

    private void getUser() {
        if (getIntent().getExtras().containsKey(USER_INFO)) {
            user = (UserInfo) getIntent().getSerializableExtra(USER_INFO);
        }
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

    public void alertUserThatHeCanNotPause() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.no_pause_title);
        alertBuilder.setMessage(R.string.no_pause_body);
        alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
//        Intent refresh = new Intent(this, SingActivity.class);
//        startActivity(refresh);
    }


    private void setAudioAndDismissPopup() {
        activityUI.changeLayout();
        activityUI.dismissPopup();
        activityUI.showLoadingIcon();
//        createEarphoneReceivers();
        downloadFile(songPlayed);
        // todo this is what I added download file it is at the end of the class
//        mKaraokeKonroller.loadAudio(songPlayed);

    }

    private void startBuild() {
        mKaraokeKonroller.loadAudio(songPlayed);
    }

    public void manTone(View view) {
        songPlayed = song.getSongResourceFile();
        setAudioAndDismissPopup();
//        createEarphoneReceivers();
    }


    public void womanTone(View view) {
        songPlayed = song.getWomanToneResourceFile();
        setAudioAndDismissPopup();
//        createEarphoneReceivers();
    }

    public void kidTone(View view) {
        songPlayed = song.getKidToneResourceFile();
        setAudioAndDismissPopup();
//        createEarphoneReceivers();
    }


    private void setKaraokeController() {
        mKaraokeKonroller = new KaraokeController();
        mKaraokeKonroller.init(this);
        mKaraokeKonroller.addViews(findViewById(R.id.word_space), R.id.lyrics, R.id.words_to_read,
                R.id.words_to_read_2, R.id.word_space, R.id.words_to_read_3);
        mPlayer = mKaraokeKonroller.getmPlayer();
        mKaraokeKonroller.setCustomObjectListener(SingActivity.this);
    }


    private void checkForPermissionAndOpenCamera() {
        if (Checks.checkCameraHardware(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionRequested = true;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
            } else {
//                initiateCamera();
                openCamera();
            }
        } else {
            activityUI.turnOffCameraOptions();
            cameraPreview.initiateRecorder();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionRequested = false;
        if (requestCode == CAMERA_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                cameraPreview.initiateRecorder();
            else
//                    initiateCamera();
                openCamera();
        }
    }

    public void openCamera() {
        OpenCameraAsync.openCamera(cameraPreview, mTextureView, mSurfaceTextureListener, new OpenCameraAsync.OpenCameraListener() {
            @Override
            public void onSuccess() {
                cameraOn = true;
            }

            @Override
            public void onFail() {
                showFailure(CAMERA_ERROR);
            }
        });
//        }

    }


    private void turnCameraOff() {
        cameraPreview.closeCamera();
        cameraOn = false;
    }

    private void createCameraAndRecorderInstance() {
        cameraPreview = new CameraPreview(SingActivity.this, this);
    }

    private void loadSong() {
        if (song.getLines() == null) {
            findViewById(android.R.id.content).getRootView().post(() -> parseWords());
        } else {

            if (!mKaraokeKonroller.loadWords(song.getLines())) {
                if (activityUI.isPopupOpened()) {
                    activityUI.dismissPopup();
                }
                finish();
            }
            activityUI.hideLoadingIndicator();
            activityUI.showPlayButton();
        }
        if (null != song) {
//            addTitleToScreen();
            activityUI.addTitleToScreen();
            activityUI.setBackgroundColor();
//            findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
        }
    }

    private void parseWords() {
        if (checkForInternet()) {
            NetworkTasks.parseWords(song, new NetworkTasks.ParseListener() {
                @Override
                public void onSuccess() {

                    if (!mKaraokeKonroller.loadWords(song.getLines())) {
                        if (activityUI.isPopupOpened()) {
                            activityUI.dismissPopup();
                        }
                        finish();
                    }

                }

                @Override
                public void onFail() {
                    finish();
                }
            });
        }
//        else
//            startTimerToFinish();
    }

    @Override
    public void callback(String result) {
        switch (result) {
            case "yes":
                ending = true;
                endSong();
                if (activityUI.isPopupOpened()) {
                    activityUI.dismissPopup();
                }
                finish();
                break;
            case "ok":
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (activityUI.isPopupOpened()) {
                    activityUI.dismissPopup();
                }
                finish();
                break;
            case "no":
                cancelled = false;
                break;
        }
    }

    private void endSong() {
        if (mKaraokeKonroller.isPlaying()) {
            mKaraokeKonroller.onStop();
//                customMediaPlayer.onStop();
        }
        if (isRecording) {
            cameraPreview.stopRecording();
        }
        if (!keepVideo)
            deleteVideo();
        cancelled = true;
        cancelTimer();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void playback(View view) {
        if (!buttonClicked) {
//            view.setBackgroundColor(getResources().getColor(R.color.gold, getTheme()));
            buttonClicked = true;

            if (postParseVideoFile == null)
                postParseVideoFile = wrapUpSong();
            buttonClicked = false;
            if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null &&
                    !authenticationDriver.getUserEmail().equals("")) {
//                compressedFile = getTempMediaFile();

//                compress(postParseVideoFile.getPath(), compressedFile.getPath());
                openNewIntent(Uri.fromFile(postParseVideoFile));
//
            } else {
                activityUI.openSignInPopup(this, this);
                funcToCall = PLAYBACK;
            }
        }
//        view.setBackgroundColor(getResources().getColor(R.color.appColor, getTheme()));
    }

//    private void compress(String filePath, String destPath) {
//        VideoCompressor.start(filePath, destPath, new CompressionListener() {
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onSuccess() {
//                openNewIntent(Uri.fromFile(compressedFile));
//            }
//
//            @Override
//            public void onFailure(String s) {
//
//            }
//
//            @Override
//            public void onProgress(float v) {
//
//            }
//
//            @Override
//            public void onCancelled() {
//
//            }
//        });
//    }

    private void openNewIntent(Uri uriFromFile) {

        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(PLAYBACK, uriFromFile.toString());
//        if (earphonesUsed)
        intent.putExtra(AUDIO_FILE, songPlayed);
        intent.putExtra(CAMERA_ON, cameraOn);
        intent.putExtra(DELAY, delay);
        intent.putExtra(LENGTH, lengthOfAudioPlayed);
        startActivity(intent);
    }

    private void blurAlbumInBackground() {
        if (song.getImageResourceFile() != null && !song.getImageResourceFile().equals("")) {
            final ImageView tv = findViewById(R.id.initial_album_cover);
            final ViewTreeObserver observer = tv.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(() -> {
                if (!measured) {
                    measured = true;
                    Picasso.get()
                            .load(song.getImageResourceFile())
                            .into(target);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!ending && !permissionRequested) {
            if (Util.SDK_INT >= 24)
                pauseSong(this.getCurrentFocus());
            else {
                finishSong();
            }
        }
        super.onPause();
    }

    public void returnToMain(View view) {
        if (!buttonClicked) {
            cancelled = true;
            buttonClicked = true;
            if (Util.SDK_INT >= 24)
                pauseSong(view);
            else
                finishSong();
            showBackDialogBox();
            buttonClicked = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (Util.SDK_INT >= 24) {
            pauseSong(findViewById(R.id.sing_song));
        } else {
            finishSong();
        }
        DialogBox back = DialogBox.newInstance(this, BACK_CODE);
        back.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }


    private void showBackDialogBox() {
        DialogBox back = DialogBox.newInstance(this, BACK_CODE);
        back.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    public void returnToMainFromPopup(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            if (!itemAcquired) {
                showBackDialogBox();
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (activityUI.isPopupOpened()) {
                    activityUI.dismissPopup();
                }
                finish();
            }
            buttonClicked = false;
        }
    }

    private void finishActivity() {
        if (user != null) {
            Intent intent = new Intent(this, SongsActivity.class);
            intent.putExtra(USER_INFO, user);
            setResult(RESULT_OK, intent);
        }
        updateServices();
        if (cameraPreview != null)
            cameraPreview.closeCamera();

        finish();
    }

    private void updateServices() {
        userService.updateUserFields(new UserService.UserUpdateListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        });
        songService.updateSongData(song.getTitle());
    }

//    public void resumeSong() {
//        mKaraokeKonroller.onResume();
//        isRunning = true;
//    }

    public void playMusic(View view) {
        if (mKaraokeKonroller.isPrepared() && !startTimerStarted) {
            restart = false;
            activityUI.setSurfaceForRecording(cameraOn);
            startTimer();
        }
    }

    void startTimer() {
        final boolean[] prepared = {false};
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cancelTimer();
        startTimerStarted = true;
        cTimer = new CountDownTimer(3800, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                activityUI.displayTimeForCountdown(millisUntilFinished);
                if (millisUntilFinished / 1000 >= 1 && !prepared[0]) {
                    prepared[0] = true;
                    cameraPreview.prepareMediaRecorder();
                    activityUI.setTotalLength(mPlayer.getDuration() / 1000);
                }
            }

            public void onFinish() {
                startTimerStarted = false;
                cancelTimer();
                if (cancelled) {
                    cameraPreview.stopRecording();
                    cameraPreview.closeCamera();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    refreshWindow();
                } else {
//                customMediaPlayer.startSong();
                    mKaraokeKonroller.onResume();

                    earphonesUsed = earphonesListener.getEarphonesUsed();
                    isRunning = true;
                    setProgressBar();
                    isRecording = true;
                    activityUI.setScreenForPlayingAfterTimerExpires();
                }
            }
        };
        cTimer.start();
    }

//    private void unregisterReceivers() {
//        unregisterReceiver(mReceiver);
//        unregisterReceiver(bReceiver);
//    }

    void cancelTimer() {
        if (cTimer != null) {
            cTimer.cancel();
            cTimer = null;
        }
        if (startTimerStarted) {
            startTimerStarted = false;
            activityUI.resetScreenForTimer();
            deleteVideo();
            cameraPreview.realeaseRecorder();
        } else if (resumeTimer) {
            resumeTimer = false;
            activityUI.resetResumeTimer();
        }
    }

    private void setProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        TextView duration = findViewById(R.id.duration);
        progressBar.setMax(mPlayer.getDuration() / 1000);
        final int[] i = {progressBar.getProgress()};
        hdlr = new Handler();
        StartProgressBar(duration, i, progressBar);
    }


    private void StartProgressBar(TextView duration, int[] i, ProgressBar
            progressBar) {
        new Thread(() -> {
            while (!ending && i[0] < mPlayer.getDuration() / 1000 && !restart) {
                while (!ending && isRunning && i[0] < mPlayer.getDuration() / 1000) {
                    i[0] += 1;
                    // Update the progress bar and display the current value in text view
                    hdlr.post(() -> {
                        progressBar.setProgress(i[0]);
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            int minutes = (mPlayer.getCurrentPosition() / 1000) / 60;
                            int seconds = (mPlayer.getCurrentPosition() / 1000) % 60;
                            @SuppressLint("DefaultLocale") String text = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
                            duration.setText(text);
                        }
                    });
                    try {
                        // Sleep for 1 second to show the progress slowly.
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }).start();
    }

    public void openEndOptions(boolean songEnded) {
        activityUI.openEndPopup(this, songEnded);
        PopupWindow popup = activityUI.getPopup();
        popup.setOnDismissListener(() -> {
            if (ending) finishActivity();
            activityUI.popupClosed();
            activityUI.undimBackground();
        });
    }

    //cancel timer

    public void openEndOptions(View view) {
        if (mPlayer != null) {
            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
//            if (Util.SDK_INT >= 24) {
//                pauseSong(view);
            openEndOptions(false);
//            } else {
//                finishSong();
//            }
        }
    }

    public void pauseSong(View view) {
        if (isRunning)
            activityUI.songPaused();
        isRunning = false;
        if (Util.SDK_INT >= 24) {
            if (isRecording) {
                cameraPreview.pauseRecording();
            }
            if (postParseVideoFile == null && cameraPreview.getVideo() != null)
                startPauseTimerToSaveBattery();
        } else {
            mKaraokeKonroller.onPause();
            finishSong();
        }
        if (mKaraokeKonroller != null)
            mKaraokeKonroller.onPause();
    }

    private void startPauseTimerToSaveBattery() {
        cancelTimer();
        cTimer = new CountDownTimer(120000, 110000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                cancelTimer();
                finishSong();
            }
        };
        cTimer.start();
    }

    private void finishSong() {
        cancelTimer();
        if (isRecording) {
            if (postParseVideoFile == null) {
                lengthOfAudioPlayed = mPlayer.getCurrentPosition();
                postParseVideoFile = wrapUpSong();
            }
        }
        if (lengthOfAudioPlayed != 0) openEndOptions(true);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stopRecordingAndSong() {
        ending = true;
        if (isRecording) {
            cameraPreview.stopRecording();
            isRecording = false;
            cameraPreview.closeCamera();
            cameraClosed = true;
        }
        if (mKaraokeKonroller.isPlaying()) {

            mKaraokeKonroller.onStop();
//            customMediaPlayer.onStop();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private File wrapUpSong() {
        cancelTimer();
        try {
            stopRecordingAndSong();
            File file = cameraPreview.getVideo();
            File newlyParsedFile = SyncFileData.parseVideo(file, getOutputMediaFile());
            setDelay(Uri.fromFile(newlyParsedFile));
            return newlyParsedFile;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setDelay(Uri uriFromFile) {
        MediaPlayer mp = MediaPlayer.create(this, uriFromFile);
        int duration = mp.getDuration();
        delay = (int) (duration - lengthOfAudioPlayed);
//        delay = (int) (mKaraokeKonroller.getTimerStarted() - cameraPreview.getTimeCreated());
//
//        showFailure();
    }


    public void resumeSong(View view) {
        if (!resumeTimer)
            startResumeTimer();
    }

//    public void stopSong

    private void startResumeTimer() {
        cancelTimer();
        resumeTimer = true;
        cTimer = new CountDownTimer(3800, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                activityUI.displayTimeForRestartCountdown(millisUntilFinished);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onFinish() {
                resumeTimer = false;
                cancelTimer();
                if (cancelled) {
                    cancelled = false;
                } else {
                    restart = false;
                    isRunning = true;
                    mKaraokeKonroller.onResume();
//                customMediaPlayer.startSong();
                    cameraPreview.resumeRecording();
                    mPlayer = mKaraokeKonroller.getmPlayer();
                    isRecording = true;
                    activityUI.setScreenForPlayingAfterRestartTimerExpires();
                }
            }
        };
        cTimer.start();
    }

    public void returnToSong(View view) {
        activityUI.dismissPopup();
    }

    public void playAgain(View view) {
        if (!buttonClicked) {
            ending = true;
            if (!keepVideo)
                deleteVideo();
            findViewById(R.id.word_space).setVisibility(View.INVISIBLE);
            cameraPreview.stopRecording();
            cameraPreview.closeCamera();
            mKaraokeKonroller.onStop();
            activityUI.dismissPopup();
            refreshWindow();
        }
    }

    private void refreshWindow() {
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, song);
        if (user != null)
            intent.putExtra(USER_INFO, user);
        if (activityUI.isPopupOpened()) {
            activityUI.dismissPopup();
        }
        finish();
        startActivity(intent);
    }

    private void deleteVideo() {
        if (cameraPreview.getVideo() != null)
            cameraPreview.getVideo().delete();
    }


    private File getOutputMediaFile() {
        File mediaStorageDir = new File(this.getFilesDir(), DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }

    private File getTempMediaFile() {
        File mediaStorageDir = new File(this.getFilesDir(), DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "VID_COMPRESS" + timeStamp + ".mp4");
        return mediaFile;
    }

    public void saveAndShare(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null &&
                    !authenticationDriver.getUserEmail().equals(""))

                if (!itemAcquired) {
                    //todo check if user has room only if he is a subcscribed user
                    String date = new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date());
                    if (date.compareTo(user.getExpirationDate()) > 0)
                        startBilling();
                    else
                        startSaveProcess(false);
                } else
                    activityUI.openShareOptions(this, user, this);
            else {
                activityUI.openSignInPopup(this, this);
                funcToCall = SAVE_FUNC;
            }
            buttonClicked = false;
        }
    }

    //todo check if user has room before sharing

    private void checkIfUserHasRoomToStore() {
        RecordingService recordingService = new RecordingService();
        recordingService.getNumberOfRecordingsFromUID(new RecordingService.NumberListener() {
            @Override
            public void recordings(List<Recording> recordings) {
                if (recordings.size() < ALLOCATED_NUMBER_OF_RECORDINGS) {
                    checkIfUserHasFreeAcquisition();
                } else {
                    View recordingPopup = activityUI.openRecordingsForDelete(SingActivity.this);
                    RecyclerView recyclerView = (RecyclerView) recordingPopup.findViewById(R.id.recordings_recycler);
                    recyclerView.setLayoutManager(new LinearLayoutManager(SingActivity.this));
                    DeleteRecordingRecycler recordAdapter = new DeleteRecordingRecycler(recordings, new DeleteRecordingListener() {
                        @Override
                        public void play(Recording mItem) {
                            playRecording(mItem);
                        }

                        @Override
                        public void delete(Recording mItem) {
                            deleteRecording(mItem);
                        }
                    }, getCurrentLanguage());
                    recyclerView.setAdapter(recordAdapter);
                }
            }

            @Override
            public void failure() {

            }
        });
    }

    private void playRecording(Recording mItem) {
        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(SingActivity.RECORDING, mItem);
        startActivity(intent);
    }

    private void openRecordingsForDelete() {

    }

    private String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private void checkIfUserHasFreeAcquisition() {
        userService.getUserFromDatabase(new UserService.GetUserListener() {
            @Override
            public void user(UserInfo freeShare) {
                if (user.getSubscriptionType() > NOT_PAYING_MEMBER) {
                    keepVideo = true;
                    itemAcquired = true;
                    userService.updateUserFields(new UserService.UserUpdateListener() {
                        @Override
                        public void onSuccess() {
                            saveSongToTempJsonFile();
                            jsonFile = renameJsonPendingFile();
                            save(jsonFile);
                            activityUI.showShareItems();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(getParent(), "We are sorry but you can not continue at this time", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else
                    SingActivity.this.startBilling();
            }
        });
    }

    private void saveSongToTempJsonFile() {
        if (postParseVideoFile == null)
            postParseVideoFile = wrapUpSong();
        if (postParseVideoFile != null) {
            recording = new Recording(song, songPlayed, timeStamp,
                    authenticationDriver.getUserUid(), recordingId, delay, lengthOfAudioPlayed, cameraOn, true);
//        if (!earphonesUsed)
//            recording.earphonesNotUsed();
            JsonHandler.createTempJsonObject(postParseVideoFile, recording, this.getFilesDir());
        }
    }


    @Override
    public void share(View view) {
        if (link1 != null) {
            sendDataThroughIntent(link1);
        }

//        createLink(recording.getRecordingId(), recording.getRecorderId(), Integer.toString(recording.getDelay()));
    }

    private void startBilling() {
        billingSession = new Billing(SingActivity.this, (billingResult, purchases) -> {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                keepVideo = true;
                saveSongToTempJsonFile();
                for (Purchase purchase : purchases) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
//                          saveSongToJsonFile();
                        itemAcquired = true;
                        billingSession.handlePurchase(purchase);

                    }
                    // the credit card is taking time
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                deletePendingJsonFile();
                keepVideo = false;
                Toast.makeText(getBaseContext(), "Purchase was cancelled", Toast.LENGTH_SHORT).show();

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_TIMEOUT) {
                deletePendingJsonFile();
                keepVideo = false;
                Toast.makeText(getBaseContext(), "Service Timed out", Toast.LENGTH_SHORT).show();

            } else {
                deletePendingJsonFile();
                keepVideo = false;
                Toast.makeText(getBaseContext(), "Credit card was declined", Toast.LENGTH_SHORT).show();
            }
        }, true, () -> {
            UserService.UserUpdateListener changeUserTypeListener = new UserService.UserUpdateListener() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure() {

                }
            };
            if (billingSession.isSubscribed()) {
                if (user.getSubscriptionType() == NOT_PAYING_MEMBER) {
                    userService.addSubscriptionType(changeUserTypeListener, FAILED_TO_RECORD_SUB_TYPE);
                    user.setSubscriptionType(FAILED_TO_RECORD_SUB_TYPE);
                }
                startSaveProcess(false);
            } else {
                if (user.getSubscriptionType() != NOT_PAYING_MEMBER) {
                    userService.addSubscriptionType(changeUserTypeListener, NOT_PAYING_MEMBER);
                    user.setSubscriptionType(NOT_PAYING_MEMBER);
                }
                startSubscriptionPath();
            }

        });
        billingSession.subscribeListener(billingResult -> {
            userService.addSubscriptionType(new UserService.UserUpdateListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure() {

                }
            }, type);
            user.setSubscriptionType(type);
            continueWithSaveProcess(false);
        });
        buttonClicked = false;
    }

    public void startSaveProcess(boolean freeShareUsed) {
        keepVideo = true;
        itemAcquired = true;
        saveSongToTempJsonFile();
        continueWithSaveProcess(freeShareUsed);

    }

    private void continueWithSaveProcess(boolean freeShareUsed) {
        keepVideo = true;
        itemAcquired = true;
        jsonFile = renameJsonPendingFile();
        addOneToSongsDownload();
        addOneToUserDownloads(freeShareUsed);
        save(jsonFile);
        activityUI.openShareOptions(this, user, this);
    }

    private void addOneToUserDownloads(boolean freeShareUsed) {
        if (!userUpdated) {
            userUpdated = true;
            user.addShare();
            if (freeShareUsed) {
                user.freeShareUsed();
                userService.addFieldToUpdate(FREE_SHARE_USED);
            }
            userService.addFieldToUpdate(USER_DOWNLOADS);
            userService.addFieldToUpdate(USER_VIEWS);
            userService.updateUserFields(new UserService.UserUpdateListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure() {
                    showFailure();
                }
            });
        }
    }

    private void startSubscriptionPath() {
        activityUI.openInitialShareOptions(this, user, this);
    }

    private void addOneToSongsDownload() {
        if (!songUpdated) {
            songUpdated = true;
            songService.addFieldToUpdate(TIMES_PLAYED);
            songService.addFieldToUpdate(TIMES_DOWNLOADED);
            songService.updateSongData(song.getTitle());
        }

    }

    private void deletePendingJsonFile() {
        JsonHandler.deletePendingJsonFile(this.getFilesDir());
    }

    private File renameJsonPendingFile() {
        return JsonHandler.renameJsonPendingFile(this.getFilesDir());
    }


    private void showFailure(int error) {
        switch (error) {
            case SHARING_ERROR:
                Toast.makeText(this, "sharing failed", Toast.LENGTH_SHORT).show();
                break;
            case UPLOAD_ERROR:
                Toast.makeText(this, "video failed to loadWords", Toast.LENGTH_SHORT).show();
                break;
            case CAMERA_ERROR:
                Toast.makeText(this, "Camera failed to open", Toast.LENGTH_SHORT).show();
                break;
        }

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MESSAGE_RESULT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "E-Mail sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast toast = Toast.makeText(this, "Email failed to send", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }

    private void save(File jsonFile) {
        try {
            SaveItems saveItems = JsonHandler.getDatabaseFromInputStream(getFileInputStream(jsonFile));
            saveToCloud(saveItems);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream getFileInputStream(File file) throws IOException {
//        File videoFile = new File(folder, JSON_FILE_NAME + ".json");
        return new FileInputStream(file);
    }


    private void launchSignIn(int code) {
        mGetContent.launch(new Intent(this, PromoActivity.class).putExtra(RESULT_CODE, code).putExtra(SING_ACTIVITY, true).putExtra(CALLBACK, true));
    }

    private void saveToCloud(SaveItems saveItems) {
        if (!fileSaved) {
            fileSaved = true;
            CloudUpload cloudUpload = new CloudUpload(saveItems.getRecording(), this.getFilesDir(), saveItems.getArtist(), new CloudUpload.UploadListener() {
                @Override
                public void onSuccess(File file) {
//                    deleteVideo(file);
                    keepVideo = false;
                }

                @Override
                public void onFailure() {

                }

                @Override
                public void onProgress(int progress) {
                    activityUI.showProgress(progress, getBaseContext(), saveItems.getRecording());
                }
            });
            cloudUpload.saveToCloud(new File(saveItems.getFile()));

        }
    }

    public void closeShareOptions(View view) {
        activityUI.hideShareItems();
    }

    public void returnToEndOptions(View view) {
        activityUI.hideSubscribeOptions();
    }

    public void openYearlySubOption(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            type = 1;
            billingSession.startFlow(YEARLY_SUB);
            buttonClicked = false;
        }
    }

    public void openMonthlySubOption(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            type = 0;
            billingSession.startFlow(MONTHLY_SUB);
            buttonClicked = false;
        }
    }


    public void deleteRecording(Recording mItem) {
        List<Recording> deleting = new ArrayList<>();
        deleting.add(mItem);
        recordingDelete = new RecordingDelete(() -> deleteRecording(), deleting);
    }

    private void deleteRecording() {
        NetworkTasks.deleteFromWasabi(recordingDelete, new NetworkTasks.DeleteListener() {
            @Override
            public void onSuccess() {
                showSuccessToast();
                activityUI.dismissRecordings();
                checkIfUserHasFreeAcquisition();

            }

            @Override
            public void onFail() {

            }
        });

    }

    private void showSuccessToast() {
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
    }

    public void changeCameraFeature(View view) {
        cameraOn = !cameraOn;
        if (!cameraOn) {
            turnCameraOff();
            findViewById(R.id.surface_camera).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.surface_camera).setVisibility(View.VISIBLE);
            openCamera();
        }
        activityUI.changeCheck(cameraOn);
    }

    @Override
    public void onSongEnded() {
        lengthOfAudioPlayed = mPlayer.getCurrentPosition();
        postParseVideoFile = wrapUpSong();
        songService.addFieldToUpdate(TIMES_PLAYED);
        userService.addFieldToUpdate(USER_VIEWS);
        isRunning = false;
        ending = true;
        finishSong();
    }

    @Override
    public void songPrepared() {
        activityUI.showPlayButton();
        activityUI.hideLoadingIndicator();
    }

    @Override
    public void openSignIn() {
        SignIn signIn = new SignIn(this, this, this, mGetContent);
        signIn.openSignIn();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            signInViewModel = ViewModelProviders.of(this).get(SignInViewModel.class);
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            signInViewModel.firebaseAuthWithGoogle(account.getIdToken(), new SignInViewModel.FirebaseAuthListener() {
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
                            if (user != null) {
                                switch (funcToCall) {
                                    case PLAYBACK:
                                        playback(findViewById(R.id.sing_song));
                                        break;
                                    case SAVE_FUNC:
                                        saveAndShare(findViewById(R.id.sing_song));
                                        break;
                                }
                            }
                        }

                        @Override
                        public void failedToSearchDatabase() {
                            if (checkForInternet()) {
                                user = new UserInfo(firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), firebaseUser.getUid(), 0, 0);

                                signInViewModel.addNewUserToDatabase(user);
                            }
                        }
                    });
                }

                @Override
                public void onFailure() {
                    showFailure();
                }
            });
        } catch (Exception e) {
//                Toast.makeText(this, context.getResources().getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkForInternet() {
        return Checks.checkForInternetConnection(findViewById(android.R.id.content).getRootView(), this);
    }

    private void showFailure() {
        Toast.makeText(this, getResources().getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void createShareLink(TextView viewById, boolean video) {
        link1 = null;
        Task<ShortDynamicLink> link = ShareLink.createLink(recording, password, video);
        link.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                Uri flowchartLink = task.getResult().getPreviewLink();
                link1 = shortLink.toString();
                viewById.setText(link1);


            } else {
                showFailure(SHARING_ERROR);
                // Error
                // ...
            }
        });
//        activityUI.hideShareItems();
    }

    @Override
    public CharSequence getLink() {
        return link1;
    }

    @Override
    public CharSequence getPassword() {
        return password;
    }

    @Override
    public void setPassword(TextView viewById) {
        link1 = null;
        password = GenerateRandomId.generateRandomPassword();
        viewById.setText(password);
    }

    public interface DeleteRecordingListener {
        void play(Recording mItem);

        void delete(Recording mItem);
    }

    private void downloadFile(String audioPath) {
        createVideoFolder();
        try {
            createVideoFileName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new DownloadFileAsync().execute(audioPath);
    }


    private void createVideoFolder() {
//        File movieFile = activity.getCacheDir();
//        File movieFile = context.getFilesDir();
        mVideoFolder = new File(getFilesDir(), DIRECTORY_NAME);
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private void createVideoFileName() throws IOException {
        String prepend = "exoplayer";
        File videoFile = new File(mVideoFolder, prepend + ".mp4");
        fileName = videoFile.getAbsolutePath();
//        mVideoFile = videoFile;
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(fileName);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            showProgress(progress[0]);
//            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            startBuild();
//            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }


    }



    @SuppressLint("SetTextI18n")
    private void showProgress(String progress) {
        ((TextView) findViewById(R.id.loading_progress)).setText(progress + "%");
    }
}



