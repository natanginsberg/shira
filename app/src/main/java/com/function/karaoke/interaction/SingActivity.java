package com.function.karaoke.interaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
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

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.activities.Model.SaveItems;
import com.function.karaoke.interaction.activities.Model.UserInfo;
import com.function.karaoke.interaction.storage.AuthenticationDriver;
import com.function.karaoke.interaction.storage.CloudUpload;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.storage.RecordingDelete;
import com.function.karaoke.interaction.storage.SongService;
import com.function.karaoke.interaction.storage.UserService;
import com.function.karaoke.interaction.tasks.NetworkTasks;
import com.function.karaoke.interaction.tasks.OpenCameraAsync;
import com.function.karaoke.interaction.ui.IndicationPopups;
import com.function.karaoke.interaction.ui.KaraokeWordUI;
import com.function.karaoke.interaction.ui.SingActivityUI;
import com.function.karaoke.interaction.utils.Billing;
import com.function.karaoke.interaction.utils.CameraPreview;
import com.function.karaoke.interaction.utils.EarphoneListener;
import com.function.karaoke.interaction.utils.JsonHandler;
import com.function.karaoke.interaction.utils.SignIn;
import com.function.karaoke.interaction.utils.static_classes.Checks;
import com.function.karaoke.interaction.utils.static_classes.GenerateRandomId;
import com.function.karaoke.interaction.utils.static_classes.ShareLink;
import com.function.karaoke.interaction.utils.static_classes.SyncFileData;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

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
import java.util.Date;
import java.util.Locale;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class SingActivity extends AppCompatActivity implements
        DialogBox.CallbackListener, KaraokeController.MyCustomObjectListener,
        SingActivityUI.SignInListener, SingActivityUI.FreeShareListener, SingActivityUI.ShareListener, CameraPreview.CameraErrorListener {

    public static final String EXTRA_SONG = "EXTRA_SONG";
    public static final String RECORDING = "recording";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final String DELAY = "delay";
    private static final String LENGTH = "length";
    private static final String RESULT_CODE = "code";
    private static final String SHARE_FUNC = "share";
    private static final String SAVE_FUNC = "save";
    private static final String USER_INFO = "User";
    private static final String FREE_SHARE_USED = "freeShares";
    private static final String USER_DOWNLOADS = "shares";
    private static final String USER_VIEWS = "views";
    private static final int BACK_CODE = 101;
    private static final int MESSAGE_RESULT = 1;
    private static final int SHARING_ERROR = 100;
    private static final int UPLOAD_ERROR = 101;
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
    private static final String LOWER_VOLUME = "lower volume";
    private final String TIMES_DOWNLOADED = "timesDownloaded";
    private final String TIMES_PLAYED = "timesPlayed";
    private final int CAMERA_CODE = 2;

    private final int ALLOCATED_NUMBER_OF_RECORDINGS = 100;
    private boolean songStarted;
    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    AuthenticationDriver authenticationDriver;
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
    private UserInfo user;
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
    private File jsonFile;
    private int type = -1;
    private RecordingDelete recordingDelete;
    private EarphoneListener earphonesListener;
    private String link1;
    private boolean earphonesUsed;
    private SongService songService;
    private boolean songUpdated = false;
    private boolean userUpdated = false;
    private File mVideoFolder;
    private String fileName;
    private String purchaseId;
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null) {
                    if (result.getResultCode() == SHARE)
                        share(this.getCurrentFocus());
                    else if (result.getResultCode() == WATCH_RECORDING)
                        openWatchRecording(Uri.fromFile(postParseVideoFile));
                    else if (result.getData().getExtras().containsKey("delay")) {
                        delay = result.getData().getIntExtra("delay", delay);
                        activityUI.changeEndWordingToFinishedWatching();
                    } else {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
            });
    private SignIn signIn;
    private boolean lowerVolume = false;
    private KaraokeWordUI karaokeLyricsUI;

    private String downloadFilePath;
    private File outputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        authenticationDriver = new AuthenticationDriver();
        downloadFilePath = getCacheDir() + DIRECTORY_NAME + "test.mp3";
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        outputFile = new File(dir, "test" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date()) + ".mp4");
        createServices();
        getUser();
        getAudioFile();
        createCameraAndRecorderInstance();
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        activityUI = new SingActivityUI(findViewById(android.R.id.content).getRootView(), song, Util.SDK_INT);
        setContentView(R.layout.activity_sing);
        findViewById(android.R.id.content).getRootView().post(this::continueWithSetup);
        mTextureView = findViewById(R.id.surface_camera);

