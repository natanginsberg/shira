package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.function.karaoke.core.utility.BlurBuilder;
import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.utils.static_classes.Converter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.squareup.picasso.Picasso;

public class SingActivityUI {

    private static final int NUMBER_OF_FREE_SHARES = 130;
    private final View view;
    private final DatabaseSong song;
    private final int sdkInt;
    private View popupView;
    private PopupWindow popup;
    private boolean popupOpened = false;
    private TextView loadingAmount;
    private PopupWindow recordingsPopup;
    private boolean songEnded;
    private PopupWindow previousPopupWindow;
    private View previousPopupView;
    private View secondPopupView;
    private PopupWindow secondPopup;
    private View thirdPopupView;
    private PopupWindow thirdPopup;
    private UserInfo user;
    private ShareListener mListener;
    private Context context;

    public SingActivityUI(View singActivity, DatabaseSong song, int sdkInt) {
        this.view = singActivity;
        this.song = song;
        this.sdkInt = sdkInt;
    }

    public void addTitleToScreen() {
        ((TextView) view.findViewById(R.id.song_name)).setText(song.getTitle());
        ((TextView) view.findViewById(R.id.artist_name)).setText(song.getArtist());
    }

    public void setBackgroundColor() {
        view.findViewById(R.id.camera_holder).setBackgroundColor(Color.BLACK);
    }

    public void setSurfaceForRecording(boolean cameraOn) {
        view.findViewById(R.id.play_button).setVisibility(View.GONE);
//        view.findViewById(R.id.camera_toggle_button).setVisibility(View.INVISIBLE);
        if (!cameraOn)
            view.findViewById(R.id.logo).setVisibility(View.VISIBLE);


    }

    public void turnOffCameraOptions() {
        changeCheck(false);
        turnOffClickListeners();
//        view.findViewById(R.id.camera_toggle_button).setVisibility(View.INVISIBLE);
    }

    private void turnOffClickListeners() {
        popupView.findViewById(R.id.check).setOnClickListener(null);
        popupView.findViewById(R.id.no_check).setOnClickListener(null);
        popupView.findViewById(R.id.check_holder).setOnClickListener(null);
    }

    public void setScreenForPlayingAfterTimerExpires() {
//        view.findViewById(R.id.open_end_options).setVisibility(View.VISIBLE);
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
        if (sdkInt >= 24)
            view.findViewById(R.id.pause).setVisibility(View.VISIBLE);
        else
            moveStopButtonToMiddle();
        hidePlayAndStop();
    }

    private void moveStopButtonToMiddle() {
        TextView stop = (TextView) (view.findViewById(R.id.stop));
        stop.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
    }

    public void resetScreenForTimer() {
        resetLyricsScreen();
        showPlayButton();
//        view.findViewById(R.id.camera_toggle_button).setVisibility(View.VISIBLE);
    }

    private void resetLyricsScreen() {
//        view.findViewById(R.id.song_name_2).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.artist_name).setVisibility(View.VISIBLE);
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
    }

    public void openEndPopup(Context context, boolean songEnded) {
        popupOpened = true;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (songEnded) {
            RelativeLayout viewGroup = view.findViewById(R.id.end_options_end_song);
            popupView = layoutInflater.inflate(R.layout.end_song_options_end_song, viewGroup);
        } else {
            RelativeLayout viewGroup = view.findViewById(R.id.end_options_mid_song);
            popupView = layoutInflater.inflate(R.layout.end_song_options_mid_song, viewGroup);
        }
        loadingAmount = popupView.findViewById(R.id.save_recording);
        this.songEnded = songEnded;
        placePopupOnScreen(context);
        popup.setFocusable(true);
        applyDim(view.findViewById(R.id.sing_song).getOverlay(), context);
    }

    private void placePopupOnScreen(Context context) {
        popup = new PopupWindow(context);

        setPopupAttributes(context, popup, popupView);
        view.post(new Runnable() {
            @Override
            public void run() {
                popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            }
        });
    }

