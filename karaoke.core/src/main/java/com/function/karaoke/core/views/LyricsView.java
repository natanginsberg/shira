package com.function.karaoke.core.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.function.karaoke.core.model.Song;

import org.jetbrains.annotations.Nullable;


/**
 * Created by ink on 2018-01-09.
 */

public class LyricsView extends TextView {

    private Song.Line mLine;

    private SpannableString mText;

    private ForegroundColorSpan mSpan;

    private int mCurrentChar = -1;

    private int syllableNumber = -1;
    private CountDownTimer cTimer = null;
    //    private int pos = 0;
    boolean lastWord = false;

    @SuppressLint("SetTextI18n")
    public LyricsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        this.setBackgroundColor(Color.GREEN);
//        this.setTextColor(Color.BLACK);
//        this.setText("nothing is working");
        mSpan = new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_light));
    }

    @SuppressLint("SetTextI18n")
    public void setLine(Song.Line line) {
        mLine = line;
        mCurrentChar = -1;
        if (null == line) {
            mText = null;
            setText("");
        } else {
            mText = new SpannableString(line.toString());
            setText(mText);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setPosition(double position) {
        if (null == mLine || null == mText)
            return;
        int pos = 0;
        for (Song.Syllable s : mLine.syllables)
//        for (int i = 0; i < mLine.syllables.size(); i++) {
//            Song.Syllable s = mLine.syllables.get(i);
            if (s.from < position)
//                if (i > syllableNumber) {
//
//                    syllableNumber++;
//                    timerToDraw((int) ((s.to - s.from) * 1000), s.text.length(),
//                            i == mLine.syllables.size() - 1);
//                }
                pos += s.text.length();

            else
                break;

        if (mCurrentChar == pos)
            return;

        mCurrentChar = pos;
//        mText.removeSpan(mSpan); // not needed actually, setSpan checks for duplicates
        mText.setSpan(mSpan, 0, pos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        setText(mText);
    }

}

//    private void timerToDraw(int length, int textLen, boolean lastSyllable) {
//        cTimer = new CountDownTimer(length, Math.round((double) length / textLen)) {
//            @SuppressLint("SetTextI18n")
//            public void onTick(long millisUntilFinished) {
//                pos += 1;
//                mCurrentChar = pos;
////        mText.removeSpan(mSpan); // not needed actually, setSpan checks for duplicates
//                mText.setSpan(mSpan, 0, pos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//
//                setText(mText);
//            }
//
//            public void onFinish() {
//                pos += 1;
//                mCurrentChar = pos;
////        mText.removeSpan(mSpan); // not needed actually, setSpan checks for duplicates
//                mText.setSpan(mSpan, 0, pos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                resetPosition(lastSyllable);
//                setText(mText);
//            }
//        };
//        cTimer.start();
//    }
//
//    private void resetPosition(boolean lastSyllable) {
//        if (lastSyllable) {
//            pos = 0;
//            syllableNumber = -1;
//        }
//    }
//
//    //cancel timer
//    void cancelTimer() {
//        if (cTimer != null)
//            cTimer.cancel();
//    }

