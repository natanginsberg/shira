package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
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
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.CloudUpload;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.tasks.OpenCameraAsync;
import com.function.karaoke.hardware.ui.SingActivityUI;
import com.function.karaoke.hardware.utils.Billing;
import com.function.karaoke.hardware.utils.CameraPreview;
import com.function.karaoke.hardware.utils.Checks;
import com.function.karaoke.hardware.utils.GenerateRandomId;
import com.function.karaoke.hardware.utils.JsonHandler;
import com.function.karaoke.hardware.utils.SyncFileData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.squareup.picasso.Picasso;

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
    private static final String RESULT_CODE = "code";

    private static final int BACK_CODE = 101;
    private static final int MESSAGE_RESULT = 1;
    private static final int SHARING_ERROR = 100;
    private static final int UPLOAD_ERROR = 101;
    private static final int SAVE = 101;
    private static final int SHARE = 102;
    private static final int CAMERA_ERROR = 111;
    private final int CAMERA_CODE = 2;
    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    AuthenticationDriver authenticationDriver;
    private String recordingId;
    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
    private PopupWindow popup;
    private DatabaseSong song;
    private boolean isRunning = true;
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
    private boolean itemBought = false;
    private boolean keepVideo = false;
    private File postParseVideoFile;
    private Recording recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authenticationDriver = new AuthenticationDriver();
        createCameraAndRecorderInstance();
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        activityUI = new SingActivityUI(findViewById(android.R.id.content).getRootView(), song);

        setContentView(R.layout.activity_sing);

        mTextureView = findViewById(R.id.surface_camera);
        mKaraokeKonroller = new KaraokeController();
        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read);
        mPlayer = mKaraokeKonroller.getmPlayer();

        recordingId = GenerateRandomId.generateRandomId();
        checkForPermissionAndOpenCamera();
        tryLoadSong();
        activityUI.showPlayButton();
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


    private void tryLoadSong() {

//        customMediaPlayer = new CustomMediaPlayer(this, song.getSongResourceFile());
        if (Checks.checkForInternetConnection(this, getSupportFragmentManager(), getApplicationContext())) {
//        checkForInternetConnection();
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
            if (!keepVideo)
                deleteVideo();
            finish();
        } else if (result.equals("ok")) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DialogBox back = DialogBox.newInstance(this, BACK_CODE);
        back.show(getSupportFragmentManager(), "NoticeDialogFragment");
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

            mKaraokeKonroller.onStop();
//            customMediaPlayer.onStop();
        }
        if (isRecording) {
            cameraPreview.stopRecording();
            isRecording = false;
        }
        activityUI.removeResumeOptionFromPopup();
    }


    private void blurAlbumInBackground() {
//        Picasso.get()
//                .load(song.getImageResourceFile())
//                .placeholder(R.drawable.ic_cover_empty)
//                .fit()
//                .into((ImageView) findViewById(R.id.album_cover));
        GetBlurImage getBlurImage = new GetBlurImage();
        getBlurImage.execute();
//        View view = findViewById(R.id.words);
//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                Bitmap blurredBitmap = null;
//                try {
//                    blurredBitmap = BlurBuilder.blur(getBaseContext(), Picasso.get().load(song.getImageResourceFile()).get());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                view.setBackground(new BitmapDrawable(Resources.getSystem(), blurredBitmap));
//            }
//        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void returnToMain(View view) {
        if (!buttonClicked) {
            buttonClicked = true;
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
        final boolean[] prepared = {false};
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
        if (songEnded)
            lengthOfAudioPlayed = mPlayer.getDuration();
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
        if (mPlayer != null && mPlayer.getCurrentPosition() / 1000.0 > 2) {
            lengthOfAudioPlayed = mPlayer.getCurrentPosition();
            pauseSong(view);
            openEndOptions(false);
        }
    }

    public void pauseSong(View view) {
        activityUI.songPaused();
        isRunning = false;
        mKaraokeKonroller.onPause();
        if (isRecording) {
            cameraPreview.pauseRecording();
        }
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
            if (!keepVideo)
                deleteVideo();
            resetFields();
            cameraPreview.stopRecording();
            popup.dismiss();
            activityUI.resetPage();
            //todo deal with deleting
//            deletePreviousVideos();
            resetKaraokeController();
            tryLoadSong();
            buttonClicked = false;
        }
    }

    private void deleteVideo() {
        cameraPreview.getVideo().delete();
    }

    private void resetFields() {
        itemBought = false;
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
//        if (!ending) {
//            saveSongToJsonFile();
//        }

        if (authenticationDriver.isSignIn())
            if (!itemBought)
                startBilling(SHARE);
            else
                share();
        else
            launchSignIn(SHARE);

    }