//        createEarphoneReceivers();
        earphonesListener = new EarphoneListener(this);
        checkForPermissionAndOpenCamera();
        songStarted = false;

        if (Util.SDK_INT < 24)
            alertUserThatHeCanNotPause();

    }

    private void getAudioFile() {
        if (getIntent().getExtras().containsKey("song file"))
            fileName = getIntent().getStringExtra("song file");
    }

    private void continueWithSetup() {
        if (checkForInternet()) {
            setKaraokeController();
            loadSong();
            activityUI.setSongInfo();
            activityUI.showAlbumInBackground();
//            blurAlbumInBackground();
            if (song.hasDifferentTones() && fileName == null) {
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
            }
        }
        if (language == null)
            language = Locale.getDefault().getLanguage();
        setLocale(language);
    }

    public void alertUserThatHeCanNotPause() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.no_pause_title_for_older_phones);
        alertBuilder.setMessage(R.string.no_pause_body_for_older_phones);
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
    }


    private void setAudioAndDismissPopup() {
        activityUI.changeLayout();
        activityUI.dismissPopup();
        activityUI.showLoadingIcon();
//        createEarphoneReceivers();
//        if (fileName == null)
//            downloadFile(songPlayed);
        new DownloadFileAsync().execute(songPlayed);
        // todo this is what I added download file it is at the end of the class
//        else
//        startBuild();

    }

    private void startBuild() {
//        findViewById(R.id.loading_progress).setVisibility(View.INVISIBLE);
//        activityUI.showPlayButton();
        mKaraokeKonroller.loadAudio(downloadFilePath);

    }

    public void manTone(View view) {
        songPlayed = song.getSongResourceFile();
        setAudioAndDismissPopup();
    }


    public void womanTone(View view) {
        songPlayed = song.getWomanToneResourceFile();
        setAudioAndDismissPopup();
    }

    public void kidTone(View view) {
        songPlayed = song.getKidToneResourceFile();
        setAudioAndDismissPopup();
    }


    private void setKaraokeController() {
        mKaraokeKonroller = new KaraokeController(this);
//        mKaraokeKonroller.init(this);
        this.karaokeLyricsUI = new KaraokeWordUI(this);
        karaokeLyricsUI.addViews(findViewById(R.id.word_space));
        mKaraokeKonroller.addUIListener(karaokeLyricsUI);
        mPlayer = mKaraokeKonroller.getmPlayer();
        if (!(Util.SDK_INT < 29 || (AcousticEchoCanceler.isAvailable()
                && AutomaticGainControl.isAvailable() && NoiseSuppressor.isAvailable()))) {
            mPlayer.setVolume(0.3f, 0.3f);
            lowerVolume = true;
        }
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
        cameraPreview.subscribeErrorListener(this);
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
//            activityUI.hideLoadingIndicator();
//            activityUI.showPlayButton();
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
                finishActivity();
                break;
            case "ok":
                clearFlags();
                if (activityUI.isPopupOpened()) {
                    activityUI.dismissPopup();
                }
                finishActivity();
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
            if (!cameraPreview.stopRecording()) {
                activityUI.showRecordingError(new SingActivityUI.TimerListener() {
                    @Override
                    public void timerOver() {
                        finishActivity();
                    }
                });
            }
        }
        if (!keepVideo)
            deleteVideo();
        cancelled = true;
        cancelTimer();
        clearFlags();
    }

    public void playback(View view) {
        if (!buttonClicked) {
            buttonClicked = true;

            if (postParseVideoFile == null)
                postParseVideoFile = wrapUpSong();
            buttonClicked = false;
            if (userSignedIn()) {
                openWatchRecording(Uri.fromFile(postParseVideoFile));
            } else {
                activityUI.openSignInPopup(this, this);
                funcToCall = PLAYBACK;
            }
        }
    }

    private boolean userSignedIn() {
        return authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null &&
                !authenticationDriver.getUserEmail().equals("") && user != null;
    }

    private void openWatchRecording(Uri uriFromFile) {
        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(PLAYBACK, uriFromFile.toString());
        intent.putExtra(AUDIO_FILE, songPlayed);
        intent.putExtra(CAMERA_ON, cameraOn);
        intent.putExtra(DELAY, delay);
        intent.putExtra(LENGTH, lengthOfAudioPlayed);
        intent.putExtra(LOWER_VOLUME, lowerVolume);
        mGetContent.launch(intent);
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
        if (songStarted) {
            DialogBox back = DialogBox.newInstance(this, BACK_CODE);
            back.show(getSupportFragmentManager(), "NoticeDialogFragment");
        } else {
            finishActivity();
        }
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
                clearFlags();
                if (activityUI.isPopupOpened()) {
                    activityUI.dismissPopup();
                }
                finishActivity();
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
        cancelTimer();

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


    public void playMusic(View view) {
        activityUI.removeEarphonePrompt();
        if (mKaraokeKonroller.isPrepared() && !startTimerStarted) {
            restart = false;
            activityUI.setSurfaceForRecording(cameraOn);
            startTimer();
        }
    }

    void startTimer() {
        final boolean[] prepared = {false};
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
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
                    clearFlags();
                    refreshWindow();
                } else {
                    songStarted = true;
//                customMediaPlayer.startSong();
                    mKaraokeKonroller.onResume();
                    earphonesUsed = earphonesListener.getEarphonesUsed();
//                    cameraPreview.setUpAudioManager();
                    if (earphonesUsed)
                        if (lowerVolume) {
                            lowerVolume = false;
                            mPlayer.setVolume(1f, 1f);
                        }
                    isRunning = true;
                    setProgressBar();
                    isRecording = true;
                    activityUI.setScreenForPlayingAfterTimerExpires();
                }
            }
        };
        cTimer.start();
    }

    private void clearFlags() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
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
            cameraPreview.releaseRecorder();
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
        boolean errorOccurred = false;
        if (mKaraokeKonroller != null)
            errorOccurred = mKaraokeKonroller.onPause();
        if (Util.SDK_INT >= 24) {
            if (isRecording) {
                cameraPreview.pauseRecording();
            }
            if (postParseVideoFile == null && cameraPreview.getVideo() != null)
                if (errorOccurred) {
                    finishSong();
                } else
                    startPauseTimerToSaveBattery();
        } else {
            finishSong();
        }
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
        clearFlags();
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
        clearFlags();
    }

    private File wrapUpSong() {
        cancelTimer();
        try {
            stopRecordingAndSong();
            File file = cameraPreview.getVideo();
            File newlyParsedFile = SyncFileData.parseVideo(file, getOutputMediaFile());
            setDelay(Uri.fromFile(newlyParsedFile));
            //downloadVideo(newlyParsedFile);
            return newlyParsedFile;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void downloadVideo(File newlyParsedFile) {
        String offset = "00:00:0" + (double) delay / (double) 1000;
        System.out.println("this is the delay " + delay + " this is the offset " + offset);
        String volume = earphonesUsed ? "1" : "0.2";
        FFmpeg.executeAsync("-ss " + offset + " -i " + newlyParsedFile + " -i " + downloadFilePath + " -filter_complex \"[1:a]volume=" + volume + "[a1];[0:a][a1]amerge=inputs=2[a]\" -map 0:v -map \"[a]\" -c:v copy -ac 2 -shortest " + outputFile, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    IndicationPopups.openCheckIndication(getApplicationContext(), getCurrentFocus(), "worked");
                } else
                    IndicationPopups.openXIndication(getApplicationContext(), getCurrentFocus(), "failed");
            }
        });
    }

    private void setDelay(Uri uriFromFile) {
        MediaPlayer mp = MediaPlayer.create(this, uriFromFile);
        int duration = mp.getDuration();
        delay = (int) (duration - lengthOfAudioPlayed);
        long recordingStarted = cameraPreview.getTimeCreated();
        long playStarted = mKaraokeKonroller.getTimerStarted();
        int delay1 = (int) Math.abs(recordingStarted - playStarted);
        clearFlags();

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
        intent.putExtra("song file", fileName);
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

//    private File getTempMediaFile() {
//        File mediaStorageDir = new File(this.getFilesDir(), DIRECTORY_NAME);
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                return null;
//            }
//        }
//        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
//                Locale.getDefault()).format(new Date());
//        File mediaFile;
//
//        mediaFile = new File(mediaStorageDir.getPath() + File.separator
//                + "VID_COMPRESS" + timeStamp + ".mp4");
//        return mediaFile;
//    }

    public void share(View view) {
        if (Util.SDK_INT >= 24) {
            funcToCall = SHARE_FUNC;
            startProcess();
        }
    }

    public void save(View view) {
        if (!itemAcquired) {
            funcToCall = SAVE_FUNC;
            startProcess();
        }
    }

    private void startProcess() {
        if (!buttonClicked) {
            buttonClicked = true;
            if (userSignedIn())

                if (!itemAcquired) {
                    //todo check if user has room only if he is a subcscribed user
                    String date = new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date());
//                    if (date.compareTo(user.getExpirationDate()) > 0 && date.compareTo("20210601_111111") > 0)
//                        startBilling();
//                    else
                    startSaveProcess(false);
                } else {
                    if (funcToCall.equals(SHARE_FUNC))
                        activityUI.openShareOptions(this, user, this, cameraOn);
                }
            else {
                activityUI.openSignInPopup(this, this);
            }
            buttonClicked = false;
        }
    }

