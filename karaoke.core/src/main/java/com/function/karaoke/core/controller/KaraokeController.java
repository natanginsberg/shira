package com.function.karaoke.core.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.function.karaoke.core.model.Parser;
import com.function.karaoke.core.model.Song;
import com.function.karaoke.core.model.Tone;
import com.function.karaoke.core.views.LyricsView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class KaraokeController implements Recorder.IToneListener {

    private final MediaPlayer mPlayer;
    //        private MediaRecorder mRecorder;
    private final Handler mHandler;
    private final List<Tone> mTones = new ArrayList<>();
    // realtime data
    private Song mSong;
    private Song.Line mCurrentLine;
    private long mLastUpdate;
    private long mLineStart;
    private MyCustomObjectListener listener;

    private boolean prepared = false;
    private boolean playing = false;
    private final LinkedList<LyricsView> tempViews = new LinkedList<>();

    // views
//    private LyricsView mLyrics;
//    private LyricsView wordsToRead;
//    private ConstraintLayout wordSpace;
//    private int lyricsSize;

//    private ToneRender mToneRender;

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
//                if (timerStarted == 0)
//                    timerStarted = new Date().getTime();
                if ((mPlayer.getDuration() / 1000.0) - position < 0.7) {
                    listener.onSongEnded();
//                    finishPlaying();
                    mHandler.removeCallbacks(mUpdater);
                } else
                    updateUI();
            }
        }
    };
    private Context context;
    //    private LyricsView twoLinesAhead;
//    private LyricsView threeLinesAhead;
//    private int lyricsHeight = 0;
    private long timerStarted;
    private CustomUIListener uiListener;


    public KaraokeController(Context context) {
        mHandler = new Handler();
        mPlayer = new MediaPlayer();
//        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
////                listener.onSongEnded(true);
//                finishPlaying();
//                mHandler.removeCallbacks(mUpdater);
//            }
//        });
//        mRecorder = new MediaRecorder();
        this.listener = null;
        this.context = context;
    }

    public void addUIListener(CustomUIListener customUIListener){
        this.uiListener = customUIListener;
    }

    public void finishPlaying() {
        mPlayer.stop();
        mPlayer.release();
        playing = false;
//        mRecorder.stop();
//        mRecorder.release();
    }

    public void init(Context context) {
        this.context = context;
    }

//    public void addViews(View view, int lyrics, int wordsToRead, int twoLinesAhead, int wordSpace, int threeLinesAhead) {
//        mLyrics = view.findViewById(lyrics);
////        this.wordsRead = view.findViewById(wordsRead);
//        this.wordsToRead = view.findViewById(wordsToRead);
//        this.wordSpace = view.findViewById(wordSpace);
//        this.twoLinesAhead = view.findViewById(twoLinesAhead);
//        this.threeLinesAhead = view.findViewById(threeLinesAhead);
//        lyricsSize = this.twoLinesAhead.getHeight();
//    }


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
                        Log.i("bug88", "song is prepared" + (System.currentTimeMillis() - timerStarted));
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
                listener.setPosition(position);
