package com.function.karaoke.core.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import com.function.karaoke.core.model.Song;
import com.function.phone.core.R;

import org.jetbrains.annotations.Nullable;


/**
 * Created by ink on 2018-01-09.
 */

public class LyricsView extends androidx.appcompat.widget.AppCompatTextView {

    //    private int pos = 0;
    boolean lastWord = false;
    private Song.Line mLine;
    private SpannableString mText;
    private final ForegroundColorSpan mSpan;
    private int mCurrentChar = -1;
    private final int syllableNumber = -1;
    private final CountDownTimer cTimer = null;
    private float originalPlace = 0;

    @SuppressLint("SetTextI18n")
    public LyricsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mSpan = new ForegroundColorSpan(getResources().getColor(R.color.focused_text_color));
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
            if (s.to < position)
                pos += s.text.length();
            else {
                if (s.from < position && s.letters.get(0).letter == '*') {
                    pos += s.text.length();
                } else
                    for (Song.Letter letter : s.letters)
                        if (letter.from < position)
                            pos += 1;
                        else
                            break;
            }
        if (mCurrentChar == pos)
            return;

        mCurrentChar = pos;
        mText.setSpan(mSpan, 0, pos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        setText(mText);
    }

    public Song.Line getmLine() {
        return mLine;
    }

    public float getOriginalPlace() {
        return originalPlace;
    }

    public void setOriginalPlace(float place) {
        originalPlace = place;
    }
}