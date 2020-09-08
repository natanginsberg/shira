package com.function.karaoke.hardware;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.function.karaoke.core.views.LyricsView;

import java.io.IOException;


public class Playback extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final int EXTERNAL_STOARGE_REQUEST = 101;
    private MediaPlayer mPlayerUrl;
    private MediaPlayer mPlayerPath;
    private Handler mHandler;
    private String url;
    private String path;

    private com.function.karaoke.core.controller.KaraokeController.MyCustomObjectListener listener;

    private boolean pathPrepared = false;
    private boolean urlPrepared = false;

    // views
    private LyricsView mLyrics;
    private TextView wordsRead;
    private TextView wordsToRead;
//    private ToneRender mToneRender;

    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mUpdater, 20);
            double position = mPlayerUrl.getCurrentPosition() / 1000.0;
            if (position >= mPlayerUrl.getDuration() / 1000.0) {
                listener.onSongEnded(true);
                finishPlaying();
            }
        }
    };
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private boolean ready = false;

    public void finishPlaying() {
        mPlayerPath.stop();
        mPlayerPath.release();
        mPlayerUrl.stop();
        mPlayerUrl.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        path = getIntent().getStringExtra("fileName");
        url = getIntent().getStringExtra("url");
        mPlayerPath = new MediaPlayer();
        mPlayerUrl = new MediaPlayer();
        mHandler = new Handler();

        mPreview = findViewById(R.id.surface_view);


        holder = mPreview.getHolder();

        holder.addCallback(this);


//


//        mTextureView = findViewById(R.id.surface_camera);
//        mKaraokeKonroller = new KaraokeController(getCacheDir().getAbsolutePath() + File.separator + "video recording");
//        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read, R.id.camera);
//        mPlayer = mKaraokeKonroller.getmPlayer();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STOARGE_REQUEST);
        else {
            load();
        }


    }

    public void load() {

        loadUrlAudio(url);
        loadPathAudio(path);
        return;
    }

    private void loadPathAudio(String path) {
        try {
            mPlayerPath.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build());
            mPlayerPath.setDataSource(path);
            mPlayerPath.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayerPath.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    pathPrepared = true;
                }
            });
            mPlayerPath.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUrlAudio(String url) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPlayerUrl.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build());
            } else {
                mPlayerUrl.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            mPlayerUrl.setDataSource(url);
            mPlayerUrl.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    urlPrepared = true;
                }
            });
            mPlayerUrl.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        if (mPlayerPath.isPlaying()) {

            mPlayerPath.pause();

        }
        if (mPlayerUrl.isPlaying()) {
            mPlayerUrl.pause();
        }
        mHandler.removeCallbacks(mUpdater);
    }

    public void onStop() {
        super.onStop();
        mPlayerUrl.release();
        mPlayerPath.release();
    }

    public void onResume() {
        super.onResume();
        if (urlPrepared && pathPrepared) {
            if (mPlayerUrl.getCurrentPosition() / 1000 < 1) {
                while (!pathPrepared || !urlPrepared) {
                }
                mPlayerUrl.start();
                mPlayerUrl.setVolume(0.09f, 0.09f);
                mPlayerPath.start();
                mHandler.post(mUpdater);
            }
        }
    }

    public void playback(View view) {
        if (urlPrepared && pathPrepared && ready) {
                    mPlayerUrl.setVolume(0.15f, 0.21f);
            mPlayerUrl.start();
            mPlayerPath.start();
            mHandler.post(mUpdater);
            findViewById(R.id.play_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
        }
    }

    public void pausePlayback(View view) {
        onPause();
        findViewById(R.id.play_button).setVisibility(View.VISIBLE);
        findViewById(R.id.pause_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        holder = mPreview.getHolder();
//        holder.addCallback(this);
        mPlayerPath.setDisplay(holder);
        ready = true;

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
