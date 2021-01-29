package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;

public class SingActivityUI {

    private final View view;
    private final DatabaseSong song;
    private final int sdkInt;
    private View popupView;
    private PopupWindow popup;
    private boolean popupOpened = false;
    private TextView loadingAmount;
    private PopupWindow recordingsPopup;

    public SingActivityUI(View singActivity, DatabaseSong song, int sdkInt) {
        this.view = singActivity;
        this.song = song;
        this.sdkInt = sdkInt;
    }

    public void addTitleToScreen() {
        ((TextView) view.findViewById(R.id.song_name)).setText(song.getTitle());
    }

    public void addArtistToScreen() {
        ((TextView) view.findViewById(R.id.song_name_2)).setText(song.getTitle());
        ((TextView) view.findViewById(R.id.artist_name)).setText(song.getArtist());
    }

    public void setBackgroundColor() {
        view.findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
    }

    public void setSurfaceForRecording(boolean cameraOn) {
        view.findViewById(R.id.play_button).setVisibility(View.GONE);
//        view.findViewById(R.id.camera_toggle_button).setVisibility(View.INVISIBLE);
        if (!cameraOn)
            view.findViewById(R.id.logo).setVisibility(View.VISIBLE);


    }

    public void turnOffCameraOptions() {
        changeChech(false);
        turnOffClickListeners();
//        view.findViewById(R.id.camera_toggle_button).setVisibility(View.INVISIBLE);
    }

    private void turnOffClickListeners() {
        popupView.findViewById(R.id.check).setOnClickListener(null);
        popupView.findViewById(R.id.no_check).setOnClickListener(null);
        popupView.findViewById(R.id.check_holder).setOnClickListener(null);
    }

    public void clearLyricsScreen() {
        view.findViewById(R.id.song_name_2).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.artist_name).setVisibility(View.INVISIBLE);
    }

    public void setScreenForPlayingAfterTimerExpires() {
        view.findViewById(R.id.open_end_options).setVisibility(View.VISIBLE);
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
        if (sdkInt >= 24)
            view.findViewById(R.id.pause).setVisibility(View.VISIBLE);
        view.findViewById(R.id.play).setVisibility(View.INVISIBLE);
    }

    public void resetScreenForTimer() {
        resetLyricsScreen();
        showPlayButton();
//        view.findViewById(R.id.camera_toggle_button).setVisibility(View.VISIBLE);
    }

    private void resetLyricsScreen() {
        view.findViewById(R.id.song_name_2).setVisibility(View.VISIBLE);
        view.findViewById(R.id.artist_name).setVisibility(View.VISIBLE);
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
    }

    public void openEndPopup(Context context, boolean songEnded) {
        popupOpened = true;
        RelativeLayout viewGroup = view.findViewById(R.id.end_options);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.end_song_options, viewGroup);
        loadingAmount = popupView.findViewById(R.id.save_recording);
        if (songEnded) {
            popupView.findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
        }
        placePopupOnScreen(context);
        popup.setFocusable(true);
        applyDim(view.findViewById(R.id.sing_song).getOverlay());
    }

    private void placePopupOnScreen(Context context) {
        popup = new PopupWindow(context);

        setEndOptionsPopupAttributes(context, popup, popupView);
        view.post(new Runnable() {
            @Override
            public void run() {
                popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            }
        });
    }

    private void applyDim(ViewOverlay overlay) {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, view.findViewById(R.id.sing_song).getWidth(), view.findViewById(R.id.sing_song).getHeight());
        dim.setAlpha((int) (255 * (float) 0.8));
