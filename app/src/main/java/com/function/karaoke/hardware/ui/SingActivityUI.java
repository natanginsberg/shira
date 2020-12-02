package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;

public class SingActivityUI {

    private final View view;
    private final DatabaseSong song;
    private View popupView;
    private PopupWindow popup;

    public SingActivityUI(View singActivity, DatabaseSong song) {
        this.view = singActivity;
        this.song = song;
    }

    public void addArtistToScreen() {
        ((TextView) view.findViewById(R.id.song_name)).setText(song.getTitle());
        ((TextView) view.findViewById(R.id.song_name_2)).setText(song.getTitle());
        ((TextView) view.findViewById(R.id.artist_name)).setText(song.getArtist());
    }

    public void setBackgroundColor() {
        view.findViewById(R.id.camera).setBackgroundColor(Color.BLACK);
    }

    public void setSurfaceForRecording(boolean cameraOn) {
        view.findViewById(R.id.play_button).setVisibility(View.GONE);
        view.findViewById(R.id.camera_toggle_button).setVisibility(View.INVISIBLE);
        if (!cameraOn)
            view.findViewById(R.id.logo).setVisibility(View.VISIBLE);


    }

    public void turnOffCameraOptions() {
        view.findViewById(R.id.camera_toggle_button).setVisibility(View.INVISIBLE);
    }

    public void clearLyricsScreen() {
        view.findViewById(R.id.song_name_2).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.artist_name).setVisibility(View.INVISIBLE);
    }

    public void setScreenForPlayingAfterTimerExpires() {
        view.findViewById(R.id.open_end_options).setVisibility(View.VISIBLE);
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.pause).setVisibility(View.VISIBLE);
        view.findViewById(R.id.play).setVisibility(View.INVISIBLE);
    }

    public void openEndPopup(Context context, boolean songEnded) {
        RelativeLayout viewGroup = view.findViewById(R.id.end_options);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.end_song_options, viewGroup);
        if (songEnded) {
            popupView.findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
        }
        placePopupOnScreen(context);
        applyDim();
    }

    private void placePopupOnScreen(Context context) {
        popup = new PopupWindow(context);
        popup.setFocusable(true);
        setPopupAttributes(context, popup, popupView);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void applyDim() {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, view.findViewById(R.id.sing_song).getWidth(), view.findViewById(R.id.sing_song).getHeight());
        dim.setAlpha((int) (255 * (float) 0.8));
        ViewOverlay overlay = view.findViewById(R.id.sing_song).getOverlay();
        overlay.add(dim);
    }

    public void undimBackground() {
        ViewOverlay overlay = view.findViewById(R.id.sing_song).getOverlay();
        overlay.clear();
    }

    private void setPopupAttributes(Context context, PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.781);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.576);
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public void resetPage() {
        deleteAllCurrentLyrics();
        resetProgressBar();
        setInvisibleIcons();
        setVisibleIcons();
    }


    private void deleteAllCurrentLyrics() {
        ((TextView) view.findViewById(R.id.lyrics)).setText("");
        ((TextView) view.findViewById(R.id.words_to_read)).setText("");
        ((TextView) view.findViewById(R.id.words_to_read_2)).setText("");
        ((TextView) view.findViewById(R.id.words_to_read_3)).setText("");
    }

    private void resetProgressBar() {
        ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);
        ((TextView) view.findViewById(R.id.duration)).setText("");
    }

    private void setInvisibleIcons() {
        view.findViewById(R.id.open_end_options).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.pause).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.play).setVisibility(View.INVISIBLE);
    }

    private void setVisibleIcons() {
        view.findViewById(R.id.play_button).setVisibility(View.VISIBLE);
        view.findViewById(R.id.camera_toggle_button).setVisibility(View.VISIBLE);
        view.findViewById(R.id.back_button).setVisibility(View.VISIBLE);
        view.findViewById(R.id.song_name_2).setVisibility(View.VISIBLE);
        view.findViewById(R.id.artist_name).setVisibility(View.VISIBLE);
    }

    public void removeResumeOptionFromPopup() {
        popupView.findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
    }

    public void songPaused() {
        view.findViewById(R.id.pause).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.play).setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void displayTimeForCountdown(long millisUntilFinished) {
        if (millisUntilFinished / 1000 >= 1) {
            ((TextView) view.findViewById(R.id.countdown)).setText(Long.toString(millisUntilFinished / 1000));
            view.findViewById(R.id.countdown).setVisibility(View.VISIBLE);
        } else {
            ((TextView) view.findViewById(R.id.countdown)).setText(R.string.start);
        }
    }

    public void showPlayButton() {
        view.findViewById(R.id.play_button).setVisibility(View.VISIBLE);
    }
}
