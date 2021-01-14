package com.function.karaoke.hardware.utils.static_classes;

import android.content.res.Resources;

/**
 * A general utilities class
 */
public class Converter {
    public static int convertDpToPx(double dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int convertPixelsToDp(float px) {
        return (int) px / (int) (Resources.getSystem().getDisplayMetrics().density);
    }

    public static int convertDpToSp(float dp) {
        return (int) convertDpToPx(dp) / (int) Resources.getSystem().getDisplayMetrics().scaledDensity;
    }
}