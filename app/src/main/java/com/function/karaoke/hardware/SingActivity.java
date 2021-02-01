package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
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
import com.function.karaoke.hardware.utils.JsonHandler;
import com.function.karaoke.hardware.utils.static_classes.GenerateRandomId;
import com.function.karaoke.hardware.utils.static_classes.ShareLink;
import com.function.karaoke.hardware.utils.static_classes.SyncFileData;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SingActivity extends AppCompatActivity implements
        DialogBox.CallbackListener {

    public static final String EXTRA_SONG = "EXTRA_SONG";
    public static final String RECORDING = "recording";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final String DELAY = "delay";
    private static final String LENGTH = "length";
    private static final String RESULT_CODE = "code";

    private static final int BACK_CODE = 101;
    private static final int MESSAGE_RESULT = 1;
    private static final int SHARING_ERROR = 100;
    private static final int UPLOAD_ERROR = 101;
    private static final int SAVE = 101;
    private static final int SHARE = 102;
    private static final int CAMERA_ERROR = 111;
    private static final String SING_ACTIVITY = "sing activity";
    private static final String CALLBACK = "callback";
    private static final int EARPHONES = 121;
    private static final int WATCH_RECORDING = 131;
    private static final String CAMERA_ON = "camera on";
    private static final int YEARLY_SUB = 1;
    private static final int MONTHLY_SUB = 0;
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
    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    AuthenticationDriver authenticationDriver;
    private String recordingId;
    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
    private PopupWindow popup;
    private DatabaseSong song;
    private boolean isRunning = false;
    private boolean restart = false;
    private boolean buttonClicked = false;
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
    private boolean isRecording = false;
    private boolean ending = false;
    private boolean timerStarted = false;
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
    private DisplayMetrics metrics;
    private boolean measured = false;
    private BroadcastReceiver mReceiver;
    private boolean microphonePluggedIn = false;
    private boolean ml;
    private boolean bluetoothConnected = false;
    private boolean bluetoothConnectionExists = false;
    private boolean earphonesUsed = false;
    private UserInfo user;
    private SignInViewModel signInViewModel;
    private UserService userService;
    private String songPlayed;
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null) {
                    if (result.getResultCode() == SAVE) {
                        saveRecordingToTheCloud(this.getCurrentFocus());
                    } else if (result.getResultCode() == SHARE) {
                        saveAndShare(this.getCurrentFocus());
                    } else if (result.getResultCode() == WATCH_RECORDING) {
                        openNewIntent(Uri.fromFile(postParseVideoFile));
                    }
                }
            });
    private boolean cancelled;
    private String language;
    private Handler hdlr;
    private boolean cameraClosed = false;
    private boolean startTimerStarted = false;
    private boolean resumeTimer = false;
    private boolean songStarted = false;
    private BroadcastReceiver bReceiver;
    private boolean prompted = false;
    private File compressedFile;
    private File jsonFile;
    private int type = -1;
    private int ALLOCATED_NUMBER_OF_RECORDINGS = 30;
    private RecordingDelete recordingDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCorrectLanguage();
        authenticationDriver = new AuthenticationDriver();
        createCameraAndRecorderInstance();
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        activityUI = new SingActivityUI(findViewById(android.R.id.content).getRootView(), song, Util.SDK_INT);
        setContentView(R.layout.activity_sing);
        setKaraokeController();
        loadSong();
        activityUI.setSongInfo();
        blurAlbumInBackground();

        mTextureView = findViewById(R.id.surface_camera);
        recordingId = GenerateRandomId.generateRandomId();
        createEarphoneReceivers();
        checkForPermissionAndOpenCamera();
        if (song.hasDifferentTones()) {
            activityUI.openTonePopup(song, SingActivity.this);
        } else {
            songPlayed = song.getSongResourceFile();
            mKaraokeKonroller.loadAudio(songPlayed);
            createEarphoneReceivers();
        }
        if (Util.SDK_INT < 24)
            alertUserThatHeCanNotPause();

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

    private void getCorrectLanguage() {
        if (getIntent().getExtras().containsKey("language")) {
            String phoneLanguage = Locale.getDefault().getLanguage();
            setLocale(phoneLanguage);
        }
//        String language = getResources().getConfiguration().getLocales().get(0).getLanguage();
//        setLocale("en");
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


    private void createEarphoneReceivers() {
        createHeadphoneReceiver();
        createBluetoothReceiver();
        checkIfHeadsetIsPairedAlready();
    }


    private void setAudioAndDismissPopup() {
        activityUI.changeLayout();
        activityUI.dismissPopup();
//        createEarphoneReceivers();
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
    }

    private void createBluetoothReceiver() {
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    if (isBluetoothHeadsetConnected(device))
                    bluetoothConnected = true;
                    showHeadphones();
                    //Device found
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                    bluetoothConnected = true;
                    showHeadphones();
                    //Device is now connected
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    bluetoothConnected = false;
                    bluetoothConnectionExists = false;
                    hideHeadphonesIfNothingElseConnected();
                    //Device has disconnected
                }
            }
        };
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        receiverFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        receiverFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bReceiver, receiverFilter);
    }

    private void hideHeadphonesIfNothingElseConnected() {
//        if (!microphonePluggedIn && !bluetoothConnectionExists && !bluetoothConnected)
//            findViewById(R.id.headphones).setVisibility(View.INVISIBLE);
    }

    private void showHeadphones() {
//        findViewById(R.id.headphones).setVisibility(View.VISIBLE);
    }

    private void createHeadphoneReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                int iii = 2;
                if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    iii = intent.getIntExtra("state", -1);
                    if (Integer.valueOf(iii) == 0) {
                        microphonePluggedIn = false;
                        hideHeadphonesIfNothingElseConnected();
//                        Toast.makeText(getApplicationContext(), "microphone not plugged in", Toast.LENGTH_LONG).show();
                        if (!bluetoothConnectionExists && !bluetoothConnected && !prompted)
                            promptUserToConnectEarphones();
                    }
                    if (Integer.valueOf(iii) == 1) {
                        microphonePluggedIn = true;
                        showHeadphones();
//                        Toast.makeText(getApplicationContext(), "microphone plugged in", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, receiverFilter);
    }

    private void promptUserToConnectEarphones() {
//        prompted = true;
//        DialogBox attachEarphones = DialogBox.newInstance(this, EARPHONES);
//        attachEarphones.show(getSupportFragmentManager(), "NoticeDialogFragment");
        Toast toast = Toast.makeText(this, getResources().getString(R.string.attach_earphones), Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();

    }

    private void checkIfHeadsetIsPairedAlready() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            int[] profiles = {BluetoothProfile.A2DP, BluetoothProfile.HEADSET, BluetoothProfile.HEALTH};
            for (int profileId : profiles) {
                if (BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(profileId)) {
                    bluetoothConnectionExists = true;
                    showHeadphones();
                    break;
                }
            }
        }
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
            if (Checks.checkForInternetConnection(this, getSupportFragmentManager(), getApplicationContext())) {
                NetworkTasks.parseWords(song, new NetworkTasks.ParseListener() {
                    @Override
                    public void onSuccess() {

                        if (!mKaraokeKonroller.loadWords(song.getLines())) {
                            if (activityUI.isPopupOpened()) {
                                activityUI.dismissPopup();
                            }
                            finish();
                        }
                        activityUI.hideLoadingIndicator();
                        activityUI.showPlayButton();
                    }

                    @Override
                    public void onFail() {
                        finish();
                    }
                });
            }
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
            } else launchSignIn(WATCH_RECORDING);
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
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!measured) {
                        measured = true;
                        Picasso.get()
                                .load(song.getImageResourceFile())
                                .into(target);
                    }
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

    public void resumeSong() {
        mKaraokeKonroller.onResume();
        isRunning = true;
    }

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
                    mKaraokeKonroller.setCustomObjectListener(new KaraokeController.MyCustomObjectListener() {
                        @Override
                        public void onSongEnded() {

//                        cameraPreview.stopRecording();
                            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
                            postParseVideoFile = wrapUpSong();
                            isRunning = false;
                            ending = true;
                            finishSong();
                        }
                    });
                    earphonesUsed = microphonePluggedIn || bluetoothConnectionExists || bluetoothConnected;
                    //todo unregister
