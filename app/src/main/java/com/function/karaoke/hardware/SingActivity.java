package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.hardware.fragments.NetworkFragment;

import java.io.File;

public class SingActivity extends AppCompatActivity implements DownloadCallback<String> {

    private static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 100;
    private final int AUDIO_CODE = 1;
    private final int CAMERA_CODE = 2;
    private final int VIDEO_REQUEST = 101;

    public static final String EXTRA_SONG = "EXTRA_SONG";
    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    private View popupView;
    private PopupWindow popup;
    private DatabaseSong song;

//    private android.hardware.camera2.CameraDevice mCamera;
//    private SurfaceView surfaceView;
//    private SurfaceHolder surfaceHolder;
//    private CameraPreview mPreview;

    private boolean isRunning = true;
    private boolean restart = false;
    private boolean previewRunning = true;

    private TextureView mTextureView;
    private CameraPreview cameraPreview;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//            mTextureView = findViewById(R.id.camera_place);
//            cameraPreview = new CameraPreview(mTextureView, SingActivity.this);
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
    private boolean isRecording;
    private NetworkFragment networkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing);
        deleteCurrentTempFile();
        mTextureView = findViewById(R.id.surface_camera);
        mKaraokeKonroller = new KaraokeController(getCacheDir().getAbsolutePath() + File.separator + "video recording");
        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read, R.id.camera);
        mPlayer = mKaraokeKonroller.getmPlayer();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
        } else {
            initiateCamera();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_CODE);
        else {
            tryLoadSong();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_WRITE_PERMISSION);
        }

    }

    private void initiateCamera() {
        cameraPreview = new CameraPreview(mTextureView, SingActivity.this);
        openCamera();
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

//    private void tryLoadSong() {
//        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
//        UrlParser urlParser = new UrlParser(song);
//        networkFragment = NetworkFragment.getDownloadInstance(getSupportFragmentManager(), urlParser);
//        networkFragment.getLifecycle().addObserver(new CreateObserver());
////        String songFile = getIntent().getStringExtra(EXTRA_SONG);
//        if (null != song)
////            if (mKaraokeKonroller.load(new File(songFile))) {
//            if (mKaraokeKonroller.load(song.getLines(), song.getSongResourceFile())) {
//                blurAlbumInBackground();
//                addArtistToScreen();
//                findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
//            } else
//                finish();
//    }

    private void tryLoadSong() {
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        UrlParser urlParser = new UrlParser(song);
        networkFragment = NetworkFragment.getDownloadInstance(getSupportFragmentManager(), urlParser);
        networkFragment.getLifecycle().addObserver(new CreateObserver());
//        String songFile = getIntent().getStringExtra(EXTRA_SONG);
        if (null != song) {
            blurAlbumInBackground();
            addArtistToScreen();
            findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
        }
    }

    private class CreateObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void connectListener() {
            networkFragment.startDownload();
        }
    }

    @Override
    public void updateFromDownload(String result) {

    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        return null;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {

    }

    @Override
    public void finishDownloading() {
        if (!mKaraokeKonroller.load(song.getLines(), song.getSongResourceFile())) {
            finish();
        }
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

    private void addArtistToScreen() {
        ((TextView) findViewById(R.id.song_name)).setText(song.getTitle());
        ((TextView) findViewById(R.id.song_name_2)).setText(song.getTitle());
        ((TextView) findViewById(R.id.artist_name)).setText(song.getArtist());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AUDIO_CODE:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                    finish();
                else
                    tryLoadSong();
                break;
            case CAMERA_CODE:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    cameraPreview.initiateRecorder();
                break;
            case EXTERNAL_STORAGE_WRITE_PERMISSION:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mKaraokeKonroller.onPause();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        mKaraokeKonroller.onStop();
        if (isRecording) {
            cameraPreview.stopRecording();
        }
        super.onDestroy();
    }

    public void returnToMain(View view) {
        pauseSong(view);
        BackToMainDialogBox back = new BackToMainDialogBox();
        back.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    public void resumeSong() {
        mKaraokeKonroller.onResume();
        isRunning = true;
    }

    public void playMusic(View view) {
        if (mKaraokeKonroller.isPrepared()) {
            restart = false;
            findViewById(R.id.play_button).setVisibility(View.GONE);
            findViewById(R.id.switch_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.video_icon).setVisibility(View.INVISIBLE);
            startTimer();
        }
    }


    //start timer function
    void startTimer() {
        findViewById(R.id.countdown).setVisibility(View.VISIBLE);
        cTimer = new CountDownTimer(3000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                ((TextView) findViewById(R.id.countdown)).setText(Long.toString(millisUntilFinished / 1000));
            }

            public void onFinish() {
                cancelTimer();
                mKaraokeKonroller.onResume();
                makeSongNameAndArtistInvisible();
                findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
                if (!previewRunning) {
                    findViewById(R.id.logo).setVisibility(View.VISIBLE);
                }
                mKaraokeKonroller.setCustomObjectListener(new KaraokeController.MyCustomObjectListener() {
                    @Override
                    public void onSongEnded(boolean songIsOver) {
                        openEndOptions(true);
                    }
                });
                ProgressBar progressBar = findViewById(R.id.progress_bar);
                TextView duration = findViewById(R.id.duration);
                progressBar.setMax(mPlayer.getDuration() / 1000);
                final int[] i = {progressBar.getProgress()};
                Handler hdlr = new Handler();
                StartProgressBar(duration, i, hdlr, progressBar);
                setPauseButton();
                cameraPreview.startRecording();
                isRecording = true;
            }
        };
        cTimer.start();

    }

    private void makeSongNameAndArtistInvisible() {
        findViewById(R.id.song_name_2).setVisibility(View.INVISIBLE);
        findViewById(R.id.artist_name).setVisibility(View.INVISIBLE);
    }

    private void setPauseButton() {
        findViewById(R.id.pause).setVisibility(View.VISIBLE);
    }

    private void StartProgressBar(TextView duration, int[] i, Handler hdlr, ProgressBar progressBar) {
        new Thread(new Runnable() {
            public void run() {
                while (i[0] < mPlayer.getDuration() / 1000 && !restart) {
                    while (isRunning && i[0] < mPlayer.getDuration() / 1000) {
                        i[0] += 1;
                        // Update the progress bar and display the current value in text view
                        hdlr.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(i[0]);
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
            }
        }).start();
    }

    public void openEndOptions(boolean songEnded) {

        RelativeLayout viewGroup = findViewById(R.id.end_options);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.end_song_options, viewGroup);
        if (songEnded) {
//            viewGroup.findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
            popupView.findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
        }

        placePopupOnScreen();
//        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
////                undimBackground();
//                if (mPlayer.getCurrentPosition() != mPlayer.getDuration()) {
////                    mKaraokeKonroller.onResume();
//                }
//            }
//        });
//        applyDim();

    }

//    private void applyDim() {
//        Drawable dim = new ColorDrawable(Color.BLACK);
//        dim.setBounds(0, 0, findViewById(R.id.sing_song).getWidth(), findViewById(R.id.sing_song).getHeight());
//        dim.setAlpha((int) (255 * (float) 0.8));
//        ViewOverlay overlay = findViewById(R.id.sing_song).getOverlay();
////        ViewOverlay headerOverlay = headerView.getOverlay();
////        headerOverlay.add(dim);
//        overlay.add(dim);
//    }

    private void placePopupOnScreen() {
        popup = new PopupWindow(this);
        setPopupAttributes(popup, popupView);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void setPopupAttributes(PopupWindow popup, View layout) {
        int width = ((this.getResources().getDisplayMetrics().widthPixels));
        int height = this.getResources().getDisplayMetrics().heightPixels;
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
    }


    //cancel timer
    void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
    }


    public void openEndOptions(View view) {
        if (mPlayer.getCurrentPosition() / 1000.0 > 2) {
            pauseSong(view);
            openEndOptions(false);
        }
    }

    public void pauseSong(View view) {
        findViewById(R.id.pause).setVisibility(View.INVISIBLE);
        findViewById(R.id.play).setVisibility(View.VISIBLE);
        isRunning = false;
        mKaraokeKonroller.onPause();
        if (isRecording) {
            cameraPreview.pauseRecording();
            isRecording = false;
        }
//        cameraPreview.stopRecording();

    }

    public void resumeSong(View view) {
        startResumeTimer();

    }

    private void startResumeTimer() {
        findViewById(R.id.countdown).setVisibility(View.VISIBLE);
        cTimer = new CountDownTimer(3000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                ((TextView) findViewById(R.id.countdown)).setText(Long.toString(millisUntilFinished / 1000));
            }

            public void onFinish() {
                cancelTimer();
                findViewById(R.id.play).setVisibility(View.INVISIBLE);
                findViewById(R.id.pause).setVisibility(View.VISIBLE);
                isRunning = true;
                mKaraokeKonroller.onResume();
                findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
                mPlayer = mKaraokeKonroller.getmPlayer();
                cameraPreview.resumeRecording();
                isRecording = true;
            }
        };
        cTimer.start();
    }

    public void returnToSong(View view) {
        popup.dismiss();
    }

    public void playAgain(View view) {
        cameraPreview.stopRecording();
        cameraPreview.closeCamera();
        popup.dismiss();
//        setContentView(R.layout.activity_sing);
        resetPage();
        restart = true;
        isRunning = false;
        mKaraokeKonroller.onStop();
        deleteCurrentTempFile();
        mKaraokeKonroller = new KaraokeController(getCacheDir().getAbsolutePath() + File.separator + "video recording");
        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read, R.id.camera);
        mPlayer = mKaraokeKonroller.getmPlayer();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        else {
            tryLoadSong();
        }
        initiateCamera();
    }

    private void resetPage() {
        findViewById(R.id.play_button).setVisibility(View.VISIBLE);
        findViewById(R.id.switch_button).setVisibility(View.VISIBLE);
        findViewById(R.id.video_icon).setVisibility(View.VISIBLE);

        findViewById(R.id.song_name_2).setVisibility(View.VISIBLE);
        findViewById(R.id.artist_name).setVisibility(View.VISIBLE);

        findViewById(R.id.pause).setVisibility(View.INVISIBLE);
        findViewById(R.id.play).setVisibility(View.INVISIBLE);

        ((ProgressBar) findViewById(R.id.progress_bar)).setProgress(0);
        ((TextView) findViewById(R.id.duration)).setText("");

        ((TextView) findViewById(R.id.words_read)).setText("");
        ((TextView) findViewById(R.id.words_to_read)).setText("");
        ((TextView) findViewById(R.id.lyrics)).setText("");


    }

    private void delayForAFewMilliseconds() {
        cTimer = new CountDownTimer(1000, 1) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                cancelTimer();
            }
        };
        cTimer.start();
    }

    private void deleteCurrentTempFile() {
        File dir = getCacheDir();
        File file = new File(dir, "video recording");
        if (file.length() > 0) {
            boolean deleted = file.delete();
        }
    }

    public void openCamera() {
        if (checkCameraHardware(this)) {

            //todo remove. this is for storing on external data

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
            } else {
                cameraPreview.startBackgroundThread();
                delayForAFewMilliseconds();
                if (mTextureView.isAvailable()) {
                    cameraPreview.setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                    cameraPreview.connectCamera();
                } else {
                    mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                }
            }
        }
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
    }
}