//
//
//    private void checkIfUserHasRoomToStore() {
//        RecordingService recordingService = new RecordingService();
//        recordingService.getNumberOfRecordingsFromUID(new RecordingService.NumberListener() {
//            @Override
//            public void recordings(List<Recording> recordings) {
//                if (recordings.size() < ALLOCATED_NUMBER_OF_RECORDINGS) {
//                    checkIfUserHasFreeAcquisition();
//                } else {
//                    View recordingPopup = activityUI.openRecordingsForDelete(SingActivity.this);
//                    RecyclerView recyclerView = (RecyclerView) recordingPopup.findViewById(R.id.recordings_recycler);
//                    recyclerView.setLayoutManager(new LinearLayoutManager(SingActivity.this));
//                    DeleteRecordingRecycler recordAdapter = new DeleteRecordingRecycler(recordings, new DeleteRecordingListener() {
//                        @Override
//                        public void play(Recording mItem) {
//                            playRecording(mItem);
//                        }
//
//                        @Override
//                        public void delete(Recording mItem) {
//                            deleteRecording(mItem);
//                        }
//                    }, getCurrentLanguage());
//                    recyclerView.setAdapter(recordAdapter);
//                }
//            }
//
//            @Override
//            public void failure() {
//
//            }
//        });
//    }

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
                            saveSongToTempJsonFile(false);
                            jsonFile = renameJsonPendingFile();
                            save(jsonFile);
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

    private void saveSongToTempJsonFile(boolean freeShareUsed) {
        if (postParseVideoFile == null)
            postParseVideoFile = wrapUpSong();
        if (postParseVideoFile != null) {
            String recordingId = GenerateRandomId.generateRandomId();
            recording = new Recording(freeShareUsed, song, songPlayed, timeStamp,
                    authenticationDriver.getUserUid(), recordingId, delay, lengthOfAudioPlayed, cameraOn);
            recording.setLowerVolume(lowerVolume);
//        if (!earphonesUsed)
//            recording.earphonesNotUsed();
            JsonHandler.createTempJsonObject(postParseVideoFile, recording, this.getFilesDir());
        }
    }


    @Override
    public void share(View view, boolean video, String password) {
        createShareLink(video, password);

//        createLink(recording.getRecordingId(), recording.getRecorderId(), Integer.toString(recording.getDelay()));
    }

    private void startBilling() {
        billingSession = new Billing(SingActivity.this, (billingResult, purchases) -> {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                keepVideo = true;
                saveSongToTempJsonFile(false);
                for (Purchase purchase : purchases) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
//                          saveSongToJsonFile();
                        itemAcquired = true;
                        purchaseId = purchase.getPurchaseToken();
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
        }, true, this::ready);
        billingSession.subscribeListener(this::onAcknowledgePurchaseResponse);
        buttonClicked = false;
    }

    public void startSaveProcess(boolean freeShareUsed) {
        keepVideo = true;
        itemAcquired = true;
        saveSongToTempJsonFile(freeShareUsed);
        continueWithSaveProcess(freeShareUsed);

    }

    private void continueWithSaveProcess(boolean freeShareUsed) {
        keepVideo = true;
        itemAcquired = true;
        jsonFile = renameJsonPendingFile();
        addOneToSongsDownload();
        addOneToUserDownloads(freeShareUsed);
        save(jsonFile);
        if (funcToCall.equals(SHARE_FUNC))
            activityUI.openShareOptions(this, user, this, cameraOn);
////        else
//            activityUI.
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
        activityUI.openInitialShareOptions(this, user, this, funcToCall);
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
                Toast.makeText(this, getString(R.string.share_failed), Toast.LENGTH_SHORT).show();
                break;
            case UPLOAD_ERROR:
                Toast.makeText(this, "video failed to loadWords", Toast.LENGTH_SHORT).show();
                break;
            case CAMERA_ERROR:
                Toast.makeText(this, getString(R.string.camera_fail), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
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
                    activityUI.showSaveFail(new SingActivityUI.TimerListener() {
                        @Override
                        public void timerOver() {

                        }
                    });
                }

                @Override
                public void onProgress(int progress) {
                    activityUI.showProgress(progress, getBaseContext(), saveItems.getRecording());
                }

                @Override
                public void noConnection() {
                    activityUI.showSlowInternetError(new SingActivityUI.TimerListener() {
                        @Override
                        public void timerOver() {

                        }
                    });

                }
            });
            cloudUpload.saveToCloud(new File(saveItems.getFile()));
            if (funcToCall.equals(SAVE_FUNC))
                activityUI.showSaveStart(new SingActivityUI.TimerListener() {
                    @Override
                    public void timerOver() {

                    }
                });

        }
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
        activityUI.closePopup();
    }

    public void openMonthlySubOption(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            type = 0;
            billingSession.startFlow(MONTHLY_SUB);
            buttonClicked = false;
        }
        activityUI.closePopup();
    }

    //todo called to see if user is out of storage
    public void deleteRecording(Recording mItem) {
//        List<Recording> deleting = new ArrayList<>();
//        deleting.add(mItem);
//        recordingDelete = new RecordingDelete(this::deleteRecording, deleting);
    }

    private void deleteRecording() {
//        NetworkTasks.deleteFromWasabi(recordingDelete, new NetworkTasks.DeleteListener() {
//            @Override
//            public void onSuccess() {
//                showSuccessToast();
//                activityUI.dismissRecordings();
//                checkIfUserHasFreeAcquisition();
//
//            }
//
//            @Override
//            public void onFail() {
//
//            }
//        });

    }

    private void showSuccessToast() {
//        Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
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
        activityUI.setPrepared();
//        activityUI.showPlayButton();
//        activityUI.hideLoadingIndicator();
    }

    @Override
    public void openSignIn() {
        if (userSignedIn())
            return;
        signIn = new SignIn(this, this, this, mGetContent);
        signIn.openSignIn();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        signIn.handleSignInResult(completedTask, findViewById(android.R.id.content).getRootView(), new SuccessFailListener() {
            @Override
            public void onSuccess() {
                activityUI.showGoodSuccessSignIn(new SingActivityUI.TimerListener() {
                    @Override
                    public void timerOver() {
                        user = signIn.getUser();
                        if (user != null)
                            if (funcToCall.equals(PLAYBACK))
                                playback(findViewById(R.id.sing_song));
                            else if (funcToCall.equals(SHARE_FUNC))
                                share(findViewById(R.id.sing_song));
                            else
                                save(findViewById(R.id.sing_song));
                    }

                });
            }

            @Override
            public void onFailure() {
                showFailure();
            }
        });
    }

    private boolean checkForInternet() {
        return Checks.checkForInternetConnection(findViewById(android.R.id.content).getRootView(), this);
    }

    private void showFailure() {
        Toast.makeText(this, getResources().getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
    }

    private void createShareLink(boolean video, String password) {
        activityUI.showPopupLoadingIndicator();
        link1 = null;
        Task<ShortDynamicLink> link = ShareLink.createLink(recording, password, video);
        link.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                Uri flowchartLink = task.getResult().getPreviewLink();
                link1 = shortLink.toString();
                sendDataThroughIntent(link1);
                activityUI.hidePopupLoadingIndicator();


            } else {
                showFailure(SHARING_ERROR);
                // Error
                // ...
            }
        });
