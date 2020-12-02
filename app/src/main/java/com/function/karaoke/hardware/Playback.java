package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.storage.RecordingService;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;


public class Playback extends AppCompatActivity implements TimeBar.OnScrubListener, PlaybackStateListener {

    public static final String RECORDING = "recording";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final int RECORDING_URL = 0;
    private static final String DELAY = "delay";
    private static final String EARPHONES_USED = "empty";

    private List<String> urls = new ArrayList<>();
    private List<Uri> uris = new ArrayList<>();

    private PlayerView playerView;
    private List<SimpleExoPlayer> players = new ArrayList<>();

    private long playbackPosition;
    private int currentWindow;
    private TextView positionView;
    private StringBuilder formatBuilder;
    private Formatter formatter;

    private PlaybackStateListener playbackStateListener;
    private boolean dynamicLink = false;

    private int delay;
    private boolean locked = false;
    private boolean earphonesUsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        playerView = findViewById(R.id.surface_view);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(PLAYBACK)) {
                uris.add(Uri.parse(getIntent().getStringExtra(PLAYBACK)));
                if (getIntent().getExtras().containsKey(AUDIO_FILE))
                    uris.add(Uri.parse(getIntent().getStringExtra(AUDIO_FILE)));
                else
                    earphonesUsed = true;
                delay = getIntent().getIntExtra(DELAY, 0);
                createTwoPlayers();
                initializePlayer();
            } else if (getIntent().getExtras().containsKey(RECORDING)) {
                Recording recording = (Recording) getIntent().getSerializableExtra(RECORDING);
                urls.add(recording.getRecordingUrl());
                if (!recording.getAudioFileUrl().equals(EARPHONES_USED))
                    urls.add(recording.getAudioFileUrl());
                delay = recording.getDelay();
                createTwoPlayers();
                initializePlayer();
            } else {
                dynamicLink = true;
                getDynamicLink();
            }
        }
        setScrubbingFields();
        addListeners();
//        playbackStateListener = new PlaybackStateListener();
    }

    private void setScrubbingFields() {
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
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
                            delay = Integer.parseInt(deepLink.getQueryParameter("delay"));
                            addUrls(recordingId, recorderId);
                        }
                        // findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
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
                    }
                });
    }

    private void addUrls(String recordingId, String recorderId) {
        RecordingService recordingService = new RecordingService();
        final Observer<Recording> recordingObserver = recording -> {
            if (recording != null) {
                urls.add(recording.getRecordingUrl());
                if (!recording.getAudioFileUrl().equals(EARPHONES_USED))
                    urls.add(recording.getAudioFileUrl());
                createTwoPlayers();
                initializePlayer();
            }
        };
        recordingService.getSharedRecording(recordingId, recorderId).observe(this, recordingObserver);
    }


    private void createTwoPlayers() {
        players.add(new SimpleExoPlayer.Builder(this).build());
        if (!earphonesUsed)
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
        if (players == null) {
            players = new ArrayList<>();
            createTwoPlayers();
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
        releasePlayers();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayers();
        }
    }

    private void initializePlayer() {
        for (int i = 0; i < players.size(); i++) {
            SimpleExoPlayer player = players.get(i);
            if (i == RECORDING_URL) {
                playerView.setPlayer(player);
                player.setVolume(0.7f);

//                if (delay != 0) {
//                    player.seekTo(currentWindow, delay);
//                }
            } else {
                player.setVolume(0.5f);
            }
            player.seekTo(currentWindow, playbackPosition);
            player.setPlayWhenReady(false);
            MediaSource mediaSource;
            if (urls.size() > 0)
                mediaSource = buildMediaSource(urls.get(i));
            else
                mediaSource = buildMediaSource(uris.get(i));
            ClippingMediaSource clippingMediaSource = null;
            if (i == RECORDING_URL && delay != 0) {
                clippingMediaSource = new ClippingMediaSource(mediaSource, delay * 1000, 1000000000);
            }
            player.addListener(new Player.EventListener() {

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        releasePlayers();
                        finish();
                    }
                }
            });
            player.addListener(this);
            if (clippingMediaSource != null) {
                player.prepare(clippingMediaSource);
            } else
                player.prepare(mediaSource, false, false);
            findViewById(R.id.exo_pr_circle).setVisibility(View.INVISIBLE);