//    private void saveSongToJsonFile() {
//        postParseVideoFile = wrapUpSong();
//        recording = new Recording(song, timeStamp,
//                authenticationDriver.getUserUid(), recordingId, delay);
//        JsonHandler.createJsonObject(postParseVideoFile, recording, this.getFilesDir());
//    }

    private void saveSongToTempJsonFile() {
        postParseVideoFile = wrapUpSong();
        recording = new Recording(song, timeStamp,
                authenticationDriver.getUserUid(), recordingId, delay);
        JsonHandler.createTempJsonObject(postParseVideoFile, recording, this.getFilesDir());
    }

    private void share() {

        shareLink(recording.getRecordingId(), recording.getRecorderId(), Integer.toString(recording.getDelay()));
    }

    private void share(File jsonFile) {
        try {
            SaveItems saveItems = JsonHandler.getDatabaseFromInputStream(getFileInputStream(jsonFile));
            saveToCloud(saveItems);
            shareLink(saveItems.getRecording().getRecordingId(), saveItems.getRecording().getRecorderId(),
                    Integer.toString(saveItems.getRecording().getDelay()));
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
                            itemBought = true;
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
                Toast.makeText(this, "video failed to load", Toast.LENGTH_SHORT).show();
                break;
            case CAMERA_ERROR:
                Toast.makeText(this, "Camera failed to open", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void shareLink(String recordingId, String userId, String delay) {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
//                setLongLink(Uri.parse("https://singJewish.page.link/?link=https://www.example.com/&recId=" + recordingId + "&uid=" + authenticationDriver.getUserUid() + "&delay=" + delay))
                .setLink(Uri.parse("https://www.example.com/?recId=" + recordingId + "&uid=" + userId + "&delay=" + delay))
                .setDomainUriPrefix("https://singjewish.page.link")
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

    private void sendDataThroughIntent(String link) {
        String data = "Listen to me sing\n" + link;
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
//        if (!ending) {
//            saveSongToJsonFile();
//        }
        if (authenticationDriver.isSignIn())
            if (!itemBought) startBilling(SAVE);
//            else save();
            else
                launchSignIn(SAVE);
    }

//    private void save() {
//
//        keepVideo = true;
////        File postParseVideoFile = wrapUpSong();
//        saveToCloud(postParseVideoFile);
//    }

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
        mGetContent.launch(new Intent(this, SignInActivity.class).putExtra(RESULT_CODE, code));
    }

    //    private void saveToCloud(Uri path, View view1) {
    private void saveToCloud(SaveItems saveItems) {
        if (!fileSaved) {
            fileSaved = true;
            CloudUpload cloudUpload = new CloudUpload(saveItems.getRecording(), this.getFilesDir(), saveItems.getArtist(), new CloudUpload.UploadListener() {
                @Override
                public void onSuccess(File file) {
                    deleteVideo(file);
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

    private class GetBlurImage extends AsyncTask<String, Void, GetBlurImage.Result> {
        GetBlurImage.Result result = null;


        @Override
        protected Result doInBackground(String... params) {
            try {
                result = new Result(BlurBuilder.blur(getBaseContext(), Picasso.get().load(song.getImageResourceFile()).get()));
                return result;
            } catch (IOException e) {
                return new Result(e);
            }

        }

        @Override
        protected void onPostExecute(GetBlurImage.Result result) {
            findViewById(R.id.words).setBackground(new BitmapDrawable(Resources.getSystem(), result.bitmap));
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        private class Result {
            public Bitmap bitmap;
            public Exception exception;

            public Result(Bitmap bitmap) {
                this.bitmap = bitmap;
            }

            public Result(Exception exception) {
                this.exception = exception;
            }
        }
    }


}



