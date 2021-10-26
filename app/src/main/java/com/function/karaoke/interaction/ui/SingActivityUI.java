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
import android.widget.Button;
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
import com.function.karaoke.interaction.activities.Model.UserInfo;
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
//        view.findViewById(R.id.camera_toggle_button).setVisibility(View.INVISIBLE);
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
        loadingAmount = view.findViewById(R.id.loading_percent);
        this.songEnded = songEnded;
        placePopupOnScreen(context);
        popup.setFocusable(true);
        if (sdkInt < 24) {
            setScreenWithoutSaveOption();
        }
        applyDim(view.findViewById(R.id.sing_song).getOverlay(), context);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setScreenWithoutSaveOption() {
        popupView.findViewById(R.id.share).setBackground(activityWeakReference.get().getDrawable(R.drawable.no_share_background));
        popupView.findViewById(R.id.save).setVisibility(View.GONE);
        ((TextView) popupView.findViewById(R.id.sale_promo)).setText(activityWeakReference.get().getString(R.string.no_save_text));
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

    private void hideLoadingIndicator() {
        view.findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
    }

    public void resetResumeTimer() {
        view.findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
    }

    public boolean isPopupOpened() {
        return popupOpened;
    }


    @SuppressLint("SetTextI18n")
    public void showProgress(double progress, Context baseContext, Recording recording) {
        if (loadingAmount != null) {
            addRecordingToScreen(progress, recording);
            hidePlayAndStop();
        }
        if (!popupOpened)
            sendIntent(progress, baseContext, recording);
    }

    private void addRecordingToScreen(double content, Recording recording) {
        songUploadedView = createViewForLoading(content, recording);
        loadingAmount.removeAllViews();
        loadingAmount.addView(songUploadedView);
        if (recording != null)
            addPercentLoaded(songUploadedView, content);
        if (recording != null && content >= 100) {
            loadingAmount.removeAllViews();
        }
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

    public void openInitialShareOptions(Context context, UserInfo user, FreeShareListener freeShareListener, String funcToCall) {
        popupView.setVisibility(View.INVISIBLE);
        if (user != null && user.getFreeShares() > 0) {
            openSharePopup(context, R.id.new_user_share, R.layout.free_share_screen);
        } else {
            openSharePopup(context, R.id.new_member_screen, R.layout.new_member_screen);
        }
        if (user != null && user.getFreeShares() > 0) {
            setRecordingsLeftNumber(context, user, freeShareListener);
            setAppropriateWord(context, funcToCall);
        }
        placeSignUpOptionsOnScreen(context);
        secondPopup.setFocusable(true);
        secondPopup.setOnDismissListener(() -> popupView.setVisibility(View.VISIBLE));
    }

    private void setAppropriateWord(Context context, String funcToCall) {
        String buttonText = funcToCall.equals(SHARE_FUNC) ? context.getResources().getString(R.string.share_save) : context.getResources().getString(R.string.save_recording);
        String titleText = funcToCall.equals(SHARE_FUNC) ? context.getResources().getString(R.string.share) : context.getResources().getString(R.string.save_recording);
        ((Button) secondPopupView.findViewById(R.id.save_free_recordings)).setText(buttonText);
        ((TextView) secondPopupView.findViewById(R.id.share_title)).setText(titleText);
    }

    private void openSharePopup(Context context, int id, int laoyout) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(id);
        secondPopupView = layoutInflater.inflate(laoyout, viewGroup);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setRecordingsLeftNumber(Context context, UserInfo user, FreeShareListener freeShareListener) {
        String textToDisplay = context.getResources().getString(R.string.share_left_label, user.getFreeShares());
        ((TextView) secondPopupView.findViewById(R.id.free_saves_left_text)).setText(textToDisplay);
        secondPopupView.findViewById(R.id.save_free_recordings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                secondPopup.dismiss();
                freeShareListener.startSaveProcess(true);
            }
        });

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

    public void openShareOptions(Context context, UserInfo userInfo, ShareListener shareListener, boolean cameraOn) {
        popupView.setVisibility(View.INVISIBLE);
        ShareOptionsUI shareOptionsUI = new ShareOptionsUI(view, userInfo, cameraOn);
        shareOptionsUI.openShareOptions(context, shareListener);
        setThirdPopupDismissListener(shareOptionsUI);
    }

    private void setThirdPopupDismissListener(ShareOptionsUI shareOptionsUI) {
        shareOptionsUI.getThirdPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (shareOptionsUI.isClear())
                    popupView.setVisibility(View.VISIBLE);
                else
                    setThirdPopupDismissListener(shareOptionsUI);
            }
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
        PopupWindow popupWindow = IndicationPopups.openCheckIndication(activityWeakReference.get(), view, activityWeakReference.get().getResources().getString(R.string.cuccessfull_sign_in));
        showPopupForOneSecond(popupWindow, timerListener);
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

    public void showSaveStart(TimerListener timerListener) {
        ((TextView) popupView.findViewById(R.id.save)).setTextColor(activityWeakReference.get().getResources().getColor(R.color.pressed_text_color));
        PopupWindow popupWindow = IndicationPopups.openCheckIndication(activityWeakReference.get(), view, activityWeakReference.get().getResources().getString(R.string.save_in_progress));
        showPopupForOneSecond(popupWindow, timerListener);
    }

    public void showSaveFail(TimerListener timerListener) {
        showFail(timerListener, activityWeakReference.get().getResources().getString(R.string.unable_to_upload));
    }

    public void showFail(TimerListener timerListener, String wording) {
        if (view != null) {
            PopupWindow popupWindow = IndicationPopups.openXIndication(activityWeakReference.get(), view, wording);
            showPopupForOneSecond(popupWindow, timerListener);
        }
    }

    public void changeEndWordingToFinishedWatching() {
        if (popupView != null)
            ((TextView) popupView.findViewById(R.id.end_song_words)).setText(activityWeakReference.get().getResources().getString(R.string.finshed_watching_recording));
    }

    public void showSlowInternetError(TimerListener timerListener) {
        showFail(timerListener, activityWeakReference.get().getString(R.string.slow_internet));
    }

    public void showErrorPausingVideo() {

    }

    public void showRecordingError(TimerListener timerListener) {
        showFail(timerListener, activityWeakReference.get().getString(R.string.recorder_error));
    }

    public void removeEarphonePrompt() {
        view.findViewById(R.id.attach_earphones_text).setVisibility(View.GONE);
    }

    public void showEarphonePrompt() {
        view.findViewById(R.id.attach_earphones_text).setVisibility(View.GONE);
    }

    public void failBackground() {
        if (view != null)
            popupView.findViewById(R.id.all_options).setBackgroundColor(Color.RED);
    }

    public void changeBackground() {
        if (view != null) {
            popupView.findViewById(R.id.all_options).setBackgroundColor(Color.GREEN);
            popupView.findViewById(R.id.download_sizes).setVisibility(View.GONE);
        }
    }

    public void changeBackgroundToBlue() {
        if (view != null)
            popupView.findViewById(R.id.all_options).setBackgroundColor(Color.BLUE);
    }

    public void showOutOfMemory(TimerListener timerListener) {
        showFail(timerListener, activityWeakReference.get().getResources().getString(R.string.out_of_memory));
    }

    public void openDownloadOptions() {
        if (view != null) {
            popupView.findViewById(R.id.download_sizes).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.download).setVisibility(View.GONE);
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
