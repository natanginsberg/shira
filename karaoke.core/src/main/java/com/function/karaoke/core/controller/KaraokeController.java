package com.function.karaoke.core.controller;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;

import com.function.karaoke.core.model.Parser;
import com.function.karaoke.core.model.Song;
import com.function.karaoke.core.model.Tone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class KaraokeController implements Recorder.IToneListener {

    private final MediaPlayer mPlayer;
    //        private MediaRecorder mRecorder;
    private final Handler mHandler;
    private final List<Tone> mTones = new ArrayList<>();
    // realtime data
    private Song mSong;
    private Song.Line mCurrentLine;
    private int lineNumber = 0;
    private long mLastUpdate;
    private long mLineStart;
    private MyCustomObjectListener listener;

    private boolean prepared = false;
    private boolean playing = false;

    private int wordNumber = 0;

    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            if (timerStarted == 0) {
                if (mPlayer.getCurrentPosition() <= 0) {
                    mHandler.postDelayed(mUpdater, 5);
                } else {
                    timerStarted = new Date().getTime();
                }

            }

            mHandler.postDelayed(mUpdater, 100);
            double position = mPlayer.getCurrentPosition() / 1000.0;
            if (position > 0 && position <= mPlayer.getDuration() / 1000.0) {
                if ((mPlayer.getDuration() / 1000.0) - position < 0.7) {
                    listener.onSongEnded();
                    mHandler.removeCallbacks(mUpdater);
                } else
                    updateUI();
            }
        }
    };
    private long timerStarted;
    private CustomUIListener uiListener;


    public KaraokeController() {
        mHandler = new Handler();
        mPlayer = new MediaPlayer();
        this.listener = null;
    }

    public void addUIListener(CustomUIListener customUIListener) {
        this.uiListener = customUIListener;
    }

    public void finishPlaying() {
        mPlayer.stop();
        mPlayer.release();
        playing = false;
//        mRecorder.stop();
//        mRecorder.release();
    }

    public boolean loadWords(List<String> lines) {
        try {
            mSong = Parser.parse(lines);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void loadAudio(String url) {
        if (url.length() > 0) {
            try {
                mPlayer.setDataSource(url);

//            mPlayer.setVolume(0, 0);
                mPlayer.setOnPreparedListener(mediaPlayer -> {
                    prepared = true;
                    mPlayer.seekTo(0);
                    listener.songPrepared();
                    if (Build.VERSION.SDK_INT >= 24) {
//                        Log.i("bug88", "song is prepared" + (System.currentTimeMillis() - timerStarted));
                        mPlayer.start();
                        mPlayer.pause();
                    }
                });
                timerStarted = System.currentTimeMillis();
                mPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            double position = mPlayer.getCurrentPosition() / 1000.0;

            if (null != mCurrentLine && mCurrentLine.isIn(position)) {
                listener.setPosition(position, false);
//            mToneRender.setPosition(position);
            } else {

                if (mSong != null)
                    for (int i = 0; i < mSong.lines.size(); i++) {
                        if (mSong.lines != null) {
                            Song.Line line = mSong.lines.get(i);
                            if (line.isIn(position)) {
                                uiListener.updateUI(mSong.lines, i);
                                mCurrentLine = line;
                                lineNumber += 1;
                                listener.setPosition(position, true);
                                mTones.clear();
                                mLastUpdate = System.currentTimeMillis();
                                mLineStart = mLastUpdate;
                                return;
                            }
                        }
                    }
                mCurrentLine = null;
//            mToneRender.setLine(null);
                mLineStart = -1;
                mTones.clear();
            }
        }
    }

    public boolean onPause() {
        try {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.pause();
            }
            mHandler.removeCallbacks(mUpdater);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public void onStop() {
        mPlayer.release();
        playing = false;
        mHandler.removeCallbacks(mUpdater);
//        mRecorder.release();
    }

    public void onResume() {
        if (prepared) {
            mPlayer.start();
            playing = true;
            mHandler.post(mUpdater);
        }
    }

    @Override
    public void toneChanged(int tone, long duration) {
        if (!mPlayer.isPlaying() || -1 == mLineStart)
            return;

        long timeMillis = System.currentTimeMillis();

        Tone last = mTones.isEmpty() ? null : mTones.get(mTones.size() - 1);
        if (null != last && last.tone == tone)
            last.duration += timeMillis - mLastUpdate;
        else if (-1 != tone)
            mTones.add(new Tone(tone, duration, timeMillis - mLineStart));

        mLastUpdate = timeMillis;
    }


    public MediaPlayer getmPlayer() {
        return mPlayer;
    }

    public void setCustomObjectListener(MyCustomObjectListener listener) {
        this.listener = listener;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public boolean isPlaying() {
        return playing;
    }

    public long getTimerStarted() {
        return timerStarted;
    }

    public interface MyCustomObjectListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onSongEnded();

        void songPrepared();

        void setPosition(double position, boolean lineChanged);
    }

    public interface CustomUIListener {
        void updateUI(List<Song.Line> lines, int i);
    }

}
