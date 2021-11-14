package com.function.karaoke.interaction.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.function.karaoke.core.utility.BlurBuilder;
import com.function.karaoke.interaction.R;
import com.function.karaoke.interaction.activities.Model.Genres;

import java.lang.ref.WeakReference;

public class SongsActivityUI {

    private final View view;
    //    private final Context context;
    private final WeakReference<Activity> activityWeakReference;
    private final GenresUI genreUI;
    private PopupWindow suggestPopup;
    private View suggestionView;
    private CountDownTimer cTimer;
    private View paymentSignInView;
    private PopupWindow paymentPopup;

    public SongsActivityUI(View songsActivity, GenresUI.GenreUIListener listener, String currentLanguage, Context context, Activity activity) {
        this.view = songsActivity;
//        this.context = context;
        activityWeakReference = new WeakReference<>(activity);
        genreUI = new GenresUI(songsActivity, activity, currentLanguage, listener);
    }

    public void addGenresToScreen(Genres genres, String currentGenre, int displayed) {
        genreUI.addGenresToScreen(genres, currentGenre, displayed);
    }

    public View openSongSuggestionsPopup() {
        RelativeLayout viewGroup = view.findViewById(R.id.song_suggestion);
        LayoutInflater layoutInflater = (LayoutInflater) activityWeakReference.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        suggestionView = layoutInflater.inflate(R.layout.song_suggestion, viewGroup);
        placeSuggestionOnScreen(suggestionView);
        return suggestionView;
    }


    private void placeSuggestionOnScreen(View suggestionView) {
        suggestPopup = new PopupWindow(activityWeakReference.get());
        suggestPopup.setContentView(suggestionView);
        suggestPopup.setHeight((int) (view.getHeight() * 0.92));
        suggestPopup.setWidth(view.getWidth());
        suggestPopup.setTouchable(true);
        suggestPopup.setFocusable(true);
        suggestPopup.setOutsideTouchable(true);
        suggestPopup.showAtLocation(suggestionView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);

    }

    public void addGenreToScreen(String genre) {
        genreUI.addGenreToScreen(genre);
    }

    public void showSongInSystem() {
        suggestionView.findViewById(R.id.song_in_system).setVisibility(View.VISIBLE);
    }

    public boolean closePopup() {
        if (suggestPopup != null) {
            suggestPopup.dismiss();
            return true;
        }
        return false;
    }

    public void makeTextInvisible() {
        suggestionView.findViewById(R.id.song_in_system).setVisibility(View.INVISIBLE);
    }

    public PopupWindow getSuggestPopup() {
        return suggestPopup;
    }

    public void showRequestAccepted() {
        PopupWindow popupWindow = IndicationPopups.openCheckIndication(activityWeakReference.get(), view, activityWeakReference.get().getResources().
                getString(R.string.request_received));
        showPopupForOneSecond(popupWindow);

    }

    public void showRequestDenied() {
        PopupWindow popupWindow = IndicationPopups.openXIndication(activityWeakReference.get(), view, activityWeakReference.get().getResources().
                getString(R.string.server_is_down));
        showPopupForOneSecond(popupWindow);
    }

    private void showPopupForOneSecond(PopupWindow popupWindow) {
        if (cTimer == null) {
            cTimer = new CountDownTimer(2500, 500) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    cTimer.cancel();
                    if (popupWindow != null)
                        popupWindow.dismiss();
                    cTimer = null;
                }
            };
            cTimer.start();
        }
    }

    public void showSuccessSignIn() {
        PopupWindow popupWindow = IndicationPopups.openCheckIndication(activityWeakReference.get(), view, activityWeakReference.get().getResources().getString(R.string.cuccessfull_sign_in));
        showPopupForOneSecond(popupWindow);
    }

    public void openPaymentPopup() {
        applyDim();
        RelativeLayout viewGroup = view.findViewById(R.id.new_member_screen);
        LayoutInflater layoutInflater = (LayoutInflater) activityWeakReference.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        paymentSignInView = layoutInflater.inflate(R.layout.new_member_screen, viewGroup);
        placeSignUpOptionsOnScreen(paymentSignInView);
    }

    private void placeSignUpOptionsOnScreen(View paymentSignInView) {
        paymentPopup = new PopupWindow(activityWeakReference.get());
        int width = (int) (view.getWidth() * 0.8);
        int height = (int) (view.getHeight() * 0.8);
        paymentPopup.setContentView(paymentSignInView);
        paymentPopup.setHeight(height);
        paymentPopup.setWidth(width);
        paymentPopup.setTouchable(true);
        paymentPopup.setFocusable(true);
        paymentPopup.setOutsideTouchable(true);
        paymentPopup.showAtLocation(paymentSignInView, Gravity.CENTER, 0, 0);
        paymentPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                view.getOverlay().clear();
            }
        });
//        setSignUpPopupAttributes(activityWeakReference.get(), paymentSignInPopup, view);
//        view.post(() -> paymentSignInPopup.showAtLocation(paymentSignInView, Gravity.CENTER, 0, 0));
    }


    public void closePaymentPopup() {
        paymentPopup.dismiss();
    }

    public void hideMemberSubscription() {
        view.findViewById(R.id.member_subscription).setVisibility(View.GONE);
    }

    public void removeTextFromQuery() {
        ((androidx.appcompat.widget.SearchView) view.findViewById(R.id.search_input)).setQuery("", false);
        view.findViewById(R.id.search_icon_and_words).setVisibility(View.VISIBLE);
    }

    private void applyDim() {
        ViewOverlay overlay = view.getOverlay();
        Drawable colorDim = new ColorDrawable(Color.WHITE);
        colorDim.setBounds(0, 0, view.getWidth(), view.getHeight());
        colorDim.setAlpha((int) (255 * (float) 0.7));
//
        Drawable dim = new BitmapDrawable(activityWeakReference.get().getResources(), BlurBuilder.blur(view));
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.7));
//        ViewOverlay headerOverlay = headerView.getOverlay();
//        headerOverlay.add(dim);
        overlay.add(colorDim);
        overlay.add(dim);
    }

    public void showLoadingIcon() {
        if (paymentSignInView != null) {
            paymentSignInView.findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);
            if (cTimer == null) {
                cTimer = new CountDownTimer(2500, 1500) {
                    @SuppressLint("SetTextI18n")
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        paymentSignInView.findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
                    }
                };
                cTimer.start();
            }
        }
    }

    public void hideLoadingIcon() {
        if (paymentSignInView != null) {
            paymentSignInView.findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
        }
    }
}
