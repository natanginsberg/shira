package com.function.karaoke.core.views;

import android.annotation.SuppressLint;
import android.content.Context;
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

    private int pos = 0;
    private Song.Line mLine;
    private SpannableString mText;
    private final ForegroundColorSpan mSpan;
    private int mCurrentChar = -1;
    private float originalPlace = 0;

    @SuppressLint("SetTextI18n")
    public LyricsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mSpan = new ForegroundColorSpan(getResources().getColor(R.color.word_highlight));
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
    public int setPosition(double position) {
        if (null == mLine || null == mText)
            return 0;
        int pos = 0;
        for (Song.Syllable s : mLine.syllables)
            if (s.from < position)
                pos += s.text.length();
        if (mCurrentChar == pos)
            return pos;

        mCurrentChar = pos;
        mText.setSpan(mSpan, 0, pos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        setText(mText);
        return pos;
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