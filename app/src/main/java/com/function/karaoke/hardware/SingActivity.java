package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.hardware.fragments.NetworkFragment;
import com.function.karaoke.hardware.storage.StorageAdder;
import com.function.karaoke.hardware.utils.CameraPreview;
import com.function.karaoke.hardware.utils.UrlHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import android.graphics.Movie;
//import org.mortbay.component.Container;

public class SingActivity extends AppCompatActivity implements DownloadCallback<String>,
        DialogBox.CallbackListener {

    public static final String EXTRA_SONG = "EXTRA_SONG";
    private static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 100;
    private static final String TAG = "HELLO WORLD";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final int BACK_CODE = 101;
    private static final int INTERNET_CODE = 102;
    private static final int PICK_CONTACT_REQUEST = 106;
    private static final int MESSAGE_RESULT = 1;
    private static final String PLAYBACK = "playabck";
    private static final String AUDIO_FILE = "audio";
    private static final CharSequence AUDIO_TOKEN = "audioToken";
    private static final CharSequence VIDEO_TOKEN = "videoToken";
    private final int AUDIO_CODE = 1;
    private final int CAMERA_CODE = 2;
    private final int VIDEO_REQUEST = 101;
    CountDownTimer cTimer = null;
    MediaPlayer mPlayer;
    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
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

    private boolean buttonClicked = false;

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
    private boolean isRecording = false;
    private NetworkFragment networkFragment;
    private boolean ending = false;
    private String uniquePath;
    private StorageAdder storageAdder;
    private String path;
    private String videoUrl;
    private boolean playback = false;
    private boolean timerStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deletePreviousVideos();

        setContentView(R.layout.activity_sing);
        deleteCurrentTempFile();
        storageAdder = new StorageAdder();
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

    private void deletePreviousVideos() {
        File dir = new File(getCacheDir(), DIRECTORY_NAME);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
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
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    private void tryLoadSong() {
        song = (DatabaseSong) getIntent().getSerializableExtra(EXTRA_SONG);
        UrlHolder urlParser = new UrlHolder(song);
        networkFragment = NetworkFragment.getDownloadInstance(getSupportFragmentManager(), urlParser);
        networkFragment.getLifecycle().addObserver(new CreateObserver());
        if (null != song) {
            blurAlbumInBackground();
            addArtistToScreen();
            findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public void callback(String result) {
        if (result.equals("yes")) {
            ending = true;
            if (!mKaraokeKonroller.isStopped())
                mKaraokeKonroller.onStop();
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
            addFileToStorageForPlayback(Uri.fromFile(postParseVideoFile));
//            openNewIntent();
        }
    }

    private File wrapUpSong() {
        try {
            stopRecordingAndSong();
            File file = cameraPreview.getVideo();
            return parseVideo(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void stopRecordingAndSong() {
        ending = true;
        if (!mKaraokeKonroller.isStopped()) {
            mKaraokeKonroller.onStop();
            findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
        }
        if (isRecording) {
            cameraPreview.stopRecording();
        }
    }

    private void addFileToStorageForPlayback(Uri path) {
        if (videoUrl == null) {
            final Observer<String> urlObserver = url -> {
                videoUrl = url;
                playback = true;
                buttonClicked = false;
                openNewIntent();

            };
            storageAdder.uploadVideo(path).observe(this, urlObserver);
        } else {
            openNewIntent();
        }
    }

    private void openNewIntent() {
        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(PLAYBACK, videoUrl);
        intent.putExtra(AUDIO_FILE, song.getSongResourceFile());
        startActivity(intent);
    }

    @Override
    public void updateFromDownload(String result) {
        if (result == null) {
            DialogBox dialogBox = DialogBox.newInstance(this, INTERNET_CODE);
            dialogBox.show(getSupportFragmentManager(), "NoticeDialogFragment");

        }
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
//        isRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isChangingConfigurations()) {
            if (!ending) {
                mKaraokeKonroller.onPause();
                isRunning = false;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            findViewById(R.id.play_button).setVisibility(View.GONE);
            findViewById(R.id.switch_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.video_icon).setVisibility(View.INVISIBLE);
            startTimer();
        }
    }

    //start timer function
    void startTimer() {
        timerStarted = true;
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
                findViewById(R.id.open_end_options).setVisibility(View.VISIBLE);
                makeSongNameAndArtistInvisible();
                cameraPreview.startRecording();
                mKaraokeKonroller.onResume();
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
                setProgressBar();
                setPauseButton();
                isRecording = true;
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
                while (!ending && i[0] < mPlayer.getDuration() / 1000 && !restart) {
                    while (!ending && isRunning && i[0] < mPlayer.getDuration() / 1000) {
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

    private void placePopupOnScreen() {
        popup = new PopupWindow(this);
        setPopupAttributes(popup, popupView);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
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
        timerStarted = false;
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
        if (!buttonClicked) {
            buttonClicked = true;
            cameraPreview.stopRecording();
//        cameraPreview.closeCamera();
            popup.dismiss();
//        setContentView(R.layout.activity_sing);
            resetPage();
            restart = true;
            isRunning = false;
            isRecording = false;
            deleteCurrentTempFile();
            resetKaraokeController();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            else {
                tryLoadSong();
            }
//        initiateCamera();
            buttonClicked = false;
        }
    }

    private void resetKaraokeController() {
        if (!mKaraokeKonroller.isStopped())
            mKaraokeKonroller.onStop();
        mKaraokeKonroller = new KaraokeController(getCacheDir().getAbsolutePath() + File.separator + "video recording");
        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read, R.id.camera);
        mPlayer = mKaraokeKonroller.getmPlayer();
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
//                delayForAFewMilliseconds();
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

    private File parseVideo(File mFilePath) throws IOException {
        DataSource channel = new FileDataSourceImpl(mFilePath.getAbsolutePath());
        IsoFile isoFile = new IsoFile(channel);
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        boolean isError = false;
        for (TrackBox trackBox : trackBoxes) {
            TimeToSampleBox.Entry firstEntry = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox().getTimeToSampleBox().getEntries().get(0);
            // Detect if first sample is a problem and fix it in isoFile
            // This is a hack. The audio deltas are 1024 for my files, and video deltas about 3000
            // 10000 seems sufficient since for 30 fps the normal delta is about 3000
            if (firstEntry.getDelta() > 10000) {
                isError = true;
                firstEntry.setDelta(3000);
            }
        }
        File file = getOutputMediaFile();
        String filePath = file.getAbsolutePath();
        if (isError) {
            Movie movie = new Movie();
            for (TrackBox trackBox : trackBoxes) {
                movie.addTrack(new Mp4TrackImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
            }
            movie.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());
            Container out = new DefaultMp4Builder().build(movie);

            //delete file first!
            FileChannel fc = new RandomAccessFile(filePath, "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
            Log.d(TAG, "Finished correcting raw video");
            return file;
        }
        return mFilePath;
    }

    /**
     * Create directory and return file
     * returning video file
     */
    private File getOutputMediaFile() {
        // External sdcard file location
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        // Create storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + "movies" + " directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }

    public void share(View view) {
        File postParseVideoFile = wrapUpSong();
        addFilesToStorageForLinking(Uri.fromFile(postParseVideoFile));
    }

    private void addFilesToStorageForLinking(Uri path) {
        if (videoUrl == null) {
            final Observer<String> urlObserver = url -> {
                videoUrl = url;
                playback = true;
                buttonClicked = false;
                shareLink();

            };
            storageAdder.uploadVideo(path).observe(this, urlObserver);
        } else {
            shareLink();
        }
    }

    //        String link_val = "example://ashira" + this.getPackageName();
//        String body = "<a href=\"" + link_val + "\">" + link_val + "</a>";
//        String data = "Hello I am using this App.\nIt has large numbers of Words meaning with synonyms.\nIts works offline very smoothly.\n\n" + body;
//        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(data));
////        emailIntent.setType("image/jpeg");
//        //File bitmapFile = new File(Environment.getExternalStorageDirectory()+"DCIM/Camera/img.jpg");
//        //myUri = Uri.fromFile(bitmapFile);
////        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///mnt/sdcard/DCIM/Camera/img.jpg"));
//        emailIntent.setData(Uri.parse("mailto:"));
//
//
//        startActivityForResult(Intent.createChooser(emailIntent, "Complete action using:"), PICK_CONTACT_REQUEST);
    private void shareLink() {
        String firstFileWithNewTokenName = song.getSongResourceFile().replace("token", AUDIO_TOKEN);
        String secondFileWithNewTokenName = videoUrl.replace("token", VIDEO_TOKEN);

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.example.com/?audio=" + firstFileWithNewTokenName + "&video=" + secondFileWithNewTokenName))
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
//        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
//        sendIntent.setAction(ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                            sendIntent.setType("text/plain");
//        sendIntent.setPackage("com.whatsapp");
//        sendIntent.setData(Uri.parse("smsto:"));
//        sendIntent.setType("text/html");
//        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, data);
//        sendIntent.putExtra(Intent.EXTRA_HTML_TEXT, body);
                            sendIntent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    Html.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            startActivity(sendIntent);
                        } else {
                            int k = 0;
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
            }
        }

    }

    private class CreateObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void connectListener() {
            networkFragment.startDownload();
        }
    }

}
