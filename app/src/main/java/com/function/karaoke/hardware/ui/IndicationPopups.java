package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.utils.static_classes.Converter;

public class IndicationPopups {

    public static PopupWindow openCheckIndication(Context context, View view, String words) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.check_popup);
        View popupView = layoutInflater.inflate(R.layout.check_popup, viewGroup);
        ((TextView) popupView.findViewById(R.id.text_to_display)).setText(words);
        return placeShareOptionsOnScreen(context, popupView);
    }

    public static PopupWindow openXIndication(Context context, View view, String words) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.x_popup);
        View popupView = layoutInflater.inflate(R.layout.x_popup, viewGroup);
        ((TextView) popupView.findViewById(R.id.text_to_display)).setText(words);
        return placeShareOptionsOnScreen(context, popupView);

    }

    private static PopupWindow placeShareOptionsOnScreen(Context context, View popupView) {
        PopupWindow popup = new PopupWindow(context);
        setSharePopupAttributes(context, popup, popupView);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        return popup;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setSharePopupAttributes(Context context, PopupWindow popup, View layout) {
        int width = Math.min(Converter.convertDpToPx(278), (int) (context.getResources().getDisplayMetrics().widthPixels * 0.77));
        int height = (int) (width * 0.73);
        popup.setContentView(layout);
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.unclicked_recording_background));
        popup.setWidth(width);
        popup.setHeight(height);
    }
}
