package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.storage.RecordingService;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RendererConfiguration;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectorResult;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MediaClock;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Queue;


public class Playback extends AppCompatActivity implements PlaybackStateListener {

    public static final String RECORDING = "recording";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final String DELAY = "delay";
    private static final String EARPHONES_USED = "empty";
    private static final String LENGTH = "length";

    private final List<String> urls = new ArrayList<>();
    private final List<Uri> uris = new ArrayList<>();

    private PlayerView playerView;
    private List<SimpleExoPlayer> players = new ArrayList<>();

    private long playbackPosition = 0;
    private int currentWindow;
    private StringBuilder formatBuilder;
    private Formatter formatter;

    private boolean dynamicLink = false;

    private int delay;
    private long length;
    private boolean earphonesUsed = false;
    private MediaSource mediaSource;

    RenderersFactory renderersFactory;
    private SimpleExoPlayer player;

    //with the following class:

    private static final class AudioRendererWithoutClock extends MediaCodecAudioRenderer {
        public AudioRendererWithoutClock(Context context,
                                         MediaCodecSelector mediaCodecSelector) {
            super(context, mediaCodecSelector);
        }

        @Override
        public MediaClock getMediaClock() {
            return null;
        }
    }

    TrackSelector trackSelector = new TrackSelector() {
        @Override
        public TrackSelectorResult selectTracks(RendererCapabilities[] rendererCapabilities,
                                                TrackGroupArray trackGroups, MediaSource.MediaPeriodId periodId, Timeline timeline) {
            Queue<Integer> audioRenderers = new ArrayDeque<>();
            RendererConfiguration[] configs = new RendererConfiguration[rendererCapabilities.length];
            TrackSelection[] selections = new TrackSelection[rendererCapabilities.length];
            for (int i = 0; i < rendererCapabilities.length; i++) {
                if (rendererCapabilities[i].getTrackType() == C.TRACK_TYPE_AUDIO) {
                    audioRenderers.add(i);
                    configs[i] = RendererConfiguration.DEFAULT;
                } else if (rendererCapabilities[i].getTrackType() == C.TRACK_TYPE_VIDEO) {
                    audioRenderers.add(i);
                    configs[i] = RendererConfiguration.DEFAULT;
                }
            }
            for (int i = 0; i < trackGroups.length; i++) {
                if (MimeTypes.isAudio(trackGroups.get(i).getFormat(0).sampleMimeType)) {
                    Integer index = audioRenderers.poll();
                    if (index != null) {
                        selections[index] = new FixedTrackSelection(trackGroups.get(i), 0);
                    }
                } else if (MimeTypes.isVideo(trackGroups.get(i).getFormat(0).sampleMimeType)) {
                    Integer index = audioRenderers.poll();
                    if (index != null) {
                        selections[index] = new FixedTrackSelection(trackGroups.get(i), 0);
                    }
                }
            }
            return new TrackSelectorResult(configs, selections, new Object());
        }

        @Override
        public void onSelectionActivated(Object info) {
        }
    };

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
                length = getIntent().getLongExtra(LENGTH, 10000);
                buildMediaSourceFromUris(uris);
                createPlayer();
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
                length = recording.getLength();
                buildMediaSourceFromUrls(urls);
                createPlayer();
            } else {
                dynamicLink = true;
                getDynamicLink();
            }
        }
        setScrubbingFields();
//        addListeners();
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
                        length = Long.parseLong(deepLink.getQueryParameter("length"));
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
        RecordingService recordingService = new RecordingService();
        final Observer<Recording> recordingObserver = recording -> {
            if (recording != null) {
                urls.add(recording.getRecordingUrl());
                if (recording.getAudioFileUrl().equals(EARPHONES_USED)) {
                    urls.add(recording.getAudioFileUrl());
                    earphonesUsed = true;
                } else {
                    findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
                    findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
                }
                buildMediaSourceFromUrls(urls);
                createPlayer();
//                initializePlayer();
            }
        };
        recordingService.getSharedRecording(recordingId, recorderId).observe(this, recordingObserver);
    }


    private void createPlayer() {

        renderersFactory = new CustomRendererFactory(this);
        player =
                new SimpleExoPlayer.Builder(this, renderersFactory)
                        .setTrackSelector(trackSelector)
                        .build();
        System.out.println(player.getRendererCount() + " this is the count of the renderes ");
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setPlayer(player);
        player.setMediaSource(mediaSource);
        findViewById(R.id.exo_pr_circle).setVisibility(View.INVISIBLE);
        player.addListener(new Player.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    releasePlayer();
                    finish();
                }
            }
        });
        player.seekTo(currentWindow, playbackPosition);
//        player.setPlayWhenReady(true);
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
        if (player == null) {
            createPlayer();
//            initializePlayer();
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
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }


    private void buildMediaSourceFromUris(List<Uri> uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Shira"));
        MediaItem mediaItem = new MediaItem.Builder().setUri(uri.get(0)).build();
        MediaSource mediaSource1 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        if (delay != 0) {
            mediaSource1 = new ClippingMediaSource(mediaSource1, delay * 1000, 1000000000);

        }
        if (uris.size() > 1) {
            MediaItem mediaItem1 = new MediaItem.Builder().setUri(uri.get(1)).build();
            MediaSource mediaSource2 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem1);
            mediaSource2 = new ClippingMediaSource(mediaSource2, 0, length * 1000);
            MediaSource[] mediaSources = new MediaSource[]{mediaSource1, mediaSource2};
//            mediaSource = new MergingMediaSource(mediaSources);
            mediaSource = new MergingMediaSource(true, mediaSources);

        } else
            mediaSource = mediaSource1;
    }

    private void buildMediaSourceFromUrls(List<String> urls) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Shira"));
        Uri song = Uri.parse(urls.get(0));
        MediaItem mediaItem = new MediaItem.Builder().setUri(uris.get(0)).build();
        MediaSource mediaSource1 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);

        if (delay != 0) {
            mediaSource1 = new ClippingMediaSource(mediaSource1, delay * 1000, 1000000000, false, true, true);

        }
        if (uris.size() > 1) {
            song = Uri.parse(urls.get(1));
            MediaItem mediaItem1 = new MediaItem.Builder().setUri(song).build();
            MediaSource mediaSource2 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem1);
            mediaSource2 = new ClippingMediaSource(mediaSource2, 0, length * 1000);
            mediaSource = new MergingMediaSource(mediaSource1, mediaSource2);
        } else
            mediaSource = mediaSource1;
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
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


    private class CustomRendererFactory extends DefaultRenderersFactory {

        public CustomRendererFactory(Context context) {
            super(context);
        }

        // it is called internally. Do not delete!
        protected void buildAudioRenderers​(Context context,
                                            int extensionRendererMode,
                                            MediaCodecSelector mediaCodecSelector,
                                            boolean enableDecoderFallback, AudioSink audioSink,
                                            Handler eventHandler, AudioRendererEventListener eventListener,
                                            ArrayList<Renderer> out) {
            super.buildAudioRenderers(context, extensionRendererMode, mediaCodecSelector, enableDecoderFallback, audioSink, eventHandler, eventListener, out);
            if (earphonesUsed)
                out.add(new AudioRendererWithoutClock(context, mediaCodecSelector));
        }
    }

}
