package com.function.karaoke.interaction.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.function.karaoke.interaction.R;
import com.function.karaoke.interaction.utils.static_classes.Converter;

public class IndicationPopups {

    public static PopupWindow openCheckIndication(Context context, View view, String words) {
        if (view == null)
            return null;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.check_popup);
        View popupView = layoutInflater.inflate(R.layout.check_popup, viewGroup);
        ((TextView) popupView.findViewById(R.id.text_to_display)).setText(words);
        return placeIndicationOnScreen(context, popupView);
    }

    public static PopupWindow openXIndication(Context context, View view, String words) {
        if (view == null)
            return null;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.x_popup);
        View popupView = layoutInflater.inflate(R.layout.x_popup, viewGroup);
        ((TextView) popupView.findViewById(R.id.text_to_display)).setText(words);
        return placeIndicationOnScreen(context, popupView);

    }

    private static PopupWindow placeIndicationOnScreen(Context context, View popupView) {
        if (popupView != null && context != null) {
            PopupWindow popup = new PopupWindow(context);
            setSharePopupAttributes(context, popup, popupView);
            popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            return popup;
        }
        return null;
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
