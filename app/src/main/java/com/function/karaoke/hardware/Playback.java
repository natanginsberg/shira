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
    private static final int SAVE_VIDEO = 111;
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
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
    private boolean scrubbing;
    private TextView positionView;
    private StringBuilder formatBuilder;
    private Formatter formatter;

    private PlaybackStateListener playbackStateListener;

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
//                createPplayer();
//                initializePlayer();
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
        setScrubbingFields();
        addListeners();
        playbackStateListener = new PlaybackStateListener();
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
//                        Log.w(TAG, "getDynamicLink:onFailure", e);
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

//    private void createPplayer() {
//        players.add(new SimpleExoPlayer.Builder(this).build());
//    }

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

    private void initializePlayer() {
        for (int i = 0; i < 2; i++) {
            SimpleExoPlayer player = players.get(i);
            if (i == RECORDING_URL) {
                playerView.setPlayer(player);
            }
            player.setVolume(0.5f);
            player.setPlayWhenReady(false);
            player.seekTo(currentWindow, playbackPosition);
            MediaSource mediaSource;
            if (urls.size() > 0)
                mediaSource = buildMediaSource(urls.get(i));
            else
                mediaSource = buildMediaSource(uris.get(i));
            int finalI = i;
            player.addListener(new Player.EventListener() {
                @Override
                public void onSeekProcessed() {
                    int k = 0;
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
//                    if (playWhenReady) {
                    switch (playbackState) {
                        case ExoPlayer.STATE_IDLE:
                            break;
                        case ExoPlayer.STATE_BUFFERING:
                            break;
                        case ExoPlayer.STATE_READY:
                            break;
                        case ExoPlayer.STATE_ENDED:
                            for (SimpleExoPlayer p : players) {
                                p.setPlayWhenReady(false);
                                p.seekTo(currentWindow, 0);
                            }
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
                    if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                        players.get(1).seekTo(currentWindow, player.getContentPosition());
                    }
                }

            });
            player.addListener(this);
            player.prepare(mediaSource, true, false);
//            player.sets

        }
    }

//    private void initializePlayer() {
//        SimpleExoPlayer player = players.get(0);
//        playerView.setPlayer(player);
//
//        player.setVolume(0.5f);
//        player.setPlayWhenReady(false);
//        player.seekTo(currentWindow, playbackPosition);
//        MergingMediaSource mediaSource;
//        if (urls.size() > 0) {
//            mediaSource = new MergingMediaSource(buildMediaSource(urls.get(0)), buildMediaSource(urls.get(1)));
//        } else {
//            mediaSource = new MergingMediaSource(buildMediaSource(uris.get(0)), buildMediaSource(uris.get(1)));
//        }
//        player.prepare(mediaSource, true, false);
//    }

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

    private void addListeners() {
        findViewById(R.id.exo_play).setOnClickListener(view -> {
            for (SimpleExoPlayer player : players) {
                player.setPlayWhenReady(true);
            }
        });
        findViewById(R.id.exo_pause).setOnClickListener(view -> {
            for (SimpleExoPlayer player : players) {
                player.setPlayWhenReady(false);
            }
        });
        findViewById(R.id.exo_shuffle).setOnClickListener(view -> {
            int k = 0;
        });
        findViewById(R.id.exo_duration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int k = 0;
            }
        });
        findViewById(R.id.exo_progress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int k = 0;
            }
        });
        ((TimeBar) findViewById(R.id.exo_progress)).addListener(this);

//        playerView.setOnClickListener(this);

    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        scrubbing = true;
        if (positionView != null) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
        if (positionView != null) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        scrubbing = false;
        if (!canceled && players != null) {
            for (SimpleExoPlayer player : players) {
                player.setPlayWhenReady(false);
                seekToTimeBarPosition(player, position);
            }
            while (!(players.get(0).getPlaybackState() == Player.STATE_READY && players.get(1).getPlaybackState() == Player.STATE_READY)) {
            }
            for (SimpleExoPlayer player : players) {
                player.setPlayWhenReady(true);
//                seekToTimeBarPosition(player, position);
            }

        }

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


    private static class PlaybackStateListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
            if (playWhenReady) {
                String stateString = "";
                switch (playbackState) {
                    case ExoPlayer.STATE_IDLE:
                        stateString = "ExoPlayer.STATE_IDLE      -";
                        break;
                    case ExoPlayer.STATE_BUFFERING:
                        stateString = "ExoPlayer.STATE_BUFFERING -";
                        break;
                    case ExoPlayer.STATE_READY:
                        stateString = "ExoPlayer.STATE_READY     -";
                        break;
                    case ExoPlayer.STATE_ENDED:
                        stateString = "ExoPlayer.STATE_ENDED     -";
                        break;
                    default:
                        stateString = "UNKNOWN_STATE             -";
                        break;
                }
            }
        }
    }

//    private void updateProgress() {
//        if (!isVisible() || !isAttachedToWindow) {
//            return;
//        }
//
//        @Nullable Player player = this.player;
//        long position = 0;
//        long bufferedPosition = 0;
//        if (player != null) {
//            position = currentWindowOffset + player.getContentPosition();
//            bufferedPosition = currentWindowOffset + player.getContentBufferedPosition();
//        }
//        if (positionView != null && !scrubbing) {
//            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
//        }
//        if (timeBar != null) {
//            timeBar.setPosition(position);
//            timeBar.setBufferedPosition(bufferedPosition);
//        }
//        if (progressUpdateListener != null) {
//            progressUpdateListener.onProgressUpdate(position, bufferedPosition);
//        }
//
//        // Cancel any pending updates and schedule a new one if necessary.
//        removeCallbacks(updateProgressAction);
//        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
//        if (player != null && player.isPlaying()) {
//            long mediaTimeDelayMs =
//                    timeBar != null ? timeBar.getPreferredUpdateDelay() : MAX_UPDATE_INTERVAL_MS;
//
//            // Limit delay to the start of the next full second to ensure position display is smooth.
//            long mediaTimeUntilNextFullSecondMs = 1000 - position % 1000;
//            mediaTimeDelayMs = Math.min(mediaTimeDelayMs, mediaTimeUntilNextFullSecondMs);
//
//            // Calculate the delay until the next update in real time, taking playback speed into account.
//            float playbackSpeed = player.getPlaybackParameters().speed;
//            long delayMs =
//                    playbackSpeed > 0 ? (long) (mediaTimeDelayMs / playbackSpeed) : MAX_UPDATE_INTERVAL_MS;
//
//            // Constrain the delay to avoid too frequent / infrequent updates.
//            delayMs = Util.constrainValue(delayMs, timeBarMinUpdateIntervalMs, MAX_UPDATE_INTERVAL_MS);
//            postDelayed(updateProgressAction, delayMs);
//        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
//            postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS);
//        }
//    }


}