//            player.sets

        }

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
        if (players != null) {
            for (SimpleExoPlayer player : players)
                if (player != null) {
//                    playWhenReady = player.getPlayWhenReady();
                    playbackPosition = player.getCurrentPosition();
                    currentWindow = player.getCurrentWindowIndex();
                    player.release();
                    player = null;
                }
            players = null;
        }
    }

    private boolean playersAreReady() {
        for (SimpleExoPlayer player : players) {
            if (player.getPlaybackState() != Player.STATE_READY)
                return false;
        }
        return true;
    }

    private void addListeners() {
        findViewById(R.id.exo_play).setOnClickListener(view -> {
            if (playersAreReady()) {
                for (SimpleExoPlayer player : players) {
                    player.setPlayWhenReady(true);
                }
            }
        });
        findViewById(R.id.exo_pause).setOnClickListener(view -> {
            for (SimpleExoPlayer player : players) {
                player.setPlayWhenReady(false);
            }
        });

        ((TimeBar) findViewById(R.id.exo_progress)).addListener(this);
    }

    @Override
    public void onScrubStart(@NonNull TimeBar timeBar, long position) {
        timeBar.setEnabled(!locked);
        locked = true;
        if (positionView != null) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }
    }

    @Override
    public void onScrubMove(@NonNull TimeBar timeBar, long position) {
        if (positionView != null) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }
    }

    @Override
    public void onScrubStop(@NonNull TimeBar timeBar, long position, boolean canceled) {
        if (!canceled && players != null) {

            for (SimpleExoPlayer player : players) {
                player.setPlayWhenReady(false);
            }
            for (SimpleExoPlayer player : players) {
                seekToTimeBarPosition(player, position);
            }
            long startTime = System.currentTimeMillis();
            while (!(playersAreReady())) {
                long endTime = System.currentTimeMillis();
                if ((endTime - startTime) / 1000 > 1) {
                    releasePlayersAndStartFromThisTime(position);
                    timeBar.setEnabled(true);
                    locked = false;
                    startAgain();
                    return;
                }

            }
//            for (SimpleExoPlayer player : players) {
            if (playersAreReady()) {
                if (players.size() > 1)
                    players.get(1).setPlayWhenReady(true);
                players.get(0).setPlayWhenReady(true);
            }
            locked = false;
            timeBar.setEnabled(true);

        }

    }

    private void startAgain() {
        for (SimpleExoPlayer player : players)
            player.setPlayWhenReady(true);
    }

    private void releasePlayersAndStartFromThisTime(long position) {
        for (SimpleExoPlayer player : players)
            if (player != null) {
//                    playWhenReady = player.getPlayWhenReady();
                playbackPosition = position;
                currentWindow = player.getCurrentWindowIndex();
                player.release();
            }
        players = new ArrayList<>();
        createTwoPlayers();
        initializePlayer();

    }

    private void seekToTimeBarPosition(Player player, long positionMs) {
        int windowIndex;
        Timeline.Window window = new Timeline.Window();
        Timeline timeline = player.getCurrentTimeline();
        if (!timeline.isEmpty()) {
            int windowCount = timeline.getWindowCount();
            windowIndex = 0;
            while (true) {
                long windowDurationMs = timeline.getWindow(windowIndex, window).getDurationMs();
                if (positionMs < windowDurationMs) {
                    break;
                } else if (windowIndex == windowCount - 1) {
                    // Seeking past the end of the last window should seek to the end of the timeline.
                    positionMs = windowDurationMs;
                    break;
                }
                positionMs -= windowDurationMs;
                windowIndex++;
            }
        } else {
            windowIndex = player.getCurrentWindowIndex();
        }
        boolean dispatched = seekTo(player, windowIndex, positionMs);
        if (!dispatched) {
            // The seek wasn't dispatched then the progress bar scrubber will be in the wrong position.
            // Trigger a progress update to snap it back.
//            updateProgress();
        }
    }

    private boolean seekTo(Player player, int windowIndex, long positionMs) {
        DefaultControlDispatcher controlDispatcher = new DefaultControlDispatcher();
        return controlDispatcher.dispatchSeekTo(player, windowIndex, positionMs);
    }

}
