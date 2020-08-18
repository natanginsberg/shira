package com.function.karaoke.hardware;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayer extends Activity {

    private VideoView mVideoView;
    private String videoPath;
    private String VIDEO_PATH = "video path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mVideoView = findViewById(R.id.video_view);
        videoPath = getIntent().getStringExtra(VIDEO_PATH);
    }


    public void fastForward(View view) {
    }

    public void rewind(View view) {
    }


    public void play(View view) {
        findViewById(R.id.pauser).setVisibility(View.VISIBLE);
        findViewById(R.id.player).setVisibility(View.INVISIBLE);

        mVideoView.setVideoPath(videoPath);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.start();
    }

    public void pause(View view) {
        findViewById(R.id.pauser).setVisibility(View.INVISIBLE);
        findViewById(R.id.player).setVisibility(View.VISIBLE);
        mVideoView.pause();
        mVideoView.stopPlayback();
    }
}