//        ViewOverlay overlay = view.findViewById(R.id.sing_song).getOverlay();
        overlay.add(dim);
    }

    public void undimBackground() {
        ViewOverlay overlay = view.findViewById(R.id.sing_song).getOverlay();
        overlay.clear();
    }

    private void setEndOptionsPopupAttributes(Context context, PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.781);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.576);
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public PopupWindow getPopup() {
        return popup;
    }


    public void makeLoadingBarVisible() {
        applyDim(popupView.getOverlay());
//        popupView.findViewById(R.id.play_again_progress).setVisibility(View.VISIBLE);

    }


    public void removeResumeOptionFromPopup() {
        popupView.findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
    }

    public void songPaused() {
        view.findViewById(R.id.pause).setVisibility(View.INVISIBLE);
        if (sdkInt >= 24)
            view.findViewById(R.id.play).setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void displayTimeForCountdown(long millisUntilFinished) {
//        if (millisUntilFinished / 1000 >= 1) {
//            ((TextView) view.findViewById(R.id.countdown)).setText(Long.toString(millisUntilFinished / 1000));
//            view.findViewById(R.id.countdown).setVisibility(View.VISIBLE);
//        } else {
//            ((TextView) view.findViewById(R.id.countdown)).setText(R.string.start);
//        }
        dealWithTimer(R.id.countdown, millisUntilFinished);
    }

    public void showPlayButton() {
        view.findViewById(R.id.play_button).setVisibility(View.VISIBLE);
    }

    public void openTonePopup(DatabaseSong song, Context context) {
        popupOpened = true;
        RelativeLayout viewGroup = view.findViewById(R.id.tone_picker);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.tone_picker_popup, viewGroup);
        placePopupOnScreen(context);
        popup.setFocusable(false);
        setArtistAndSongNames(song);
    }

    private void setArtistAndSongNames(DatabaseSong song) {
        ((TextView) popupView.findViewById(R.id.artist_name)).setText(song.getArtist());
        ((TextView) popupView.findViewById(R.id.song_name)).setText(song.getTitle());

    }

    private void setTonePopupAttributes(Context context, PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.781);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.576);
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public void dismissPopup() {
        popupOpened = false;
        popup.dismiss();
        undimBackground();
    }

    public void hideLoadingIndicator() {
        view.findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
    }

    public void resetResumeTimer() {
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
    }

    public boolean isPopupOpened() {
        return popupOpened;
    }


    @SuppressLint("SetTextI18n")
    public void showProgress(double progress, Context baseContext) {
        if (loadingAmount != null)

            loadingAmount.setText(progress + "%");
        if (!popupOpened)
            sendIntent(progress, baseContext);
    }

    private void sendIntent(double progress, Context baseContext) {
        Intent intent = new Intent();
        intent.setAction("changed");
        intent.putExtra("content", progress);
        LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent);
    }

    public View getPopupView() {
        return popupView;
    }

    public void displayTimeForRestartCountdown(long millisUntilFinished) {
        dealWithTimer(R.id.restart_countdown, millisUntilFinished);
    }

    private void dealWithTimer(int countdown, long millisUntilFinished) {
        if (millisUntilFinished / 1000 >= 1) {
            ((TextView) view.findViewById(countdown)).setText(Long.toString(millisUntilFinished / 1000));
            view.findViewById(countdown).setVisibility(View.VISIBLE);
        } else {
            ((TextView) view.findViewById(countdown)).setText(R.string.start);
        }
    }

    public void setScreenForPlayingAfterRestartTimerExpires() {
        view.findViewById(R.id.restart_countdown).setVisibility(View.INVISIBLE);
        if (sdkInt >= 24)
            view.findViewById(R.id.pause).setVisibility(View.VISIBLE);
        view.findViewById(R.id.play).setVisibility(View.INVISIBLE);
    }

    public void showShareItems() {
        popupView.findViewById(R.id.share_options_layout).setVisibility(View.VISIBLE);
        popupView.findViewById(R.id.end_options_layout).setVisibility(View.GONE);
        popupView.findViewById(R.id.subscript_layout).setVisibility(View.GONE);
    }

    public void hideShareItems() {
        popupView.findViewById(R.id.end_options_layout).setVisibility(View.VISIBLE);
        popupView.findViewById(R.id.share_options_layout).setVisibility(View.GONE);
        popupView.findViewById(R.id.loading_amount_window).setVisibility(View.VISIBLE);
    }

    public void showSubscribeOptions() {
        popupView.findViewById(R.id.subscript_layout).setVisibility(View.VISIBLE);
        popupView.findViewById(R.id.end_options_layout).setVisibility(View.GONE);
    }

    public void hideSubscribeOptions() {
        popupView.findViewById(R.id.end_options_layout).setVisibility(View.VISIBLE);
        popupView.findViewById(R.id.subscript_layout).setVisibility(View.GONE);
    }

    public void popupClosed() {
        popupOpened = false;
    }

    public View openRecordingsForDelete(Context context) {
        RelativeLayout viewGroup = view.findViewById(R.id.recordings_to_delete);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View recordingsView = layoutInflater.inflate(R.layout.recording_to_delete, viewGroup);

        placeRecordingsPopupOnScreen(recordingsView, context);
        return recordingsView;
    }

    private void placeRecordingsPopupOnScreen(View recordingsView, Context context) {
        recordingsPopup = new PopupWindow(context);
        recordingsPopup.setFocusable(true);
        setEndOptionsPopupAttributes(context, recordingsPopup, recordingsView);
        recordingsPopup.showAtLocation(recordingsView, Gravity.CENTER, 0, 0);
    }

    public void dismissRecordings() {
        recordingsPopup.dismiss();
    }

    public void changeChech(boolean cameraOn) {
        if (cameraOn) {
            popupView.findViewById(R.id.check).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.no_check).setVisibility(View.INVISIBLE);
        } else{
            popupView.findViewById(R.id.no_check).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.check).setVisibility(View.INVISIBLE);

        }
    }
}
