package com.function.karaoke.interaction.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.core.views.LyricsView;

import java.util.ArrayDeque;
import java.util.List;

public class KaraokeWordUI {

    private final Context context;
    private LyricsView mLyrics;
    private LyricsView wordsToRead;
    private LyricsView twoLinesAhead;
    private LyricsView threeLinesAhead;
    private ConstraintLayout wordSpace;
    private int lyricsSize;
    private int lyricsHeight;
    private ArrayDeque<LyricsView> tempViews = new ArrayDeque<>();

    public KaraokeWordUI(Context context) {
        this.context = context;
    }

    public void addViews(View view, int lyrics, int wordsToRead, int twoLinesAhead, int wordSpace, int threeLinesAhead) {
        mLyrics = view.findViewById(lyrics);
//        this.wordsRead = view.findViewById(wordsRead);
        this.wordsToRead = view.findViewById(wordsToRead);
        this.wordSpace = view.findViewById(wordSpace);
        this.twoLinesAhead = view.findViewById(twoLinesAhead);
        this.threeLinesAhead = view.findViewById(threeLinesAhead);
        lyricsSize = this.twoLinesAhead.getHeight();
    }

    public void updateUI(List<Song.Line> lines, int i) {
        if (mLyrics.getmLine() != null)
            changeLines();
        else {
            mLyrics.setLine(lines.get(i));
            if (i < lines.size() - 1) {
                wordsToRead.setLine(lines.get(i + 1));
            } else {
                wordsToRead.setText(" ");
            }
            if (i < lines.size() - 2) {
                twoLinesAhead.setLine(lines.get(i + 2));
            } else {
                twoLinesAhead.setText(" ");
            }
        }
        if (i < lines.size() - 3) {
            threeLinesAhead.setLine(lines.get(i + 3));
        } else {
            threeLinesAhead.setText(" ");
        }

    }

    public void changeLines() {
        setOriginalYs();
        float topDelta = -lyricsHeight - mLyrics.getOriginalPlace();
        float secondDelta = mLyrics.getY() - wordsToRead.getOriginalPlace();
        float thirdDelta = wordsToRead.getY() - twoLinesAhead.getOriginalPlace();
        float bottomDelta = twoLinesAhead.getY() - threeLinesAhead.getOriginalPlace();

        tempViews.push(mLyrics);
//        Log.i("bug88", threeLinesAhead.getY() + " " + twoLinesAhead.getY() + " " + wordsToRead.getY() + " " + mLyrics.getY());
        Log.i("bug88", threeLinesAhead.getHeight() + " " + twoLinesAhead.getHeight() + " " + wordsToRead.getHeight() + " " + mLyrics.getHeight());
//        Log.i("bug88", bottomDelta + " " + thirdDelta + " " + secondDelta + " " + topDelta);

        mLyrics.animate().translationY(topDelta).setDuration(500).start();
        wordsToRead.animate().translationY(secondDelta).setDuration(500).start();
        twoLinesAhead.animate().translationY(bottomDelta).setDuration(500).start();

        ObjectAnimator animation = scrollViewUp(topDelta, mLyrics);
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                tempViews.pop().setVisibility(View.GONE);

            }
        });
        scrollViewUp(secondDelta, wordsToRead);
        scrollViewUp(thirdDelta, twoLinesAhead);
        scrollViewUp(bottomDelta, threeLinesAhead);
//        giveLyricsViewWeightOfZero(mLyrics);
//        mLyrics.setVisibility(View.GONE);
        mLyrics = wordsToRead;
        wordsToRead = twoLinesAhead;
//        giveLyricsViewWeightOfOne(twoLinesAhead);
        twoLinesAhead = threeLinesAhead;
        threeLinesAhead = createNewLyricsView();

    }

    private void setOriginalYs() {
        if (mLyrics.getOriginalPlace() == 0) {
            mLyrics.setOriginalPlace(mLyrics.getY());
            if (lyricsHeight == 0)
                lyricsHeight = mLyrics.getHeight();
        }
        if (wordsToRead.getOriginalPlace() == 0) {
            wordsToRead.setOriginalPlace(wordsToRead.getY());
        }
        if (twoLinesAhead.getOriginalPlace() == 0) {
            twoLinesAhead.setOriginalPlace(twoLinesAhead.getY());
        }
        if (threeLinesAhead.getOriginalPlace() == 0) {
            threeLinesAhead.setOriginalPlace(threeLinesAhead.getY());
        }
    }

    private ObjectAnimator scrollViewUp(float yDelta, LyricsView view2) {
//        xDelta = 0;
//        yDelta = -126;

        ObjectAnimator animation = ObjectAnimator.ofFloat(view2, "translationY", yDelta);
//        animation.setRepeatMode(0);
        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setAutoCancel(true);
//        animation.setFillAfter(true);
//        view2.startAnimation(animation);
        animation.start();
        return animation.clone();
    }

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

    private LyricsView createNewLyricsView() {
        ConstraintSet set = new ConstraintSet();
        set.clone(wordSpace);
//        LinearLayout linearLayout = wordSpace;

        LyricsView lyricsView = new LyricsView(context, null);
        lyricsView.setId(View.generateViewId());

        setAttributes(lyricsView);

        wordSpace.addView(lyricsView);
        set.connect(lyricsView.getId(), ConstraintSet.TOP, wordSpace.getId(), ConstraintSet.BOTTOM, 0);
//        set.connect(lyricsView.getId(), ConstraintSet.TOP, bottomGuide.getId(), ConstraintSet.TOP, 0);
        set.connect(lyricsView.getId(), ConstraintSet.RIGHT, wordSpace.getId(), ConstraintSet.RIGHT, 0);
        set.connect(lyricsView.getId(), ConstraintSet.LEFT, wordSpace.getId(), ConstraintSet.LEFT, 0);
        set.constrainHeight(lyricsView.getId(), lyricsSize);
        set.constrainedWidth(lyricsView.getId(), true);
        set.applyTo(wordSpace);
//        linearLayout.addView(lyricsView);
        return lyricsView;
    }

    private void setAttributes(LyricsView lyricsView) {
//        giveLyricsViewWeightOfOne(lyricsView);\
        lyricsView.setHeight(0);
        lyricsView.setTextColor(context.getResources().getColor(com.function.phone.core.R.color.unhighlight_words));
        lyricsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        ;
        lyricsView.setGravity(Gravity.CENTER);
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/varela_round_regular.ttf");
        lyricsView.setTypeface(tf);
    }


    public void setPosition(double position) {
        mLyrics.setPosition(position);
    }
}
