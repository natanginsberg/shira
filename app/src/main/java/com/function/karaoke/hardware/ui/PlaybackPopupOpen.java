package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.function.karaoke.hardware.R;

public class PlaybackPopupOpen {

    private final View view;
    private final Context context;
    private final PlaybackPopupListener mlistner;
    private View popupView;
    private PopupWindow popup;
    private CountDownTimer cTimer;

    public PlaybackPopupOpen(View view, Context context, PlaybackPopupListener playbackPopupListener) {
        this.view = view;
        this.context = context;
        this.mlistner = playbackPopupListener;
    }

    private void placeSignUpOptionsOnScreen(Context context) {
        popup = new PopupWindow(context);
        setSignUpPopupAttributes(popup, popupView);
        view.post(() -> {
            popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setSignUpPopupAttributes(PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8);
        int height = (int) (width * 1.3);
        popup.setContentView(layout);
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.unclicked_recording_background));
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public View openPopup(int id, int layout) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(id);
        popupView = layoutInflater.inflate(layout, viewGroup);

        placeSignUpOptionsOnScreen(context);
        popup.setFocusable(true);
        return popupView;
    }

    public View getPopupView() {
        return popupView;
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public void showInvalidPassword() {
        popupView.findViewById(R.id.incorrect_password).setVisibility(View.VISIBLE);
        showMessageForOneSecond();
    }

    public void hideInvalidPassword() {
        popupView.findViewById(R.id.incorrect_password).setVisibility(View.INVISIBLE);
    }

    private void showMessageForOneSecond() {
        if (cTimer == null) {
            cTimer = new CountDownTimer(1500, 500) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    cTimer.cancel();
                    hideInvalidPassword();
                    cTimer = null;
                }
            };
            cTimer.start();
        }
    }

    public void dismissPopup() {
        popup.dismiss();
    }

    public interface PlaybackPopupListener {
        void validatePassword(String password);
    }
}
