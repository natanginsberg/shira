package com.function.karaoke.interaction.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.core.model.Song;
import com.function.karaoke.core.views.LyricsView;
import com.function.karaoke.interaction.R;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.List;

public class KaraokeWordUI implements KaraokeController.CustomUIListener {

//    private final Context context;
    private LyricsView mLyrics;
    private LyricsView wordsToRead;
    private LyricsView twoLinesAhead;
    private LyricsView threeLinesAhead;
    private ConstraintLayout wordSpace;
    private final WeakReference<Activity> activityWeakReference;
    private int lyricsSize;
    private int lyricsHeight;
    private ArrayDeque<LyricsView> tempViews = new ArrayDeque<>();

    public KaraokeWordUI(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    public void addViews(View view) {
        mLyrics = view.findViewById(R.id.lyrics);
//        this.wordsRead = view.findViewById(wordsRead);
        this.wordsToRead = view.findViewById(R.id.words_to_read);
        this.wordSpace = (ConstraintLayout)view;
        this.twoLinesAhead = view.findViewById(R.id.words_to_read_2);
        this.threeLinesAhead = view.findViewById(R.id.words_to_read_3);
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


    private LyricsView createNewLyricsView() {
        ConstraintSet set = new ConstraintSet();
        set.clone(wordSpace);
//        LinearLayout linearLayout = wordSpace;

        LyricsView lyricsView = new LyricsView(activityWeakReference.get(), null);
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
        lyricsView.setTextColor(activityWeakReference.get().getResources().getColor(com.function.phone.core.R.color.unhighlight_words));
        lyricsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        ;
        lyricsView.setGravity(Gravity.CENTER);
        Typeface tf = Typeface.createFromAsset(activityWeakReference.get().getAssets(), "fonts/varela_round_regular.ttf");
        lyricsView.setTypeface(tf);
    }


    public int setPosition(double position) {
        return mLyrics.setPosition(position);
    }
}
