package com.function.karaoke.interaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
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
import android.provider.MediaStore;
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

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.activities.Model.UserInfo;
import com.function.karaoke.interaction.storage.AuthenticationDriver;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.storage.SongService;
import com.function.karaoke.interaction.storage.UserService;
import com.function.karaoke.interaction.tasks.NetworkTasks;
import com.function.karaoke.interaction.tasks.OpenCameraAsync;
import com.function.karaoke.interaction.ui.KaraokeWordUI;
import com.function.karaoke.interaction.ui.SingActivityUI;
import com.function.karaoke.interaction.utils.CameraPreview;
import com.function.karaoke.interaction.utils.EarphoneListener;
import com.function.karaoke.interaction.utils.JsonHandler;
import com.function.karaoke.interaction.utils.SignIn;
import com.function.karaoke.interaction.utils.static_classes.Checks;
import com.function.karaoke.interaction.utils.static_classes.GenerateRandomId;
import com.function.karaoke.interaction.utils.static_classes.SyncFileData;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class SingActivity extends AppCompatActivity implements
        DialogBox.CallbackListener, KaraokeController.MyCustomObjectListener,
        SingActivityUI.SignInListener, CameraPreview.CameraErrorListener {

    public static final String EXTRA_SONG = "EXTRA_SONG";
    public static final String RECORDING = "recording";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final String WATERMARK_DIRECTORY_NAME = "watermark";
    private static final String PLAYBACK = "watch";
    private static final String AUDIO_FILE = "audio";
    private static final String DELAY = "delay";
    private static final String LENGTH = "length";
    private static final String USER_INFO = "User";
    private static final String FREE_SHARE_USED = "freeShares";
    private static final String USER_DOWNLOADS = "shares";
    private static final String USER_VIEWS = "views";
    private static final int BACK_CODE = 101;
    private static final int MESSAGE_RESULT = 1;
    private static final int SHARING_ERROR = 100;
    private static final int UPLOAD_ERROR = 101;
    private static final int CAMERA_ERROR = 111;
    private static final int WATCH_RECORDING = 131;
    private static final String CAMERA_ON = "camera on";
    private static final String LOWER_VOLUME = "lower volume";
    private static final String DOWNLOAD = "download";
    private static boolean finished = false;
    private final String TIMES_DOWNLOADED = "timesDownloaded";
    private final String TIMES_PLAYED = "timesPlayed";
    private final int CAMERA_CODE = 2;
    private final int WRITE_CODE = 100;

    private boolean songStarted = false;
    private boolean isRunning = false;
    private boolean buttonClicked = false;
    private boolean isRecording = false;
    private boolean ending = false;
    private boolean cameraOn = false;
    private boolean permissionRequested = false;
    private boolean cameraClosed = false;
    private boolean keepVideo = false;
    private boolean cancelled = false;
    private boolean startTimerStarted = false;
    private boolean resumeTimer = false;
    private boolean earphonesUsed;
    private boolean songUpdated = false;
    private boolean userUpdated = false;
    private boolean downloadRequested = false;
    private boolean fileDownloaded = false;
    private boolean fileDownloadPercentAdded = false;

    private String timeStamp;
    private String songPlayed;
    private String funcToCall;
    private String language;
    private String fileName;
    private String downloadFilePath;

    private long lengthOfAudioPlayed = 0;
    private int delay;
    private int previousPosition = -1;

    private final List<Boolean> voiceRecognizer = new ArrayList<>();

    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    AuthenticationDriver authenticationDriver;
    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
    private DatabaseSong song;
    private SingActivityUI activityUI;
    private File postParseVideoFile;
    private Recording recording;
    private UserInfo user;
    private SongService songService;
    private UserService userService;
    private EarphoneListener earphonesListener;
    private SignIn signIn;
    private KaraokeWordUI karaokeLyricsUI;
    private Handler hdlr;
    private TextureView mTextureView;
    private CameraPreview cameraPreview;
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

    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null) {
                    if (result.getResultCode() == WATCH_RECORDING)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        authenticationDriver = new AuthenticationDriver();
        downloadFilePath = getCacheDir() + DIRECTORY_NAME + "test.mp3";
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
        earphonesListener = new EarphoneListener(this, activityUI);
        if (earphonesListener.getEarphonesUsed()) {
            activityUI.removeEarphonePrompt();
        }
        checkForPermissionAndOpenCamera();
        songStarted = false;

    }

    private File createOutputFile() {
        File dir = new File(Environment.getExternalStorageDirectory(), "DCIM" + File.separator + "Camera");
        if (!dir.exists())
            dir = new File(Environment.getExternalStorageDirectory(), "Movies");
        return new File(dir, "ashira" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date()) + ".mp4");
    }

    private void createWatermarkPath(String watermarkPath) {

        try {
            InputStream in = getAssets().open("beta_2.png");
            OutputStream out = new FileOutputStream(new File(watermarkPath));
            copyFile(in, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
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
        startBuild();

    }

    private void startBuild() {
        mKaraokeKonroller.loadAudio(songPlayed);
        try {
            new DownloadFileAsync().execute(songPlayed);
        } catch (OutOfMemoryError e) {
            activityUI.showOutOfMemory(new SingActivityUI.TimerListener() {
                @Override
                public void timerOver() {

                }
            });
        }
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
        mKaraokeKonroller = new KaraokeController();
        this.karaokeLyricsUI = new KaraokeWordUI(this);
        karaokeLyricsUI.addViews(findViewById(R.id.word_space));
        mKaraokeKonroller.addUIListener(karaokeLyricsUI);
        mPlayer = mKaraokeKonroller.getmPlayer();
        if (noiseIsNotSuppressed()) {
            mPlayer.setVolume(0.3f, 0.3f);
        }
        mKaraokeKonroller.setCustomObjectListener(SingActivity.this);
    }

    private boolean noiseIsNotSuppressed() {
        return !(Util.SDK_INT < 29 || (AcousticEchoCanceler.isAvailable()
                && AutomaticGainControl.isAvailable() && NoiseSuppressor.isAvailable()));
    }

    private void checkForPermissionAndOpenCamera() {
        if (Checks.checkCameraHardware(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionRequested = true;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
            } else {
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
                openCamera();
        } else if (requestCode == WRITE_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                endAndDownload();
            }
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
                showCameraFailure();
            }
        });
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
            findViewById(android.R.id.content).getRootView().post(this::parseWords);
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

    public void watch(View view) {
        if (!buttonClicked) {
            buttonClicked = true;

            if (postParseVideoFile == null)
                postParseVideoFile = wrapUpSong();
            if (postParseVideoFile != null) {
                buttonClicked = false;
                if (userSignedIn()) {
                    openWatchRecording(Uri.fromFile(postParseVideoFile));
                } else {
                    activityUI.openSignInPopup(this, this);
                    funcToCall = PLAYBACK;
                }
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
        intent.putExtra(LOWER_VOLUME, !earphonesUsed && noiseIsNotSuppressed());
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
            showBackDialogBox();
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
        finished = true;
        activityUI.activityFinished();
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
                if (cameraPreview.isMicInUse())
                    finishActivity();
                else {
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
                            mPlayer.setVolume(1f, 1f);
                        isRunning = true;
                        setProgressBar();
                        isRecording = true;
                        activityUI.setScreenForPlayingAfterTimerExpires();
                    }
                }
            }
        };
        cTimer.start();
    }

    private void clearFlags() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }


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


    private void StartProgressBar(TextView duration, int[] i, ProgressBar progressBar) {
        new Thread(() -> {
            while (!ending && i[0] < mPlayer.getDuration() / 1000) {
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

    public void openEndOptions(View view) {
        if (mPlayer != null) {
            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
            openEndOptions(false);
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
            String recordingId = GenerateRandomId.generateRandomId();
            recording = new Recording(false, song, songPlayed, timeStamp,
                    authenticationDriver.getUserUid(), recordingId, delay, lengthOfAudioPlayed, cameraOn);
            return newlyParsedFile;
        } catch (IOException e) {
            buttonClicked = false;
            activityUI.showSaveFail(new SingActivityUI.TimerListener() {
                @Override
                public void timerOver() {

                }
            });
        }
        return null;
    }

    public void download(View view) {
        if (!downloadRequested) {
            buttonClicked = true;
            if (songSung()) {
                if (userSignedIn()) {
                    endAndDownload();
                } else {
                    activityUI.openSignInPopup(this, this);
                    funcToCall = DOWNLOAD;
                }
            } else
                activityUI.showNotEnoughSung();
        }
    }

    private boolean songSung() {
        int occurrences = Collections.frequency(voiceRecognizer, true);
        return occurrences * 5 > voiceRecognizer.size();
    }

    public void endAndDownload() {
        activityUI.showDownloadStarting();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_CODE);
        else {
            downloadRequested = true;
            if (postParseVideoFile == null)
                postParseVideoFile = wrapUpSong();
            if (postParseVideoFile != null)

                if (fileDownloaded)
                    downloadVideo(postParseVideoFile);
        }
        buttonClicked = false;
    }


    private void downloadVideo(File newlyParsedFile) {
        String watermarkPath = getCacheDir() + WATERMARK_DIRECTORY_NAME + "image.mp3";
        createWatermarkPath(watermarkPath);
        File outputFile = createOutputFile();
        String offset = Double.toString((double) delay / (double) 1000);
        String volume = earphonesUsed ? "0.8" : "0.2";
        FFmpeg.executeAsync("-ss " + offset + " -i " + newlyParsedFile + " -i " + watermarkPath + " -i " + downloadFilePath + " -filter_complex \"[1]scale=iw/2:-1[wm];[0:v][wm]overlay=main_w-overlay_w-5:main_h-overlay_h-5[v0];[2:a]volume=" + volume + "[a2];[0:a][a2]amerge=inputs=2[a]\" -map \"[v0]\" -map \"[a]\" -ac 2 -shortest -b:v 1M " + outputFile, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    MediaScannerConnection.scanFile(
                            getApplicationContext(),
                            new String[]{outputFile.getAbsolutePath()},
                            new String[]{"video/mp4"},
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                }
                            });

                    addVideo(outputFile);
                    galleryAddPic(outputFile);
                    activityUI.changeBackground(outputFile.getAbsolutePath().contains("DCIM"));
                } else {
                    downloadRequested = false;
                    activityUI.failBackground();
                }
            }
        });

        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics statistics) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int loadingPercent = fileDownloadPercentAdded ? 50 + (int) (statistics.getTime() * 100 / (2 * lengthOfAudioPlayed)) : (int) (statistics.getTime() * 100 / lengthOfAudioPlayed);
                        activityUI.changeLoadingPercent(loadingPercent, getBaseContext(), recording);
                    }
                });

            }
        });
        addOneToSongsDownload();
        addOneToUserDownloads();
    }

    private void galleryAddPic(File outputFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(outputFile.getAbsolutePath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    public Uri addVideo(File videoFile) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, "My video title");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        return getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void setDelay(Uri uriFromFile) {
        MediaPlayer mp = MediaPlayer.create(this, uriFromFile);
        int duration = mp.getDuration();
        delay = (int) (duration - lengthOfAudioPlayed);
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
                    isRunning = true;
                    mKaraokeKonroller.onResume();
//                customMediaPlayer.startSong();
                    cameraPreview.resumeRecording();
                    mPlayer = mKaraokeKonroller.getmPlayer();
                    isRecording = true;
                    activityUI.setScreenForPlayingAfterRestartTimerExpires();
                    if (cameraPreview.isMicInUse())
                        finishActivity();
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
//        intent.putExtra("song file", fileName);
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


    private void addOneToUserDownloads() {
        if (!userUpdated) {
            userUpdated = true;
            userService.addFieldToUpdate(USER_DOWNLOADS);
            userService.addFieldToUpdate(USER_VIEWS);
            userService.updateUserFields(new UserService.UserUpdateListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure() {
                }
            });
        }
    }

    private void addOneToSongsDownload() {
        if (!songUpdated) {
            songUpdated = true;
            songService.addFieldToUpdate(TIMES_PLAYED);
            songService.addFieldToUpdate(TIMES_DOWNLOADED);
            songService.updateSongData(song.getTitle());
        }

    }

    private File renameJsonPendingFile() {
        return JsonHandler.renameJsonPendingFile(this.getFilesDir());
    }


    private void showCameraFailure() {
        Toast.makeText(this, getString(R.string.camera_fail), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void openSignIn() {
        if (userSignedIn())
            return;
        signIn = new SignIn(this, this, this, mGetContent);
        signIn.openSignIn();
        activityUI.showLoadingIndicator();
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
                                watch(findViewById(R.id.sing_song));
                            else if (funcToCall.equals(DOWNLOAD))
                                endAndDownload();
                    }

                });
            }

            @Override
            public void onFailure() {
                activityUI.showSignInFailed();
            }
        });
    }

    private boolean checkForInternet() {
        return Checks.checkForInternetConnection(findViewById(android.R.id.content).getRootView(), this);
    }

    @Override
    public void setPosition(double position, boolean lineChanged) {
        int pos = karaokeLyricsUI.setPosition(position);
        if (lineChanged || previousPosition != pos)
            voiceRecognizer.add(cameraPreview.getMaxAmplitude() > 1);
        else if (voiceRecognizer.size() > 0 && !voiceRecognizer.get(voiceRecognizer.size() - 1))
            voiceRecognizer.set(voiceRecognizer.size() - 1, cameraPreview.getMaxAmplitude() > 1);
        previousPosition = pos;
//        System.out.println(voiceRecognizer);
    }


    @Override
    public void cameraError() {
        activityUI.showRecordingError(new SingActivityUI.TimerListener() {
            @Override
            public void timerOver() {
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
                byte[] data = new byte[1024];
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
            if (downloadRequested) {
                fileDownloadPercentAdded = true;
                activityUI.changeLoadingPercent(Integer.parseInt(progress[0]) / 2, getBaseContext(), recording);
            }
//        mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {

//            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
//            startBuild();
            fileDownloaded = true;
            if (downloadRequested)
                downloadVideo(postParseVideoFile);
        }
    }
}