//            mToneRender.setPosition(position);
            } else {

                for (int i = 0; i < mSong.lines.size(); i++) {
                    Song.Line line = mSong.lines.get(i);
                    if (line.isIn(position)) {
//                        if (mLyrics.getmLine() != null)
                        uiListener.updateUI(mSong.lines, i);
//                        else {
//                            listener.setLines(mSong.lines, i);
//                            mLyrics.setLine(line);
//                            if (i < mSong.lines.size() - 1) {
//                                wordsToRead.setLine(mSong.lines.get(i + 1));
//                            } else {
//                                wordsToRead.setText(" ");
//                            }
//                            if (i < mSong.lines.size() - 2) {
//                                twoLinesAhead.setLine(mSong.lines.get(i + 2));
//                            } else {
//                                twoLinesAhead.setText(" ");
//                            }
//                        }
//                        if (i < mSong.lines.size() - 3) {
//                            threeLinesAhead.setLine(mSong.lines.get(i + 3));
//                        } else {
//                            threeLinesAhead.setText(" ");
//                        }

                        mCurrentLine = line;
//                        mLyrics.setLine(mCurrentLine);
//                        mLyrics.setPosition(position);
                        listener.setPosition(position);
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
    }

//    private void updateUI() {
//
//        setOriginalYs();
//        float topDelta = -lyricsHeight - mLyrics.getOriginalPlace();
//        float secondDelta = mLyrics.getY() - wordsToRead.getOriginalPlace();
//        float thirdDelta = wordsToRead.getY() - twoLinesAhead.getOriginalPlace();
//        float bottomDelta = twoLinesAhead.getY() - threeLinesAhead.getOriginalPlace();
//
//        tempViews.push(mLyrics);
////        Log.i("bug88", threeLinesAhead.getY() + " " + twoLinesAhead.getY() + " " + wordsToRead.getY() + " " + mLyrics.getY());
//        Log.i("bug88", threeLinesAhead.getHeight() + " " + twoLinesAhead.getHeight() + " " + wordsToRead.getHeight() + " " + mLyrics.getHeight());
////        Log.i("bug88", bottomDelta + " " + thirdDelta + " " + secondDelta + " " + topDelta);
//
//        mLyrics.animate().translationY(topDelta).setDuration(500).start();
//        wordsToRead.animate().translationY(secondDelta).setDuration(500).start();
//        twoLinesAhead.animate().translationY(bottomDelta).setDuration(500).start();
//
//        ObjectAnimator animation = scrollViewUp(topDelta, mLyrics);
//        animation.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationCancel(Animator animation) {
//                super.onAnimationCancel(animation);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                tempViews.pop().setVisibility(View.GONE);
//
//            }
//        });
//        scrollViewUp(secondDelta, wordsToRead);
//        scrollViewUp(thirdDelta, twoLinesAhead);
//        scrollViewUp(bottomDelta, threeLinesAhead);
////        giveLyricsViewWeightOfZero(mLyrics);
////        mLyrics.setVisibility(View.GONE);
//        mLyrics = wordsToRead;
//        wordsToRead = twoLinesAhead;
////        giveLyricsViewWeightOfOne(twoLinesAhead);
//        twoLinesAhead = threeLinesAhead;
//        threeLinesAhead = createNewLyricsView();
//    }
//
//    private void setOriginalYs() {
//        if (mLyrics.getOriginalPlace() == 0) {
//            mLyrics.setOriginalPlace(mLyrics.getY());
//            if (lyricsHeight == 0)
//                lyricsHeight = mLyrics.getHeight();
//        }
//        if (wordsToRead.getOriginalPlace() == 0) {
//            wordsToRead.setOriginalPlace(wordsToRead.getY());
//        }
//        if (twoLinesAhead.getOriginalPlace() == 0) {
//            twoLinesAhead.setOriginalPlace(twoLinesAhead.getY());
//        }
//        if (threeLinesAhead.getOriginalPlace() == 0) {
//            threeLinesAhead.setOriginalPlace(threeLinesAhead.getY());
//        }
//    }
//
//    private ObjectAnimator scrollViewUp(float yDelta, LyricsView view2) {
////        xDelta = 0;
////        yDelta = -126;
//
//        ObjectAnimator animation = ObjectAnimator.ofFloat(view2, "translationY", yDelta);
////        animation.setRepeatMode(0);
//        animation.setDuration(500);
//        animation.setRepeatCount(0);
//        animation.setAutoCancel(true);
////        animation.setFillAfter(true);
////        view2.startAnimation(animation);
//        animation.start();
//        return animation.clone();
//    }

//    private void giveLyricsViewWeightOfOne(LyricsView lyricsView) {
//        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
//        param.height = 0;
//        lyricsView.setLayoutParams(param);
//    }
//
//    private void giveLyricsViewWeightOfZero(LyricsView lyricsView) {
//        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
//        lyricsView.setLayoutParams(param);
//    }

//    private LyricsView createNewLyricsView() {
//        ConstraintSet set = new ConstraintSet();
//        set.clone(wordSpace);
////        LinearLayout linearLayout = wordSpace;
//
//        LyricsView lyricsView = new LyricsView(context, null);
//        lyricsView.setId(View.generateViewId());
//
//        setAttributes(lyricsView);
//
//        wordSpace.addView(lyricsView);
//        set.connect(lyricsView.getId(), ConstraintSet.TOP, wordSpace.getId(), ConstraintSet.BOTTOM, 0);
////        set.connect(lyricsView.getId(), ConstraintSet.TOP, bottomGuide.getId(), ConstraintSet.TOP, 0);
//        set.connect(lyricsView.getId(), ConstraintSet.RIGHT, wordSpace.getId(), ConstraintSet.RIGHT, 0);
//        set.connect(lyricsView.getId(), ConstraintSet.LEFT, wordSpace.getId(), ConstraintSet.LEFT, 0);
//        set.constrainHeight(lyricsView.getId(), lyricsSize);
//        set.constrainedWidth(lyricsView.getId(), true);
//        set.applyTo(wordSpace);
////        linearLayout.addView(lyricsView);
//        return lyricsView;
//    }
//
//    private void setAttributes(LyricsView lyricsView) {
////        giveLyricsViewWeightOfOne(lyricsView);\
//        lyricsView.setHeight(0);
//        lyricsView.setTextColor(context.getResources().getColor(R.color.unhighlight_words));
//        lyricsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
//        ;
//        lyricsView.setGravity(Gravity.CENTER);
//        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/varela_round_regular.ttf");
//        lyricsView.setTypeface(tf);
//    }


    public void onPause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mHandler.removeCallbacks(mUpdater);
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

        void setPosition(double position);
    }

    public interface CustomUIListener{
        void updateUI(List<Song.Line> lines, int i);
    }

}
