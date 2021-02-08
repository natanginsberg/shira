package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.storage.RecordingService;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;


public class PlaybackTemp extends AppCompatActivity implements TimeBar.OnScrubListener, PlaybackStateListener {

    public static final String RECORDING = "recording";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final int RECORDING_URL = 0;
    private static final String DELAY = "delay";
    private static final String EARPHONES_USED = "empty";

    private final List<String> urls = new ArrayList<>();
    private final List<Uri> uris = new ArrayList<>();

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
    private boolean earphonesUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        playerView = findViewById(R.id.surface_view);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(PLAYBACK)) {
                uris.add(Uri.parse(getIntent().getStringExtra(PLAYBACK)));
                if (getIntent().getExtras().containsKey(AUDIO_FILE)) {
                    uris.add(Uri.parse(getIntent().getStringExtra(AUDIO_FILE)));
                    earphonesUsed = true;
                } else {
                    findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
                    findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
                }
                delay = getIntent().getIntExtra(DELAY, 0);
                createTwoPlayers();
                initializePlayer();
            } else if (getIntent().getExtras().containsKey(RECORDING)) {
                Recording recording = (Recording) getIntent().getSerializableExtra(RECORDING);
                urls.add(recording.getRecordingUrl());
                if (recording.getAudioFileUrl().equals(EARPHONES_USED)) {
                    urls.add(recording.getAudioFileUrl());
                    earphonesUsed = true;

//                    findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
                    findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
                }
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
                .addOnSuccessListener(this, pendingDynamicLinkData -> {

                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink;
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
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    private static final String TAG = "Error";

                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void addUrls(String recordingId, String recorderId) {
//        RecordingService recordingService = new RecordingService();
//        final Observer<Recording> recordingObserver = recording -> {
//            if (recording != null) {
//                urls.add(recording.getRecordingUrl());
//                if (recording.getAudioFileUrl().equals(EARPHONES_USED)) {
//                    urls.add(recording.getAudioFileUrl());
//                    earphonesUsed = true;
////                    findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
//                } else {
//                    findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
//                    findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
//                }
//                createTwoPlayers();
//                initializePlayer();
//            }
//        };
//        recordingService.getSharedRecording(recordingId, recorderId).observe(this, recordingObserver);
    }


    private void createTwoPlayers() {
        players.add(new SimpleExoPlayer.Builder(this).build());
        if (earphonesUsed) {
            TrackSelector trackSelector = new DefaultTrackSelector(this);
            DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                    .setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024)
                    .createDefaultLoadControl();
            players.add(new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).setLoadControl(loadControl).build());
        }
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
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        for (int i = 0; i < players.size(); i++) {
            SimpleExoPlayer player = players.get(i);
            if (i == RECORDING_URL) {
                playerView.setPlayer(player);
                player.setVolume(0.8f);
            } else {
                player.setVolume(0.5f);

            }
            player.setPlayWhenReady(false);
            MediaSource mediaSource;
            if (urls.size() > 0) {
                mediaSource = buildMediaSource(urls.get(i));
            } else {
                mediaSource = buildMediaSource(uris.get(i));
            }
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
            } else {
                player.prepare(mediaSource, false, false);
            }
            player.seekTo(currentWindow, playbackPosition);
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
        int o = 0;
        for (SimpleExoPlayer player : players) {
            System.out.println("player " + o + " is ready " + (player.getPlaybackState() == Player.STATE_READY));
            o++;
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
        addSpinnerListeners();
    }

    private void addSpinnerListeners() {
        Spinner recordingSpinner = findViewById(R.id.recording_spinner);
        recordingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String volume = adapterView.getItemAtPosition(i).toString();
                if (volume.equals(getResources().getString(R.string.default_value))) {
                    players.get(0).setVolume(0.8f);
                } else
                    players.get(0).setVolume(Float.parseFloat(volume));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Spinner playbackSpinner = findViewById(R.id.playback_spinner);
        playbackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (earphonesUsed) {
                    String volume = adapterView.getItemAtPosition(i).toString();
                    if (volume.equals(getResources().getString(R.string.default_value))) {
                        players.get(1).setVolume(0.7f);
                    } else
                        players.get(1).setVolume(Float.parseFloat(volume));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
        if (!earphonesUsed) {
            seekToTimeBarPosition(players.get(0), position);
            locked = false;
            timeBar.setEnabled(true);
        } else {
            if (!canceled && players != null) {

                for (SimpleExoPlayer player : players) {
                    player.setPlayWhenReady(false);
                    seekToTimeBarPosition(player, position);
                }

                findViewById(R.id.exo_pr_circle).setVisibility(View.VISIBLE);
                long startTime = System.currentTimeMillis();
                while (!(playersAreReady())) {
                    long endTime = System.currentTimeMillis();
                    if ((endTime - startTime) / (double) 1000 > 0.2) {
                        releasePlayersAndStartFromThisTime(position);
                        onScrubStop(timeBar, position, canceled);
                        return;
//                    startAgain(timeBar);
//                    final Handler handler = new Handler(Looper.getMainLooper());
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            //the players are not played simultaneously without this delay
//                            findViewById(R.id.exo_pr_circle).setVisibility(View.INVISIBLE);
//                            for (SimpleExoPlayer player : players) player.setPlayWhenReady(true);
//                            locked = false;
//                            timeBar.setEnabled(true);
//
//                        }
//                    }, 1000);
//                    return;
                    }
                }
                if (playersAreReady()) {
//                    final Handler handler = new Handler(Looper.getMainLooper());
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
                    //the players are not played simultaneously without this delay
                    findViewById(R.id.exo_pr_circle).setVisibility(View.INVISIBLE);
                    System.out.println("player 1 is ready " + (players.get(0).getPlaybackState() == Player.STATE_READY) +
                            "  " + players.get(0).getPlaybackState() + " the second player is ready " +
                            (players.get(1).getPlaybackState() == Player.STATE_READY) + " " + players.get(1).getPlaybackState());
                    for (SimpleExoPlayer player : players) player.setPlayWhenReady(true);
                    locked = false;
                    timeBar.setEnabled(true);

//                        }
//                    }, 1000);

                }

            }
        }
    }

    private void startAgain(TimeBar timeBar) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //the players are not played simultaneously without this delay
                findViewById(R.id.exo_pr_circle).setVisibility(View.INVISIBLE);
                for (SimpleExoPlayer player : players) player.setPlayWhenReady(true);

            }
        }, 900);
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
