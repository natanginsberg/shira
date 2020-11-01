package com.function.karaoke.hardware;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class CustomMediaPlayer {

    private String url;
    private SimpleExoPlayer simpleExoPlayer;
    private long playbackPosition;
    private int currentWindow;
    Context context;
    private SimpleExoPlayer player;

    public CustomMediaPlayer(Context context, String url) {
        this.context = context;
        this.url = url;
        initializePlayer();

    }

    public boolean isPlayerReady(){
        return player.getPlaybackState() == SimpleExoPlayer.STATE_READY;
    }

    public void pauseSong(){
        player.setPlayWhenReady(false);
    }

    public void startSong(){
        player.setPlayWhenReady(true);
    }


    public void onPause() {
        releasePlayer();
    }

    public void onStop() {
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }


    private void releasePlayer() {
        if (simpleExoPlayer != null) {
            playbackPosition = simpleExoPlayer.getCurrentPosition();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }


    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(context).build();
        currentWindow = player.getCurrentWindowIndex();
        player.setPlayWhenReady(false);
        player.seekTo(currentWindow, playbackPosition);
        MediaSource mediaSource;
        mediaSource = buildMediaSource();
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
                switch (playbackState) {
                    case ExoPlayer.STATE_ENDED:
                        player.setPlayWhenReady(false);
                        break;
                }

            }

        });
        player.prepare(mediaSource, true, false);

    }

    private MediaSource buildMediaSource() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Shira"));
        Uri song = Uri.parse(url);
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(song);
    }

    public long getCurrentPosition(){
        return player.getCurrentPosition();
    }
}
