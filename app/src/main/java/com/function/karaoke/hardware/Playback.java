package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.storage.RecordingService;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.List;


public class Playback extends AppCompatActivity {

    private static final int SAVE_VIDEO = 111;
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    public static final String RECORDING = "recording";

    private static final String AUDIO_TOKEN = "audioToken";
    private static final String VIDEO_TOKEN = "videoToken";
    private static final int RECORDING_URL = 0;

    private int ready = 0;

    private List<String> urls = new ArrayList<>();
    private List<Uri> uris = new ArrayList<>();

    private PlayerView playerView;
    private List<SimpleExoPlayer> players = new ArrayList<>();

    private com.function.karaoke.core.controller.KaraokeController.MyCustomObjectListener listener;

    private boolean playWhenReady;
    private long playbackPosition;
    private int currentWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        playerView = findViewById(R.id.surface_view);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(PLAYBACK)) {
//                urls.add(getIntent().getStringExtra(PLAYBACK));
                uris.add(Uri.parse(getIntent().getStringExtra(PLAYBACK)));
//                urls.add(getIntent().getStringExtra(AUDIO_FILE));
                uris.add(Uri.parse(getIntent().getStringExtra(AUDIO_FILE)));
                createPplayer();
                initializePlayer();
                createTwoPlayers();
                initializePlayer();
            } else if (getIntent().getExtras().containsKey(RECORDING)) {
                Recording recording = (Recording) getIntent().getSerializableExtra(RECORDING);
                urls.add(recording.getRecordingUrl());
                urls.add(recording.getAudioFileUrl());
                createTwoPlayers();
                initializePlayer();
            } else {
                getDynamicLink();
            }
        }
    }

    private void getDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
//                            String originalUrl = deepLink.toString();

//                            addUrls(deepLink.toString());

                            String recordingId = deepLink.getQueryParameter("recId");
                            String recorderId = deepLink.getQueryParameter("uid");
                            addUrls(recordingId, recorderId);
                        } else {
//                            findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    private static final String TAG = "Error";

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    private void addUrls(String recordingId, String recorderId) {
        RecordingService recordingService = new RecordingService();
        final Observer<Recording> recordingObserver = recording -> {
            if (recording != null) {
                urls.add(recording.getRecordingUrl());
                urls.add(recording.getAudioFileUrl());
                createTwoPlayers();
                initializePlayer();
            }
        };
        recordingService.getSharedRecording(recordingId, recorderId).observe(this, recordingObserver);
    }


    private void createTwoPlayers() {
        players.add(new SimpleExoPlayer.Builder(this).build());
        players.add(new SimpleExoPlayer.Builder(this).build());
    }

    private void createPplayer() {
        players.add(new SimpleExoPlayer.Builder(this).build());
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (Util.SDK_INT >= 24 && players.size() == 0) {
//            initializePlayer();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
//        if (players.size() == 0) {
//            initializePlayer();
//        }
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

//    private void initializePlayer() {
//        for (int i = 0; i < 2; i++) {
//            SimpleExoPlayer player = players.get(i);
//            if (i == RECORDING_URL) {
//                playerView.setPlayer(player);
//            }
//            player.setVolume(0.5f);
//            player.setPlayWhenReady(false);
//            player.seekTo(currentWindow, playbackPosition);
//            MediaSource mediaSource;
//            if (urls.size() > 0)
//                mediaSource = buildMediaSource(urls.get(i));
//            else
//                mediaSource = buildMediaSource(uris.get(i));
//            player.prepare(mediaSource, true, false);
//
//            if (i == RECORDING_URL) {
//
//                player.addListener(new Player.EventListener() {
//                    @Override
//                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                        ready++;
//                    }
//
//                    @Override
//                    public void onIsPlayingChanged(boolean isPlaying) {
//                        if (ready >= 2) {
//                            for (SimpleExoPlayer p : players) {
//                                p.setPlayWhenReady(isPlaying);
//                            }
//                        }
//                    }
//                    @Override
//                    public void onPositionDiscontinuity(int reason) {
//                        int currentWindowIndex = player.getCurrentWindowIndex();
//                        if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
//                        }
//                    }
//                });
//
//
//            }
//        }
////        addListener();
//    }

    private void initializePlayer() {
        SimpleExoPlayer player = players.get(0);
        playerView.setPlayer(player);

        player.setVolume(0.5f);
        player.setPlayWhenReady(false);
        player.seekTo(currentWindow, playbackPosition);
        MergingMediaSource mediaSource;
        if (urls.size() > 0) {
            mediaSource = new MergingMediaSource(buildMediaSource(urls.get(0)), buildMediaSource(urls.get(1)));
        } else {
            mediaSource = new MergingMediaSource(buildMediaSource(uris.get(0)), buildMediaSource(uris.get(1)));
        }
        player.prepare(mediaSource, true, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Shira"));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private MediaSource buildMediaSource(String url) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Shira"));
        Uri song = Uri.parse(url);
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(song);
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

    public void backToOptions(View view) {
        super.onBackPressed();
    }

}
