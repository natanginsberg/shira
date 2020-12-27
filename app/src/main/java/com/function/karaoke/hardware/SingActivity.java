package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.core.utility.BlurBuilder;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.SaveItems;
import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.CloudUpload;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.UserService;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.tasks.OpenCameraAsync;
import com.function.karaoke.hardware.ui.SingActivityUI;
import com.function.karaoke.hardware.utils.Billing;
import com.function.karaoke.hardware.utils.CameraPreview;
import com.function.karaoke.hardware.utils.Checks;
import com.function.karaoke.hardware.utils.GenerateRandomId;
import com.function.karaoke.hardware.utils.JsonHandler;
import com.function.karaoke.hardware.utils.ShareLink;
import com.function.karaoke.hardware.utils.SyncFileData;
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
    private final int CAMERA_CODE = 2;
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
            cameraPreview.setTextureView(mTextureView);
            cameraPreview.setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            cameraPreview.connectCamera();
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

    private final Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Bitmap is loaded, use image here
            Bitmap bm = BlurBuilder.blur(getBaseContext(), bitmap);
            findViewById(R.id.album_cover).setBackground((new BitmapDrawable(getResources(), bm)));
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            Toast.makeText(getBaseContext(), "failed to loadWords album", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    };
    private boolean isRecording = false;
    private boolean ending = false;
    private boolean timerStarted = false;
    private boolean fileSaved = false;
    private boolean cameraOn = false;
    private boolean permissionRequested = false;
    private String timeStamp;
    private long lengthOfAudioPlayed;
    private SingActivityUI activityUI;
    private int delay;

    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null) {
                    if (result.getResultCode() == SAVE) {
                        saveRecordingToTheCloud(this.getCurrentFocus());
                    } else if (result.getResultCode() == SHARE) {
                        share(this.getCurrentFocus());
                    }
                }
            });
    private Billing billingSession;
    private boolean itemAcquired = false;
    private boolean keepVideo = false;
    private File postParseVideoFile;
    private Recording recording;
    private DisplayMetrics metrics;
    private boolean measured = false;
    private BroadcastReceiver mReceiver;
    private boolean Microphone_Plugged_in = false;
    private boolean ml;
    private boolean bluetoothConnected = false;
    private boolean bluetoothConnectionExists = false;
    private boolean earphonesUsed = false;
    private UserInfo user;
    private SignInViewModel signInViewModel;
    private UserService userService;
    private String songPlayed;
    private boolean cancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authenticationDriver = new AuthenticationDriver();
        createCameraAndRecorderInstance();
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        activityUI = new SingActivityUI(findViewById(android.R.id.content).getRootView(), song);
        setContentView(R.layout.activity_sing);
        setKaraokeController();
        loadSong();
        blurAlbumInBackground();

        mTextureView = findViewById(R.id.surface_camera);
        recordingId = GenerateRandomId.generateRandomId();
        checkForPermissionAndOpenCamera();
        if (song.hasDifferentTones()) {
            activityUI.openTonePopup(SingActivity.this);
        } else {
            songPlayed = song.getSongResourceFile();
            mKaraokeKonroller.loadAudio(songPlayed);
            createEarphoneReceivers();

        }
    }

    private void createEarphoneReceivers() {
        createHeadphoneReceiver();
        createBluetoothReceiver();
        checkIfHeadsetIsPairedAlready();
    }


    private void setAudioAndDismissPopup() {
        mKaraokeKonroller.loadAudio(songPlayed);
        activityUI.dismissPopup();
        createEarphoneReceivers();
    }

    public void manTone(View view) {
        songPlayed = song.getSongResourceFile();
        setAudioAndDismissPopup();
        createEarphoneReceivers();
    }


    public void womanTone(View view) {
        songPlayed = song.getWomanToneResourceFile();
        setAudioAndDismissPopup();
        createEarphoneReceivers();
    }

    public void kidTone(View view) {
        songPlayed = song.getKidToneResourceFile();
        setAudioAndDismissPopup();
        createEarphoneReceivers();
    }


    private void setKaraokeController() {
        mKaraokeKonroller = new KaraokeController();
        mKaraokeKonroller.init(this);
        mKaraokeKonroller.addViews(findViewById(R.id.root), R.id.lyrics, R.id.words_to_read,
                R.id.words_to_read_2, R.id.word_space, R.id.words_to_read_3);
        mPlayer = mKaraokeKonroller.getmPlayer();
    }

    private void createBluetoothReceiver() {
        mReceiver = new BroadcastReceiver() {
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
        registerReceiver(mReceiver, receiverFilter);
    }

    private void hideHeadphonesIfNothingElseConnected() {
        if (!Microphone_Plugged_in && !bluetoothConnectionExists && !bluetoothConnected)
            findViewById(R.id.headphones).setVisibility(View.INVISIBLE);
    }

    private void showHeadphones() {
        findViewById(R.id.headphones).setVisibility(View.VISIBLE);
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
                        Microphone_Plugged_in = false;
                        hideHeadphonesIfNothingElseConnected();
//                        Toast.makeText(getApplicationContext(), "microphone not plugged in", Toast.LENGTH_LONG).show();
                        if (!bluetoothConnectionExists && !bluetoothConnected)
                            promptUserToConnectEarphones();
                    }
                    if (Integer.valueOf(iii) == 1) {
                        Microphone_Plugged_in = true;
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
        DialogBox attachEarphones = DialogBox.newInstance(this, EARPHONES);
        attachEarphones.show(getSupportFragmentManager(), "NoticeDialogFragment");
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

    private void createCameraAndRecorderInstance() {
        cameraPreview = new CameraPreview(mTextureView, SingActivity.this, Checks.checkCameraHardware(this), this);
    }


    private void loadSong() {
        if (song.getLines() == null) {
            if (Checks.checkForInternetConnection(this, getSupportFragmentManager(), getApplicationContext())) {
                NetworkTasks.parseWords(song, new NetworkTasks.ParseListener() {
                    @Override
                    public void onSuccess() {
                        activityUI.showPlayButton();
                        if (!mKaraokeKonroller.loadWords(song.getLines())) {
                            finish();
                        }
                    }

                    @Override
                    public void onFail() {
                        finish();
                    }
                });
            }
        } else {
            activityUI.showPlayButton();
            if (!mKaraokeKonroller.loadWords(song.getLines())) {
                finish();
            }
        }
        if (null != song) {

//            addArtistToScreen();
            activityUI.addArtistToScreen();
            activityUI.setBackgroundColor();
//            findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public void callback(String result) {
        if (result.equals("yes")) {
            ending = true;
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
            finish();
        } else if (result.equals("ok")) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        pauseSong(findViewById(R.id.sing_song));
        DialogBox back = DialogBox.newInstance(this, BACK_CODE);
        back.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    public void playback(View view) {
        if (!buttonClicked) {
//            view.setBackgroundColor(getResources().getColor(R.color.gold, getTheme()));
            buttonClicked = true;
            if (postParseVideoFile == null)
                postParseVideoFile = wrapUpSong();
            buttonClicked = false;
            openNewIntent(Uri.fromFile(postParseVideoFile));
        }
//        view.setBackgroundColor(getResources().getColor(R.color.appColor, getTheme()));
    }

    private void openNewIntent(Uri uriFromFile) {

        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(PLAYBACK, uriFromFile.toString());
        if (earphonesUsed)
            intent.putExtra(AUDIO_FILE, songPlayed);
        intent.putExtra(DELAY, delay);
        intent.putExtra(LENGTH, lengthOfAudioPlayed);
        startActivity(intent);
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

    private void stopRecordingAndSong() {
        ending = true;
        if (mKaraokeKonroller.isPlaying()) {

            mKaraokeKonroller.onStop();
//            customMediaPlayer.onStop();
        }
        if (isRecording) {
            cameraPreview.stopRecording();
            isRecording = false;
            cameraPreview.closeCamera();
        }
        activityUI.removeResumeOptionFromPopup();
    }

    private void blurAlbumInBackground() {
        if (song.getImageResourceFile() != null) {
            final ImageView tv = findViewById(R.id.album_cover);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!ending && !permissionRequested) {
            pauseSong(this.getCurrentFocus());
        }
        super.onPause();
    }

    public void returnToMain(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            pauseSong(view);
            showBackDialogBox();
            buttonClicked = false;
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
            }
            buttonClicked = false;
        }
    }

    public void resumeSong() {
        mKaraokeKonroller.onResume();
        isRunning = true;
    }

    public void playMusic(View view) {
        if (mKaraokeKonroller.isPrepared() && !timerStarted) {
            restart = false;
            activityUI.setSurfaceForRecording(cameraOn);
            startTimer();
        }
    }

    void startTimer() {
        activityUI.clearLyricsScreen();
        if (Microphone_Plugged_in || bluetoothConnectionExists || bluetoothConnected)
            earphonesUsed = true;
        ending = false;
        timerStarted = true;
        final boolean[] prepared = {false};
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cTimer = new CountDownTimer(3800, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                activityUI.displayTimeForCountdown(millisUntilFinished);
                if (millisUntilFinished / 1000 >= 1 && !prepared[0]) {
                    prepared[0] = true;
                    cameraPreview.prepareMediaRecorder();
                }
            }

            public void onFinish() {
                cancelTimer();
                if (!cancelled) {
//                customMediaPlayer.startSong();
                    mKaraokeKonroller.onResume();
                    mKaraokeKonroller.setCustomObjectListener(new KaraokeController.MyCustomObjectListener() {
                        @Override
                        public void onSongEnded() {

//                        cameraPreview.stopRecording();
                            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
                            isRunning = false;
                            ending = true;
                            pauseSong(findViewById(R.id.sing_song));
                            openEndOptions(true);
                        }
                    });
                    isRunning = true;
                    setProgressBar();
                    isRecording = true;
                    activityUI.setScreenForPlayingAfterTimerExpires();
                }
            }
        };
        cTimer.start();
    }

    private void setProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        TextView duration = findViewById(R.id.duration);
        progressBar.setMax(mPlayer.getDuration() / 1000);
        final int[] i = {progressBar.getProgress()};
        Handler hdlr = new Handler();
        StartProgressBar(duration, i, hdlr, progressBar);
    }


    private void StartProgressBar(TextView duration, int[] i, Handler hdlr, ProgressBar
            progressBar) {
        new Thread(() -> {
            while (!ending && i[0] < mPlayer.getDuration() / 1000 && !restart) {
                while (!ending && isRunning && i[0] < mPlayer.getDuration() / 1000) {
                    i[0] += 1;
                    // Update the progress bar and display the current value in text view
                    hdlr.post(() -> {
                        progressBar.setProgress(i[0]);
                        int minutes = (mPlayer.getCurrentPosition() / 1000) / 60;
                        int seconds = (mPlayer.getCurrentPosition() / 1000) % 60;
                        @SuppressLint("DefaultLocale") String text = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
                        duration.setText(text);
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
            activityUI.undimBackground();
        });
    }

    //cancel timer
    void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
        timerStarted = false;
    }

    public void openEndOptions(View view) {
        if (mPlayer != null && mPlayer.getCurrentPosition() / 1000.0 > 2) {
            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
            pauseSong(view);
            openEndOptions(false);
        }
    }

    public void pauseSong(View view) {
        if (isRunning)
            activityUI.songPaused();
        isRunning = false;
        mKaraokeKonroller.onPause();
        if (isRecording) {
            cameraPreview.pauseRecording();
        }
        if (postParseVideoFile == null)
            startPauseTimerToSaveBattery();
    }

    private void startPauseTimerToSaveBattery() {
        cTimer = new CountDownTimer(120000, 110000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                cancelTimer();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                openEndOptions(true);
                postParseVideoFile = wrapUpSong();
            }
        };
        cTimer.start();
    }

    public void resumeSong(View view) {
        startResumeTimer();

    }

//    public void stopSong

    private void startResumeTimer() {
        cTimer = new CountDownTimer(3800, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                activityUI.displayTimeForCountdown(millisUntilFinished);
            }

            public void onFinish() {
                cancelTimer();
                if (!cancelled) {
                    restart = false;
                    isRunning = true;
                    mKaraokeKonroller.onResume();
//                customMediaPlayer.startSong();
                    cameraPreview.resumeRecording();
                    mPlayer = mKaraokeKonroller.getmPlayer();
                    isRecording = true;
                    activityUI.setScreenForPlayingAfterTimerExpires();
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
            if (!keepVideo)
                deleteVideo();
            activityUI.makeLoadingBarVisible();
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
        finish();
        startActivity(intent);
    }

    private void deleteVideo() {
        if (cameraPreview.getVideo() != null)
            cameraPreview.getVideo().delete();
    }


    public void openCamera() {
        ((SwitchCompat) findViewById(R.id.camera_toggle_button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean turnCameraOff) {
                if (!turnCameraOff) {
                    turnCameraOff();
                    findViewById(R.id.surface_camera).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.surface_camera).setVisibility(View.VISIBLE);
                    turnCameraOn();
                }
            }
        });
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

    }

    private void turnCameraOn() {
        openCamera();
    }

    private void turnCameraOff() {
        cameraPreview.closeCamera();
        cameraOn = false;
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

    public void share(View view) {
        buttonClicked = true;
        if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null &&
                !authenticationDriver.getUserEmail().equals(""))

            if (!itemAcquired)
                checkIfUserHasFreeAcquisition(SHARE);
            else
                share();

        else
            launchSignIn(SHARE);
        buttonClicked = false;
    }

    private void checkIfUserHasFreeAcquisition(int funcToCall) {
        userService = new UserService(new DatabaseDriver(), authenticationDriver);
        userService.getUserFromDatabase(new SignInViewModel.FreeShareListener() {
            @Override
            public void hasFreeShare(boolean freeShare) {
                if (freeShare) {
                    keepVideo = true;
                    itemAcquired = true;
                    userService.addOneToUserShares(new UserService.UserUpdateListener() {
                        @Override
                        public void onSuccess() {
                            saveSongToTempJsonFile();
                            File jsonFile = renameJsonPendingFile();
                            share(jsonFile);
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(getParent(), "We are sorry but you can not continue at this time", Toast.LENGTH_LONG).show();
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
        recording = new Recording(song, songPlayed, timeStamp,
                authenticationDriver.getUserUid(), recordingId, delay, lengthOfAudioPlayed);
        if (earphonesUsed)
            recording.earphonesUsed();
        JsonHandler.createTempJsonObject(postParseVideoFile, recording, this.getFilesDir());
    }

    private void share() {

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
    }

    private void share(File jsonFile) {
        try {
            SaveItems saveItems = JsonHandler.getDatabaseFromInputStream(getFileInputStream(jsonFile));
            saveToCloud(saveItems);

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

//            createLink(saveItems.getRecording().getRecordingId(), saveItems.getRecording().getRecorderId(),
//                    Integer.toString(saveItems.getRecording().getDelay()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startBilling(int funcToCall) {
        billingSession = new Billing(SingActivity.this, new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        keepVideo = true;
                        saveSongToTempJsonFile();
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
            }
        }, true, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    File jsonFile = renameJsonPendingFile();
                    userService.addOneToUserShares(new UserService.UserUpdateListener() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                    if (funcToCall == SAVE)
                        save(jsonFile);
                    else
                        share(jsonFile);
                }
            }
        });

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
                Toast.makeText(getApplicationContext(), "E-Mail sent successfully", Toast.LENGTH_LONG).show();
            } else {
                Toast toast = Toast.makeText(this, "Email failed to send", Toast.LENGTH_LONG);
                toast.show();
            }
        }

    }

    public void saveRecordingToTheCloud(View view) {
        if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null && !authenticationDriver.getUserEmail().equals("")) {
            if (!itemAcquired)
                checkIfUserHasFreeAcquisition(SAVE);
        } else
            launchSignIn(SAVE);
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
            });
            cloudUpload.saveToCloud(new File(saveItems.getFile()));
        }
    }

    private void deleteVideo(File file) {
        if (file.delete()) {
            int k = 0;
        }
    }


}