    private void applyDim(ViewOverlay overlay, Context context) {
        Drawable colorDim = new ColorDrawable(Color.WHITE);
        colorDim.setBounds(0, 0, view.getWidth(), view.getHeight());
        colorDim.setAlpha((int) (255 * (float) 0.7));
//
        Drawable dim = new BitmapDrawable(context.getResources(), BlurBuilder.blur(view));
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.7));
        overlay.add(colorDim);
        overlay.add(dim);
    }

    public void undimBackground() {
        ViewOverlay overlay = view.findViewById(R.id.sing_song).getOverlay();
        overlay.clear();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setPopupAttributes(Context context, PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.77);
        int height;
        if (songEnded) {
            height = (int) (width * 1.5);
            ImageView imageView = layout.findViewById(R.id.check);
            imageView.getLayoutParams().height = ((int) (width * .25));
            imageView.getLayoutParams().width = ((int) (width * .25));
        } else
            height = Math.min((int) (width * 1.3), (int) (context.getResources().getDisplayMetrics().heightPixels * 0.558));
//        height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.558);
        popup.setContentView(layout);
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.unclicked_recording_background));
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public PopupWindow getPopup() {
        return popup;
    }


    public void songPaused() {
        view.findViewById(R.id.pause).setVisibility(View.INVISIBLE);
        if (sdkInt >= 24)
            showPlayAndStop();
    }

    private void showPlayAndStop() {
        view.findViewById(R.id.play).setVisibility(View.VISIBLE);
        view.findViewById(R.id.stop).setVisibility(View.VISIBLE);
        view.findViewById(R.id.continue_text).setVisibility(View.VISIBLE);
        view.findViewById(R.id.stop_text).setVisibility(View.VISIBLE);
    }

    private void hidePlayAndStop() {
        view.findViewById(R.id.play).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.stop).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.continue_text).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.stop_text).setVisibility(View.INVISIBLE);
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

    public void showPlayButton() {//todo show regular play button
        view.findViewById(R.id.play_button).setVisibility(View.VISIBLE);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public void openTonePopup(DatabaseSong song, Context context) {
        popupOpened = true;
        this.context = context;
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
        hidePlayAndStop();
    }

    public void showShareItems() {
//        popupView.findViewById(R.id.share_options_layout).setVisibility(View.VISIBLE);
//        popupView.findViewById(R.id.end_options_layout).setVisibility(View.GONE);
//        popupView.findViewById(R.id.subscript_layout).setVisibility(View.GONE);
    }

    public void hideShareItems() {
//        popupView.findViewById(R.id.end_options_layout).setVisibility(View.VISIBLE);
//        popupView.findViewById(R.id.share_options_layout).setVisibility(View.GONE);
//        popupView.findViewById(R.id.loading_amount_window).setVisibility(View.VISIBLE);
    }

    public void showSubscribeOptions() {
//        popupView.findViewById(R.id.subscript_layout).setVisibility(View.VISIBLE);
//        popupView.findViewById(R.id.end_options_layout).setVisibility(View.GONE);
    }

    public void hideSubscribeOptions() {
//        popupView.findViewById(R.id.end_options_layout).setVisibility(View.VISIBLE);
//        popupView.findViewById(R.id.subscript_layout).setVisibility(View.GONE);
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
        setPopupAttributes(context, recordingsPopup, recordingsView);
        recordingsPopup.showAtLocation(recordingsView, Gravity.CENTER, 0, 0);
    }

    public void dismissRecordings() {
        recordingsPopup.dismiss();
    }

    public void changeCheck(boolean cameraOn) {
        if (cameraOn) {
            popupView.findViewById(R.id.check).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.no_check).setVisibility(View.INVISIBLE);
            ((TextView) popupView.findViewById(R.id.check_label)).setText(context.getResources().getString(R.string.with_video));
        } else {
            popupView.findViewById(R.id.no_check).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.check).setVisibility(View.INVISIBLE);
            ((TextView) popupView.findViewById(R.id.check_label)).setText(context.getResources().getString(R.string.without_video));
        }
    }

    public void changeLayout() {
        view.findViewById(R.id.initial_album_cover).setVisibility(View.INVISIBLE);
    }

    public void setSongInfo() {
        view.findViewById(R.id.song_info).setVisibility(View.VISIBLE);
        ShapeableImageView mCover = view.findViewById(R.id.recording_album_pic);
        if (!song.getImageResourceFile().equals("")) {
            Picasso.get()
                    .load(song.getImageResourceFile())
                    .placeholder(R.drawable.plain_rec)
                    .fit()
                    .into(mCover);
        }
        mCover.setShapeAppearanceModel(mCover.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, Converter.convertDpToPx(10))
                .build());
    }

    public void setTotalLength(int duration) {
        int minutes = duration / 60;
        int seconds = duration % 60;
        @SuppressLint("DefaultLocale") String text = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        ((TextView) view.findViewById(R.id.all_time)).setText(text);
    }

    public void openInitialShareOptions(Context context, UserInfo user, FreeShareListener freeShareListener) {
        popupView.setVisibility(View.INVISIBLE);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.new_user_share);
        secondPopupView = layoutInflater.inflate(R.layout.new_user_share_screen, viewGroup);

        if (user != null)
            setRecordingsLeftNumber(context, user, freeShareListener);
        placeSignUpOptionsOnScreen(context);
        secondPopup.setFocusable(true);
        secondPopup.setOnDismissListener(() -> popupView.setVisibility(View.VISIBLE));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setRecordingsLeftNumber(Context context, UserInfo user, FreeShareListener freeShareListener) {
        String textToDisplay = context.getResources().getString(R.string.share_left_label, user.getFreeShares());
        ((TextView) secondPopupView.findViewById(R.id.free_saves_left_text)).setText(textToDisplay);
        if (user.getFreeShares() <= 0) {
            secondPopupView.findViewById(R.id.save_free_recordings).setBackground(context.getResources().getDrawable(R.drawable.ic_search_port_gray));
            //todo add click to payment
        } else {
            secondPopupView.findViewById(R.id.save_free_recordings).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    secondPopup.dismiss();
                    freeShareListener.startSaveProcess(true);
                }
            });
        }
    }

    private void placeSignUpOptionsOnScreen(Context context) {
        secondPopup = new PopupWindow(context);
        setSignUpPopupAttributes(context, secondPopup, secondPopupView);
        view.post(() -> secondPopup.showAtLocation(secondPopupView, Gravity.CENTER, 0, 0));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setSignUpPopupAttributes(Context context, PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8);
        int height = (int) (width * 1.3);
        popup.setContentView(layout);
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.unclicked_recording_background));
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public void openSignInPopup(Context context, SignInListener listener) {
        popupView.setVisibility(View.INVISIBLE);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.sign_in_end_song);
        secondPopupView = layoutInflater.inflate(R.layout.sign_in_end_of_song, viewGroup);

        placeSignUpOptionsOnScreen(context);
        secondPopup.setFocusable(true);
        secondPopup.setOnDismissListener(() -> popupView.setVisibility(View.VISIBLE));
        secondPopupView.findViewById(R.id.sign_up).setOnClickListener(view -> {
            listener.openSignIn();
            secondPopup.dismiss();
        });
    }

    public void openShareOptions(Context context, UserInfo userInfo, ShareListener shareListener) {
        popupView.setVisibility(View.INVISIBLE);
        ShareOptionsUI shareOptionsUI = new ShareOptionsUI(view, userInfo);
        shareOptionsUI.openShareOptions(context, shareListener);
        getThirdPopupDismissListener(shareOptionsUI);

    }

    private void getThirdPopupDismissListener(ShareOptionsUI shareOptionsUI) {
        shareOptionsUI.getThirdPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (shareOptionsUI.isClear())
                    popupView.setVisibility(View.VISIBLE);
                else
                    getThirdPopupDismissListener(shareOptionsUI);
            }
        });
    }

    public interface SignInListener {
        void openSignIn();
    }

    public interface FreeShareListener {
        void startSaveProcess(boolean freeShareUsed);
    }

    public interface ShareListener {
        void createShareLink(TextView viewById);

        void share(View view);

        CharSequence getLink();

        void setPassword(TextView viewById);
    }
}
