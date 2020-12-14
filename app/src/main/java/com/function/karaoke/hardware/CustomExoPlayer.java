package com.function.karaoke.hardware;

import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class CustomExoPlayer extends SimpleExoPlayer {

    protected CustomExoPlayer(Builder builder) {
        super(builder);
    }

    public Renderer[] getRenderer(){
        return renderers;
    }
}
