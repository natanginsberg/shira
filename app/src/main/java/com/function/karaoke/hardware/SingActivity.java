package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.view.TextureView;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.StorageAdder;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.ui.SingActivityUI;
import com.function.karaoke.hardware.utils.CameraPreview;
import com.function.karaoke.hardware.utils.GenerateRandomId;
import com.function.karaoke.hardware.utils.SyncFileData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SingActivity extends AppCompatActivity implements
        DialogBox.CallbackListener {

    public static final String EXTRA_SONG = "EXTRA_SONG";
    public static final String RECORDING = "recording";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final int BACK_CODE = 101;
    private static final int INTERNET_CODE = 102;
    private static final int MESSAGE_RESULT = 1;
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final int SHARING_ERROR = 100;
    private static final int UPLOAD_ERROR = 101;
    private static final String DELAY = "delay";

    private final int CAMERA_CODE = 2;
    private String recordingId;
    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
    private PopupWindow popup;
    private DatabaseSong song;

    private boolean isRunning = true;
    private boolean restart = false;
    private boolean previewRunning = true;

    private TextureView mTextureView;
    private CameraPreview cameraPreview;

    private boolean buttonClicked = false;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
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
    private boolean isRecording = false;
    private boolean ending = false;
    private StorageAdder storageAdder;
    private boolean timerStarted = false;
    private String timeStamp;
    AuthenticationDriver authenticationDriver;
    private boolean fileSaved = false;
    private boolean cameraOn = false;
    private boolean permissionRequested = false;
    private long lengthOfAudioPlayed;
    private SingActivityUI activityUI;
    private int delay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authenticationDriver = new AuthenticationDriver();
        deletePreviousVideos();
        createCameraAndRecorderInstance();
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        activityUI = new SingActivityUI(findViewById(android.R.id.content).getRootView(), song);

        setContentView(R.layout.activity_sing);

        mTextureView = findViewById(R.id.surface_camera);
        mKaraokeKonroller = new KaraokeController();
        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read);
        mPlayer = mKaraokeKonroller.getmPlayer();

        recordingId = GenerateRandomId.generateRandomId();


        if (checkCameraHardware(this)) {
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

        tryLoadSong();
    }

    private void createCameraAndRecorderInstance() {
        cameraPreview = new CameraPreview(mTextureView, SingActivity.this, checkCameraHardware(this), this);
    }

    private void deletePreviousVideos() {
        File dir = new File(getCacheDir(), DIRECTORY_NAME);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
        }
    }


    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    private void tryLoadSong() {

//        customMediaPlayer = new CustomMediaPlayer(this, song.getSongResourceFile());
        checkForInternetConnection();
        NetworkTasks.parseWords(song, new NetworkTasks.ParseListener() {
            @Override
            public void onSuccess() {
                if (!mKaraokeKonroller.load(song.getLines(), song.getSongResourceFile())) {
                    finish();
                }
            }

            @Override
            public void onFail() {
                finish();
            }
        });
        if (null != song) {
            blurAlbumInBackground();
//            addArtistToScreen();
            activityUI.addArtistToScreen();
            activityUI.setBackgroundColor();
//            findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
        }
    }

    private void checkForInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            // If no connectivity, cancel task and update Callback with null data.
            DialogBox dialogBox = DialogBox.newInstance(this, INTERNET_CODE);
            dialogBox.show(getSupportFragmentManager(), "NoticeDialogFragment");
        }
    }

    @Override
    public void callback(String result) {
        if (result.equals("yes")) {
            ending = true;
            if (!mKaraokeKonroller.isStopped()) {
                mKaraokeKonroller.onStop();
//                customMediaPlayer.onStop();
            }
            if (isRecording) {
                cameraPreview.stopRecording();
            }
            finish();
        } else if (result.equals("ok")) {
            finish();
        }
    }

    public void playback(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
            File postParseVideoFile = wrapUpSong();
            buttonClicked = false;
            openNewIntent(Uri.fromFile(postParseVideoFile));
        }
    }

    private void openNewIntent(Uri uriFromFile) {

        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(PLAYBACK, uriFromFile.toString());
        intent.putExtra(AUDIO_FILE, song.getSongResourceFile());
        intent.putExtra(DELAY, delay);
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
        if (!mKaraokeKonroller.isStopped()) {
            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
            mKaraokeKonroller.onStop();
//            customMediaPlayer.onStop();
            findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
        }
        if (isRecording) {
            cameraPreview.stopRecording();
        }
        activityUI.removeResumeOptionFromPopup();
    }


    private void blurAlbumInBackground() {
//        Picasso.get()
//                .load(song.getImageResourceFile())
//                .placeholder(R.drawable.ic_cover_empty)
//                .fit()
//                .into((ImageView)findViewById(R.id.album_cover));
////        View view = findViewById(R.id.words);
//        BlurBuilder blurBuilder = new BlurBuilder();
//        Bitmap blurredBitmap = null;
//        try {
//            blurredBitmap = blurBuilder.blur(view.getContext(), Picasso.get().load(song.getImageResourceFile()).get());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        view.setBackground(new BitmapDrawable(Resources.getSystem(), blurredBitmap));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionRequested = false;
        switch (requestCode) {
            case CAMERA_CODE:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    cameraPreview.initiateRecorder();
                else
//                    initiateCamera();
                    openCamera();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!isChangingConfigurations()) {
            if (!ending && !permissionRequested) {
                pauseSong(this.getCurrentFocus());
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void returnToMain(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
//            mKaraokeKonroller.onPause();
            DialogBox back = DialogBox.newInstance(this, BACK_CODE);
            back.show(getSupportFragmentManager(), "NoticeDialogFragment");
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

    //start timer function
    void startTimer() {
        ending = false;
        timerStarted = true;
        findViewById(R.id.countdown).setVisibility(View.VISIBLE);
        final boolean[] prepared = {false};
        cTimer = new CountDownTimer(3500, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished / 1000 >= 1) {
                    ((TextView) findViewById(R.id.countdown)).setText(Long.toString(millisUntilFinished / 1000));
                } else {
                    ((TextView) findViewById(R.id.countdown)).setText(R.string.start);
//                    if (!prepared[0])
//                        cameraPreview.prepareMediaRecorder(cameraOn);
//                    cameraPreview.start();
                }
                if (millisUntilFinished / 1000 >= 1 && !prepared[0]) {
                    prepared[0] = true;
                    cameraPreview.prepareMediaRecorder(cameraOn);
                }
            }

            public void onFinish() {
                cancelTimer();
//                while (!customMediaPlayer.isPlayerReady()) {
//                }


                activityUI.clearLyricsScreen();
//                customMediaPlayer.startSong();
                mKaraokeKonroller.onResume();
                mKaraokeKonroller.setCustomObjectListener(songIsOver -> openEndOptions(true));
                isRunning = true;
                setProgressBar();
                isRecording = true;
                activityUI.setScreenForPlayingAfterTimerExpires();
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


    private void StartProgressBar(TextView duration, int[] i, Handler hdlr, ProgressBar progressBar) {
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
        popup.setOnDismissListener(() -> activityUI.undimBackground());

    }


    //cancel timer
    void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
        timerStarted = false;
    }

    public void openEndOptions(View view) {
        if (mPlayer.getCurrentPosition() / 1000.0 > 2) {
            pauseSong(view);
            openEndOptions(false);
        }
    }

    public void pauseSong(View view) {
        activityUI.songPaused();
        isRunning = false;
        mKaraokeKonroller.onPause();
//        customMediaPlayer.pauseSong();
        if (isRecording) {
            cameraPreview.pauseRecording();
//            isRecording = false;
        }
//        cameraPreview.stopRecording();
    }

    public void resumeSong(View view) {
        startResumeTimer();

    }

//    public void stopSong

    private void startResumeTimer() {
        findViewById(R.id.countdown).setVisibility(View.VISIBLE);
        cTimer = new CountDownTimer(3500, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished / 1000 >= 1)
                    ((TextView) findViewById(R.id.countdown)).setText(Long.toString(millisUntilFinished / 1000));
                else ((TextView) findViewById(R.id.countdown)).setText(R.string.start);

            }

            public void onFinish() {
                cancelTimer();
                restart = false;
                isRunning = true;
                mKaraokeKonroller.onResume();
//                customMediaPlayer.startSong();
                cameraPreview.resumeRecording();
                mPlayer = mKaraokeKonroller.getmPlayer();
                isRecording = true;
                activityUI.setScreenForPlayingAfterTimerExpires();
            }
        };
        cTimer.start();
    }

    public void returnToSong(View view) {
        popup.dismiss();
    }

    public void playAgain(View view) {
        if (!buttonClicked) {
            resetFields();
            cameraPreview.stopRecording();
            popup.dismiss();
            activityUI.resetPage();
//            resetPage();
            deletePreviousVideos();
            resetKaraokeController();
            tryLoadSong();
            buttonClicked = false;
        }
    }

    private void resetFields() {
        buttonClicked = true;
        restart = false;
        isRunning = false;
        isRecording = false;
        recordingId = GenerateRandomId.generateRandomId();
        ending = true;
        fileSaved = false;
    }

    private void resetKaraokeController() {
        if (!mKaraokeKonroller.isStopped())
            mKaraokeKonroller.onStop();
        mKaraokeKonroller = new KaraokeController();
        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read);
        mPlayer = mKaraokeKonroller.getmPlayer();
    }

    public void openCamera() {
        cameraPreview.startBackgroundThread();
        if (mTextureView.isAvailable()) {
            cameraPreview.setTextureView(mTextureView);
            cameraPreview.setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            cameraPreview.connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        cameraOn = true;
    }

    public void toggleVideo(View view) {
        if (previewRunning) {
            previewRunning = false;
            turnCameraOff();
            findViewById(R.id.surface_camera).setVisibility(View.INVISIBLE);
        } else {
            previewRunning = true;
            findViewById(R.id.surface_camera).setVisibility(View.VISIBLE);
            turnCameraOn();
        }
    }

    private void turnCameraOn() {
        openCamera();
    }

    private void turnCameraOff() {
        cameraPreview.closeCamera();
        cameraOn = false;
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(getCacheDir(), DIRECTORY_NAME);
        // Create storage directory if it does not exist
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
        File postParseVideoFile = wrapUpSong();
//        addFilesToStorageForLinking(Uri.fromFile(postParseVideoFile));
        saveToCloud(postParseVideoFile);
        shareLink();
    }

    private void showFailure(int error) {
        switch (error) {
            case SHARING_ERROR:
                Toast.makeText(this, "sharing failed", Toast.LENGTH_SHORT).show();
                break;
            case UPLOAD_ERROR:
                Toast.makeText(this, "video failed to load", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void shareLink() {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.example.com/?recId=" + recordingId + "&uid=" + authenticationDriver.getUserUid() + "&delay=" + delay))
                .setDomainUriPrefix("https://singJewish.page.link")
                // Set parameters
                // ...
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            String link = flowchartLink.toString();


                            String body = "<a href=\"" + link + "\">" + link + "</a>";
                            String data = "Listen to me sing\n" + body;
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.setType("text/plain");
                            sendIntent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    Html.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            startActivity(sendIntent);
                        } else {
                            showFailure(SHARING_ERROR);
                            // Error
                            // ...
                        }
                    }
                });
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
        File postParseVideoFile = wrapUpSong();
        saveToCloud(postParseVideoFile);
    }

    //    private void saveToCloud(Uri path, View view1) {
    private void saveToCloud(File path) {
        if (!fileSaved) {
            fileSaved = true;
            storageAdder = new StorageAdder(Uri.fromFile(path));
            storageAdder.uploadRecording(new Recording(song, timeStamp,
                    authenticationDriver.getUserUid(), recordingId, delay), new StorageAdder.UploadListener() {
                @Override
                public void onSuccess() {
//                    parentView.findViewById(R.id.upload_progress_wheel).setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure() {
//                    ((ProgressBar) parentView.findViewById(R.id.upload_progress_wheel)).setBackgroundColor(Color.BLACK);
                }
            });

        }
    }


//    //todo think about compressing the files

}