//        activityUI.hideShareItems();
    }

    @Override
    public void setPassword(TextView viewById) {
        link1 = null;
        String password = GenerateRandomId.generateRandomPassword();
        viewById.setText(password);
    }

    private void ready() {
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
                userService.addSubscriptionType(changeUserTypeListener, FAILED_TO_RECORD_SUB_TYPE, purchaseId);
                user.setSubscriptionType(FAILED_TO_RECORD_SUB_TYPE);
            }
            startSaveProcess(false);
        } else {
            if (user.getSubscriptionType() != NOT_PAYING_MEMBER) {
                userService.addSubscriptionType(changeUserTypeListener, NOT_PAYING_MEMBER, purchaseId);
                user.setSubscriptionType(NOT_PAYING_MEMBER);
            }
            startSubscriptionPath();
        }

    }

    private void onAcknowledgePurchaseResponse(BillingResult billingResult) {
        userService.addSubscriptionType(new UserService.UserUpdateListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        }, type, purchaseId);
        user.setSubscriptionType(type);
        continueWithSaveProcess(false);
    }


    @Override
    public void setPosition(double position) {
        karaokeLyricsUI.setPosition(position);
    }


    @Override
    public void cameraError() {
        activityUI.showRecordingError(new SingActivityUI.TimerListener() {
            @Override
            public void timerOver() {
//                finishActivity();
            }
        });
    }

    @Override
    public void recorderError() {
        activityUI.showRecordingError(new SingActivityUI.TimerListener() {
            @Override
            public void timerOver() {
//                finishActivity();
            }
        });
    }


//    private void startDownload() {
//        String url = "http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg";
//        new DownloadFileAsync().execute(url);
//    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//        showDialog(DIALOG_DOWNLOAD_PROGRESS);
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
                OutputStream output = new FileOutputStream(downloadFilePath);
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
//        mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {

//            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            startBuild();
        }
    }
}



