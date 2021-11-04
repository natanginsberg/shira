package com.function.karaoke.interaction.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOverlay;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.function.karaoke.core.utility.BlurBuilder;
import com.function.karaoke.interaction.R;
import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.utils.static_classes.Converter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

public class SingActivityUI {

    private static final String SHARE_FUNC = "share";
    private final View view;
    private final DatabaseSong song;
    private final int sdkInt;
    private View popupView;
    private PopupWindow popup;
    private boolean popupOpened = false;
    private LinearLayout loadingAmount;
    private PopupWindow recordingsPopup;
    private boolean songEnded;
    private View secondPopupView;
    private PopupWindow secondPopup;
    //    private Context context;
    private WeakReference<Activity> activityWeakReference;
    private CountDownTimer cTimer;
    private boolean prepared;
    private View songUploadedView;
    private static boolean finished = false;

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

    @SuppressLint("SetJavaScriptEnabled")
    public void setSurfaceForRecording(boolean cameraOn) {
        view.findViewById(R.id.play_button).setVisibility(View.GONE);
        if (!cameraOn) {
            WebView webview = (WebView) view.findViewById(R.id.logo);
            webview.setVisibility(View.VISIBLE);
            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            webview.setWebChromeClient(new WebChromeClient());
            webview.setBackgroundColor(Color.BLACK);
            String file = readFileToString("ashira_ani.html");
            webview.loadDataWithBaseURL("file:///android_asset/", file, "text/html", "utf-8", null);
        }
//            view.findViewById(R.id.logo).setVisibility(View.VISIBLE);


    }

    private String readFileToString(String s) {
        BufferedReader br = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(activityWeakReference.get().getAssets().open(s), StandardCharsets.UTF_8));
            String myLine;
            while ((myLine = br.readLine()) != null) {
                stringBuilder.append(myLine);
                stringBuilder.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
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
//        if (sdkInt >= 24)
        view.findViewById(R.id.pause).setVisibility(View.VISIBLE);
//        else
//            moveStopButtonToMiddle();
        hidePlayAndStop();
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
        loadingAmount = view.findViewById(R.id.loading_percent);
        this.songEnded = songEnded;
        placePopupOnScreen(context);
        popup.setFocusable(true);
        applyDim(view.findViewById(R.id.sing_song).getOverlay(), context);
    }

