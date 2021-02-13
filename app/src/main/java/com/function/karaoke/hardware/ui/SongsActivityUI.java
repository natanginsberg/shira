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
import com.function.karaoke.hardware.activities.Model.Genres;

public class SongsActivityUI {

    private final View view;
    private final Context context;
    private final GenresUI genreUI;
    private PopupWindow suggestPopup;
    private View suggestionView;
    private CountDownTimer cTimer;

    public SongsActivityUI(View songsActivity, GenresUI.GenreUIListener listener, String currentLanguage, Context context) {
        this.view = songsActivity;
        this.context = context;
        genreUI = new GenresUI(songsActivity, context, currentLanguage, listener);
    }

    public void addGenresToScreen(Genres genres, String currentGenre, int displayed) {
        genreUI.addGenresToScreen(genres, currentGenre, displayed);
    }

    public View openSongSuggestionsPopup() {
        RelativeLayout viewGroup = view.findViewById(R.id.song_suggestion);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        suggestionView = layoutInflater.inflate(R.layout.song_suggestion, viewGroup);
        placeSuggestionOnScreen(suggestionView);
        return suggestionView;
    }


    private void placeSuggestionOnScreen(View suggestionView) {
        suggestPopup = new PopupWindow(context);
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
        PopupWindow popupWindow = IndicationPopups.openCheckIndication(context, view, context.getResources().
                getString(R.string.request_received));
        showPopupForOneSecond(popupWindow);

    }

    public void showRequestDenied() {
        PopupWindow popupWindow = IndicationPopups.openXIndication(context, view, context.getResources().
                getString(R.string.server_is_down));
        showPopupForOneSecond(popupWindow);
    }

    private void showPopupForOneSecond(PopupWindow popupWindow) {
        if (cTimer == null) {
            cTimer = new CountDownTimer(1500, 500) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    cTimer.cancel();
                    popupWindow.dismiss();
                    cTimer = null;
                }
            };
            cTimer.start();
        }
    }

    public void showSuccessSignIn() {
        PopupWindow popupWindow = IndicationPopups.openCheckIndication(context, view, context.getResources().getString(R.string.cuccessfull_sign_in));
        showPopupForOneSecond(popupWindow);
    }
}
