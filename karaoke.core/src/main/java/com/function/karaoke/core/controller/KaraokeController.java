package com.function.karaoke.core.controller;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.core.model.SongParser;
import com.function.karaoke.core.model.Tone;
import com.function.karaoke.core.utility.FileReader;
import com.function.karaoke.core.views.LyricsView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class KaraokeController implements Recorder.IToneListener {

    private final MediaPlayer mPlayer;
    private final String tempOutputFile;
    //    private MediaRecorder mRecorder;
    private final Handler mHandler;

    // realtime data
    private Song mSong;
    private Song.Line mCurrentLine;
    private long mLastUpdate;
    private long mLineStart;
    private final List<Tone> mTones = new ArrayList<>();
    private MyCustomObjectListener listener;

    // views
    private LyricsView mLyrics;
    private TextView wordsRead;
    private TextView wordsToRead;
//    private ToneRender mToneRender;

    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mUpdater, 20);
            double position = mPlayer.getCurrentPosition() / 1000.0;
            if (position >= mPlayer.getDuration() / 1000.0) {
                listener.onSongEnded(true);
                finishPlaying();


            }
            updateUI();
        }
    };

    public void finishPlaying() {
        mPlayer.stop();
        mPlayer.release();
//        mRecorder.stop();
//        mRecorder.release();
    }

    public KaraokeController(String tempOutputFile) {
        mHandler = new Handler();
        mPlayer = new MediaPlayer();
//        mRecorder = new MediaRecorder();
        this.tempOutputFile = tempOutputFile;
        this.listener = null;
    }

    public void init(View view, int lyrics, int wordsRead, int wordsToRead, int camera) {
        mLyrics = view.findViewById(lyrics);
        this.wordsRead = view.findViewById(wordsRead);
        this.wordsToRead = view.findViewById(wordsToRead);
//        mToneRender = view.findViewById(camera);
//        mToneRender.setTextField(mLyrics);
//        mToneRender.setTones(mTones); // risky a bit, but we all are in the UI thread
    }

    public boolean load(File file, View view) {
        try {
            List<String> lines = FileReader.readLines(file);
            mSong = SongParser.parse(lines);
            mSong.fullPath = file;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        File audioFile = mSong.getAudioFile();
        if (null == audioFile)
            return false;
//        Blurry.with(view.getContext())
//                .radius(10)
//                .sampling(8)
//                .color(Color.argb(66, 255, 255, 0))
//                .async();
        if (!loadAudio(audioFile))
            return false;


//        mPlayer.start();
//        mRecorder.start();
        return true;
    }


    private boolean loadAudio(@NotNull File file) {
        try {
            mPlayer.setDataSource(file.toString());
            mPlayer.prepare();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        double position = mPlayer.getCurrentPosition() / 1000.0;

        if (null != mCurrentLine && mCurrentLine.isIn(position)) {
            mLyrics.setPosition(position);
//            mToneRender.setPosition(position);
        } else {

            for (int i = 0; i < mSong.lines.size(); i++) {
                Song.Line line = mSong.lines.get(i);
//            }
//            for (Song.Line line : mSong.lines) {
                if (line.isIn(position)) {
                    if (i > 0) {
                        wordsRead.setText(mSong.lines.get(i - 1).toString());
                    }
                    if (i < mSong.lines.size() - 1) {
                        wordsToRead.setText(mSong.lines.get(i + 1).toString());
                    }
                    mCurrentLine = line;
                    mLyrics.setLine(mCurrentLine);
                    mLyrics.setPosition(position);
//                    mToneRender.setLine(mCurrentLine);
//                    mToneRender.setPosition(position);
                    mTones.clear();
                    mLastUpdate = System.currentTimeMillis();
                    mLineStart = mLastUpdate;

                    return;
                }
            }
            mCurrentLine = null;
//            mToneRender.setLine(null);
            mLineStart = -1;
            mTones.clear();
        }
    }

    public void onPause() {
        if (mPlayer.isPlaying()) {

            mPlayer.pause();
//            mRecorder.pause();
        }
        mHandler.removeCallbacks(mUpdater);
    }

    public void onStop() {
        mPlayer.release();
//        mRecorder.release();
    }

    public void onResume() {
        if (mPlayer.getCurrentPosition() / 1000 < 1) {
//            try {
//                mRecorder.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mRecorder.start();
        } else {
//            mRecorder.resume();
        }
        mPlayer.start();
        mHandler.post(mUpdater);

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

    public Song getmSong() {
        return mSong;
    }

    public MediaPlayer getmPlayer() {
        return mPlayer;
    }

    public void setCustomObjectListener(MyCustomObjectListener listener) {
        this.listener = listener;
    }

    public interface MyCustomObjectListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onSongEnded(boolean songIsOver);

    }
}