//                    unregisterReceivers();
                    isRunning = true;
                    setProgressBar();
                    isRecording = true;
                    activityUI.setScreenForPlayingAfterTimerExpires();
                }
            }
        };
        cTimer.start();
    }

    private void unregisterReceivers() {
        unregisterReceiver(mReceiver);
        unregisterReceiver(bReceiver);
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
            // todo make this better not working
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
        popup = activityUI.getPopup();
        popup.setOnDismissListener(() -> {
            if (ending) finish();
            activityUI.popupClosed();
            activityUI.undimBackground();
        });
    }

    //cancel timer

    public void openEndOptions(View view) {
        if (mPlayer != null && mPlayer.getCurrentPosition() / 1000.0 > 2) {
            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
            if (Util.SDK_INT >= 24) {
                pauseSong(view);
                openEndOptions(false);
            } else {
                finishSong();
            }
        }
    }

    public void pauseSong(View view) {
        if (isRunning)
            activityUI.songPaused();
        isRunning = false;
        mKaraokeKonroller.onPause();
        if (Util.SDK_INT >= 24) {
            if (isRecording) {
                cameraPreview.pauseRecording();
            }
            if (postParseVideoFile == null && cameraPreview.getVideo() != null)
                startPauseTimerToSaveBattery();
        } else
            finishSong();

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
            if (lengthOfAudioPlayed == 0) {
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
        if (activityUI.isPopupOpened())
            activityUI.removeResumeOptionFromPopup();
    }

    private File wrapUpSong() {
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
                //todo check for null pointer when creating postparsefile
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

                if (!itemAcquired)
                    checkIfUserHasRoomToStore();

                else
//                    saveAndShare();
                    activityUI.showShareItems();

            else
                launchSignIn(SHARE);
            buttonClicked = false;
        }
    }

    private void checkIfUserHasRoomToStore() {
        RecordingService recordingService = new RecordingService();
        recordingService.getNumberOfRecordingsFromUID(new RecordingService.NumberListener() {
            @Override
            public void recordings(List<Recording> recordings) {
                if (recordings.size() < ALLOCATED_NUMBER_OF_RECORDINGS) {
                    checkIfUserHasFreeAcquisition(SHARE);
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

    private void checkIfUserHasFreeAcquisition(int funcToCall) {
        userService = new UserService(new DatabaseDriver(), authenticationDriver);
        userService.getUserFromDatabase(new SignInViewModel.FreeShareListener() {
            @Override
            public void hasFreeAcquisition(boolean freeShare) {
                if (freeShare) {
                    keepVideo = true;
                    itemAcquired = true;
                    userService.addOneToUserShares(new UserService.UserUpdateListener() {
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
                    startBilling(funcToCall);
            }
        });
    }

    private void saveSongToTempJsonFile() {
        if (postParseVideoFile == null)
            postParseVideoFile = wrapUpSong();
        //todo check that post parse video isn't null
        recording = new Recording(song, songPlayed, timeStamp,
                authenticationDriver.getUserUid(), recordingId, delay, lengthOfAudioPlayed, cameraOn, true);
//        if (!earphonesUsed)
//            recording.earphonesNotUsed();
        JsonHandler.createTempJsonObject(postParseVideoFile, recording, this.getFilesDir());
    }

    public void share(View view) {

//        createLink(recording.getRecordingId(), recording.getRecorderId(), Integer.toString(recording.getDelay()));

        Task<ShortDynamicLink> link = ShareLink.createLink(recording);
        link.addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();
                    Uri flowchartLink = task.getResult().getPreviewLink();
                    String link = shortLink.toString();
                    sendDataThroughIntent(link);


                } else {
                    showFailure(SHARING_ERROR);
                    // Error
                    // ...
                }
            }
        });
        activityUI.hideShareItems();
    }

    private void startBilling(int funcToCall) {
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
        }, true, new Billing.ReadyListener() {
            @Override
            public void ready() {
                if (billingSession.isSubscribed()) {
                    keepVideo = true;
                    saveSongToTempJsonFile();
                    jsonFile = renameJsonPendingFile();
                    addOneToSongsDownload();
                    save(jsonFile);
                    activityUI.showShareItems();
                } else
                    activityUI.showSubscribeOptions();

            }
        });
        billingSession.subscribeListener(new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                userService.addSubscriptionType(new UserService.UserUpdateListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure() {

                    }
                }, type);
                jsonFile = renameJsonPendingFile();
                addOneToSongsDownload();
                save(jsonFile);
                activityUI.showShareItems();
            }
        });
        buttonClicked = false;
    }

    private void addOneToSongsDownload() {
        SongService.addDownloadToSong(song.getTitle());
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

    public void saveRecordingToTheCloud(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("")) {
                if (!itemAcquired)
                    checkIfUserHasFreeAcquisition(SAVE);
            } else {
                launchSignIn(SAVE);
                buttonClicked = false;
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
        mGetContent.launch(new Intent(this, SignInActivity.class).putExtra(RESULT_CODE, code).putExtra(SING_ACTIVITY, true).putExtra(CALLBACK, true));
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
                    activityUI.showProgress(progress, getBaseContext());
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
        recordingDelete = new RecordingDelete(new RecordingDelete.SetupListener() {
            @Override
            public void setup() {
                deleteRecording();
            }
        }, deleting);
    }

    private void deleteRecording() {
        NetworkTasks.deleteFromWasabi(recordingDelete, new NetworkTasks.DeleteListener() {
            @Override
            public void onSuccess() {
                showSuccessToast();
                activityUI.dismissRecordings();
                checkIfUserHasFreeAcquisition(SHARE);

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


    public interface DeleteRecordingListener {
        void play(Recording mItem);

        void delete(Recording mItem);
    }
}



