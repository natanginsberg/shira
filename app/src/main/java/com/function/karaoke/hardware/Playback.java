package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.core.views.LyricsView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Playback extends AppCompatActivity {

    private final int RECORDING = 0;
    private final int ORIGINAL = 1;

    private static final int EXTERNAL_STOARGE_REQUEST = 101;
    private MediaPlayer mPlayerUrl;
    private MediaPlayer mPlayerPath;
    private Handler mHandler;
    private int ready = 0;

    private List<String> urls = new ArrayList<>();

    private PlayerView playerView;
    private List<SimpleExoPlayer> players = new ArrayList<>();

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
    private File videoFile;
    private String videoUrl;
    private boolean playWhenReady;
    private long playbackPosition;
    private int currentWindow;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private ConcatenatingMediaSource concatenatingMediaSource;

    public void finishPlaying() {
        mPlayerPath.stop();
        mPlayerPath.release();
        mPlayerUrl.stop();
        mPlayerUrl.release();
    }

    // todo add clipping function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        urls.add(getIntent().getStringExtra("fileUrl"));
//        videoFile = new File(path);
//        videoUrl = String.valueOf(Uri.fromFile(videoFile));


        urls.add(getIntent().getStringExtra("url"));
        mPlayerPath = new MediaPlayer();
        mPlayerUrl = new MediaPlayer();
        mHandler = new Handler();

        playerView = findViewById(R.id.surface_view);
//        holder = mPreview.getHolder();
//
//        holder.addCallback(this);
        createTwoPlayers();
        initializePlayer();
//        initializePlayer();

//        mTextureView = findViewById(R.id.surface_camera);
//        mKaraokeKonroller = new KaraokeController(getCacheDir().getAbsolutePath() + File.separator + "video recording");
//        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.words_read, R.id.words_to_read, R.id.camera);
//        mPlayer = mKaraokeKonroller.getmPlayer();
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STOARGE_REQUEST);
//        else {
////            load();
//        }


    }

    private void createTwoPlayers() {
        players.add(new SimpleExoPlayer.Builder(this).build());
        players.add(new SimpleExoPlayer.Builder(this).build());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24 && players.size() == 0) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if (players.size() == 0) {
            initializePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayers();
        }
    }

    private void initializePlayer() {

        for (int i = 0; i < 2; i++) {
            SimpleExoPlayer player = players.get(i);
            if (i == RECORDING) {
                playerView.setPlayer(player);
                player.setPlayWhenReady(false);
//                player.setVolume(0f);
            } else {
                player.setPlayWhenReady(false);
            }
            player.seekTo(currentWindow, playbackPosition);
            MediaSource mediaSource = buildMediaSource(urls.get(i));
            player.prepare(mediaSource, true, false);
            if (i == RECORDING) {
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        ready++;
                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if (ready >= 2) {
                            for (SimpleExoPlayer p : players) {
                                p.setPlayWhenReady(isPlaying);
                            }
                        }
                    }
                });
            }
        }
    }

    private MediaSource buildMediaSource(String url) {
//        DataSource.Factory dataSourceFactory =
//                new DefaultDataSourceFactory(this, "exoplayer-local");
//        return new ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(Uri.parse(videoUrl));
        DataSource.Factory datasourceFactroy = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Shira"));
        return new ProgressiveMediaSource.Factory(datasourceFactroy).createMediaSource(Uri.parse(url));
    }

    private void releasePlayers() {
        for (SimpleExoPlayer player : players)
            if (player != null) {
                playWhenReady = player.getPlayWhenReady();
                playbackPosition = player.getCurrentPosition();
                currentWindow = player.getCurrentWindowIndex();
                player.release();
                player = null;
            }
    }
}