    private void placePopupOnScreen(Context context) {
        popup = new PopupWindow(context);

        setPopupAttributes(context, popup, popupView);
        if (view != null && context != null)
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
            height = Math.min((int) (width * 1.65), (int) (context.getResources().getDisplayMetrics().heightPixels * 0.8));
            ImageView imageView = layout.findViewById(R.id.check);
            imageView.getLayoutParams().height = ((int) (width * .25));
            imageView.getLayoutParams().width = ((int) (width * .25));
        } else
            height = Math.min((int) (width * 1.57), (int) (context.getResources().getDisplayMetrics().heightPixels * 0.65));
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
        dealWithTimer(R.id.countdown, millisUntilFinished);
    }

    public void showPlayButton() {
        view.findViewById(R.id.play_button).setVisibility(View.VISIBLE);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public void openTonePopup(DatabaseSong song, Activity activity) {
        popupOpened = true;
        this.activityWeakReference = new WeakReference<>(activity);
        RelativeLayout viewGroup = view.findViewById(R.id.tone_picker);
        LayoutInflater layoutInflater = (LayoutInflater) activityWeakReference.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.tone_picker_popup, viewGroup);
        placePopupOnScreen(activityWeakReference.get());
        popup.setFocusable(false);
        setArtistAndSongNames(song);
        view.findViewById(R.id.play_button).setVisibility(View.INVISIBLE);
    }

    private void setArtistAndSongNames(DatabaseSong song) {
        ((TextView) popupView.findViewById(R.id.artist_name)).setText(song.getArtist());
        ((TextView) popupView.findViewById(R.id.song_name)).setText(song.getTitle());

    }

    public void dismissPopup() {
        popupOpened = false;
        popup.dismiss();
        undimBackground();
    }

    private void hideLoadingIndicator() {
        view.findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
    }

    public void resetResumeTimer() {
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
    }

    public boolean isPopupOpened() {
        return popupOpened;
    }


    private View createViewForLoading(double content, Recording recording) {
        View to_add = LayoutInflater.from(activityWeakReference.get()).inflate(R.layout.recording_upload, null);
        addValuesToView(to_add, content, recording);
        return to_add;
    }

    private void addValuesToView(View loadingView, double content, Recording recording) {
        ((TextView) loadingView.findViewById(R.id.artist_name)).setText(recording.getArtist());
        ((TextView) loadingView.findViewById(R.id.song_name)).setText(recording.getTitle());
        ShapeableImageView mCover = loadingView.findViewById(R.id.recording_album_pic);
        if (!recording.getImageResourceFile().equals("") && !recording.getImageResourceFile().equals("no image resource")) {
            Picasso.get()
                    .load(recording.getImageResourceFile())
                    .placeholder(R.drawable.plain_rec)
                    .fit()
                    .into(mCover);
        }
        mCover.setShapeAppearanceModel(mCover.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, Converter.convertDpToPx(10))
                .build());
        addPercentLoaded(loadingView, content);

    }

    @SuppressLint("SetTextI18n")
    private void addPercentLoaded(View loadingView, double content) {
        ((TextView) loadingView.findViewById(R.id.loading_percent_upload)).setText("Loading " + content + "%");
    }

    private void sendIntent(double progress, Context baseContext, Recording recording) {
        Intent intent = new Intent();
        intent.setAction("changed");
        intent.putExtra("content", progress);
        intent.putExtra("recording", recording);
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
//        if (sdkInt >= 24)
        view.findViewById(R.id.pause).setVisibility(View.VISIBLE);
        hidePlayAndStop();
    }


    public void popupClosed() {
        popupOpened = false;
    }

    public void changeCheck(boolean cameraOn) {
        if (cameraOn) {
            popupView.findViewById(R.id.check).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.no_check).setVisibility(View.INVISIBLE);
            ((TextView) popupView.findViewById(R.id.check_label)).setText(activityWeakReference.get().getResources().getString(R.string.with_video_when_chosing));
        } else {
            popupView.findViewById(R.id.no_check).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.check).setVisibility(View.INVISIBLE);
            ((TextView) popupView.findViewById(R.id.check_label)).setText(activityWeakReference.get().getResources().getString(R.string.without_video_when_chosing));
        }
    }

    public void changeLayout() {
        view.findViewById(R.id.initial_album_cover).setVisibility(View.INVISIBLE);
    }

    public void setSongInfo() {
        view.findViewById(R.id.song_info).setVisibility(View.VISIBLE);
        ShapeableImageView mCover = view.findViewById(R.id.recording_album_pic);
        if (!song.getImageResourceFile().equals("") && !song.getImageResourceFile().equals("no image resource")) {
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


    public void closePopup() {
        secondPopup.dismiss();
    }

    private void placeSignUpOptionsOnScreen(Context context) {
        secondPopup = new PopupWindow(context);
        setSignUpPopupAttributes(context, secondPopup, secondPopupView);
        secondPopup.showAtLocation(secondPopupView, Gravity.CENTER, 0, 0);
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

    public void showLoadingIcon() {
//        startTimerForPercent();
        ProgressBar progressBar = view.findViewById(R.id.loading_indicator);
        progressBar.setVisibility(View.VISIBLE);
        if (cTimer == null) {
            cTimer = new CountDownTimer(7000, 70) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    if (prepared) {
                        cTimer.cancel();
                        showPlayButton();
                        hideLoadingIndicator();
                        cTimer = null;
                    }
                    progressBar.setProgress((int) (100 - millisUntilFinished / 70));
                }

                public void onFinish() {
                    cTimer.cancel();
                    cTimer = null;
                    if (!prepared) {
                        progressBar.setProgress(0);
                        showLoadingIcon();
                    } else {
                        showPlayButton();
                        hideLoadingIndicator();
                    }
                }
            };
            cTimer.start();
        }
    }

    private void startTimerForPercent() {
        CountDownTimer countDownTimer = null;
        countDownTimer = new CountDownTimer(5000, 50) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                ((TextView) view.findViewById(R.id.loading_progress)).setText(((int) 100 - (millisUntilFinished / 50)) + "%");
            }

            public void onFinish() {
                view.findViewById(R.id.loading_progress).setVisibility(View.INVISIBLE);
                showPlayButton();
            }
        };
        countDownTimer.start();
    }

    public void showPopupLoadingIndicator() {

        popupView.findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);
    }

    public void hidePopupLoadingIndicator() {
        popupView.findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
    }

    public void showAlbumInBackground() {
        if (!song.getImageResourceFile().equals("") && !song.getImageResourceFile().equals("no image resource")) {
            ImageView imageView = (ImageView) view.findViewById(R.id.initial_album_cover);
            Picasso.get()
                    .load(song.getImageResourceFile())
//                .resize(450, 420)
                    .centerCrop()
                    .fit()
                    .into(imageView);
        }
    }

    public void showGoodSuccessSignIn(TimerListener timerListener) {
        showSuccess(timerListener, activityWeakReference.get().getResources().getString(R.string.cuccessfull_sign_in));
    }

    private void showPopupForOneSecond(PopupWindow popupWindow, TimerListener timerListener) {
        if (cTimer != null) {
            cTimer.cancel();
            cTimer = null;
        }
        if (cTimer == null) {
            cTimer = new CountDownTimer(2500, 500) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    if (cTimer != null)
                        cTimer.cancel();
                    if (popupWindow != null)
                        popupWindow.dismiss();
                    timerListener.timerOver();
                    cTimer = null;
                }
            };
            cTimer.start();
        }
    }

    public void setPrepared() {
        prepared = true;
    }

    public void showSaveFail(TimerListener timerListener) {
        showFail(timerListener, activityWeakReference.get().getResources().getString(R.string.unable_to_upload));
    }

    public void showFail(TimerListener timerListener, String wording) {
        if (view != null && activityWeakReference.get() != null && popupOpened) {
            PopupWindow popupWindow = IndicationPopups.openXIndication(activityWeakReference.get(), view, wording);
            showPopupForOneSecond(popupWindow, timerListener);
        }
    }

    public void showSuccess(TimerListener timerListener, String wording) {
        if (view != null && activityWeakReference.get() != null && popupOpened) {
            PopupWindow popupWindow = IndicationPopups.openCheckIndication(activityWeakReference.get(), view, wording);
            showPopupForOneSecond(popupWindow, timerListener);
        }
    }

    public void changeEndWordingToFinishedWatching() {
        if (popupView != null && activityWeakReference.get() != null)
            ((TextView) popupView.findViewById(R.id.end_song_words)).setText(activityWeakReference.get().getResources().getString(R.string.finshed_watching_recording));
    }

    public void showRecordingError(TimerListener timerListener) {
        showFail(timerListener, activityWeakReference.get().getString(R.string.recorder_error));
    }

    public void removeEarphonePrompt() {
        view.findViewById(R.id.attach_earphones_text).setVisibility(View.GONE);
    }

    public void showEarphonePrompt() {
        view.findViewById(R.id.attach_earphones_text).setVisibility(View.VISIBLE);
    }

    public void failBackground() {
        if (view != null)
//            popupView.findViewById(R.id.all_options).setBackgroundColor(Color.RED);
            showFail(new TimerListener() {
                @Override
                public void timerOver() {

                }
            }, activityWeakReference.get().getResources().getString(R.string.download_failed));
    }

    public void changeBackground() {
        if (view != null) {
//            popupView.findViewById(R.id.all_options).setBackgroundColor(Color.GREEN);
            popupView.findViewById(R.id.download).setVisibility(View.GONE);

            showSuccess(new TimerListener() {
                @Override
                public void timerOver() {

                }
            }, activityWeakReference.get().getResources().getString(R.string.download_complete));
        }
    }

    public void changeLoadingPercent(int percent, Context baseContext, Recording recording) {
        String textToDisplay = activityWeakReference.get().getResources().getString(R.string.loading_percent, percent) + "%";
        if (popupView != null && popupOpened) {
            ((TextView) popupView.findViewById(R.id.download_text)).setText(textToDisplay);
        } else {
            sendIntent(percent, baseContext, recording);
        }
    }

    public void showOutOfMemory(TimerListener timerListener) {
        showFail(timerListener, activityWeakReference.get().getResources().getString(R.string.out_of_memory));
    }

    public void showNotEnoughSung() {
        if (view != null) {
            showFail(new TimerListener() {
                @Override
                public void timerOver() {

                }
            }, activityWeakReference.get().getResources().getString(R.string.not_enough_sung));
        }
    }

    public void activityFinished() {
        finished = true;
    }

    public void showLoadingIndicator() {
        if (popupView != null) {
            popupView.findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);
            if (cTimer == null) {
                cTimer = new CountDownTimer(2500, 1500) {
                    @SuppressLint("SetTextI18n")
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        view.findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
                    }
                };
                cTimer.start();
            }
        }
    }


    public interface SignInListener {
        void openSignIn();
    }

    public interface FreeShareListener {
        void startSaveProcess(boolean freeShareUsed);
    }

    public interface ShareListener {

        void share(View view, boolean video, String password);

        void setPassword(TextView viewById);
    }

    public interface TimerListener {
        void timerOver();
    }
}
