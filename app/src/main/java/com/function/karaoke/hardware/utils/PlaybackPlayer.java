package com.function.karaoke.hardware.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.audiofx.PresetReverb;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import com.function.karaoke.hardware.Playback;
import com.function.karaoke.hardware.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RendererConfiguration;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.AuxEffectInfo;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class PlaybackPlayer {


    private boolean cameraOn;
    private final TrackSelector trackSelector = new TrackSelector() {
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
                    if (cameraOn) {

                        audioRenderers.add(i);

                        configs[i] = RendererConfiguration.DEFAULT;
                    }

                }
            }

            for (
                    int i = 0;
                    i < trackGroups.length; i++) {
                if (MimeTypes.isAudio(trackGroups.get(i).getFormat(0).sampleMimeType)) {
                    Integer index = audioRenderers.poll();
                    if (index != null) {
                        selections[index] = new FixedTrackSelection(trackGroups.get(i), 0);
                    }
                } else if (MimeTypes.isVideo(trackGroups.get(i).getFormat(0).sampleMimeType)) {
                    if (cameraOn) {
                        Integer index = audioRenderers.poll();
                        if (index != null)
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
    private final Context context;
    private int sessionId;
    private MediaSource mediaSource;
    private boolean earphonesUsed;
    private long playbackPosition;
    private int currentWindow;
    private final PlayerView playerView;
    private SimpleExoPlayer player;
    private List<Renderer> renderers;
    private AudioRendererWithoutClock earphoneRenderer;
    private RenderersFactory renderersFactory;
    private View view;

    public PlaybackPlayer(Context context, PlayerView playerView) {
        this.context = context;
        this.playerView = playerView;
    }

    public void assignEarphonesAndCamera(boolean cameraOn, boolean earphonesUsed) {
        this.cameraOn = cameraOn;
        this.earphonesUsed = earphonesUsed;
    }

    public void assignView(View view) {
        this.view = view;
    }

    private void addReverb() {
        PresetReverb pReverb = new PresetReverb(1, sessionId);
        pReverb.setPreset(PresetReverb.PRESET_SMALLROOM);
        pReverb.setEnabled(true);
        AuxEffectInfo effect = new AuxEffectInfo(pReverb.getId(), 1f);
        player.createMessage(renderers.get(1)).setType(Renderer.MSG_SET_AUX_EFFECT_INFO).setPayload(effect).send();
        player.prepare();
    }


    @SuppressLint("InlinedApi")
    public void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    public void buildMediaSourceFromUris(List<Uri> uri, int delay, long length) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Shira"));
        MediaItem mediaItem = new MediaItem.Builder().setUri(uri.get(0)).build();
        MediaSource mediaSource1 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        if (delay != 0) {
            mediaSource1 = new ClippingMediaSource(mediaSource1, delay * 1000,
                    (length + delay) * 1000, false, false, true);
        }
        if (uri.size() > 1) {
            MediaItem mediaItem1 = new MediaItem.Builder().setUri(uri.get(1)).build();
            MediaSource mediaSource2 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem1);

            if (length != 0)
                mediaSource2 = new ClippingMediaSource(mediaSource2, 0, length * 1000);


            MediaSource[] mediaSources = new MediaSource[]{mediaSource1, mediaSource2};
            mediaSource = new MergingMediaSource(true, mediaSources);
        } else
            mediaSource = mediaSource1;
    }

    public void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    public void createPlayer(Playback playback) {
        renderersFactory = new CustomRendererFactory(context);
        player =
                new SimpleExoPlayer.Builder(context, renderersFactory)
                        .setTrackSelector(trackSelector)
                        .build();
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setPlayer(player);
        player.setMediaSource(mediaSource);
//        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
//        player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        view.findViewById(R.id.exo_pr_circle).setVisibility(View.INVISIBLE);
        player.addListener(new Player.EventListener() {

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == ExoPlayer.STATE_ENDED) {
                    releasePlayer();
                    playback.showEndPopup();
                }
                if (state == ExoPlayer.STATE_READY) {
                    if (sessionId == -1)
                        sessionId = player.getAudioSessionId();
                    addReverb();
                }
            }
        });
        player.prepare();
        player.seekTo(currentWindow, playbackPosition);
        player.setSeekParameters(SeekParameters.EXACT); // accurate seeking
        player.setPlayWhenReady(true);
//        addSpinnerListeners();
        if (earphonesUsed) player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
                sessionId = audioSessionId;
            }
        });
        if (!cameraOn)
            view.findViewById(R.id.logo).setVisibility(View.VISIBLE);
    }

    public void restart() {
        playbackPosition = 0;
    }

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
                earphoneRenderer = new AudioRendererWithoutClock(context, mediaCodecSelector);
            out.add(earphoneRenderer);
            renderers = out;
        }
    }

    public void setPlayerToStart(boolean start) {
        player.setPlayWhenReady(start);
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

}
