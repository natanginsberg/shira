package com.function.karaoke.hardware.ui;

import android.content.Context;
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

import com.function.karaoke.hardware.R;

public class SongsActivityUI {

    private static final int ALL_SONGS_DISPLAYED = 1;
    private static final int PERSONAL_RECORDING_DISPLAYED = 2;

    private final View view;
    private View popupView;
    private PopupWindow popup;
    private Context context;

    public SongsActivityUI(View songsActivity) {
        this.view = songsActivity;
    }

    public void openSettingsPopup(Context context, boolean isUserSignedIn, int contentsDisplayed) {
        this.context = context;
        RelativeLayout viewGroup = view.findViewById(R.id.settings_popup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.settings_popup, viewGroup);
//        addPopupListeners();
        //todo set the button that is pressed either the recordings or home
        if (contentsDisplayed == PERSONAL_RECORDING_DISPLAYED) {
            ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(context.getResources().getColor(R.color.gold, context.getTheme()));
        } else
            ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(context.getResources().getColor(R.color.gold, context.getTheme()));
        setSignInOrOut(isUserSignedIn);

        placePopupOnScreen();
        applyDim();

    }

    private void setSignInOrOut(boolean isSignedIn) {
        TextView signInOrOutButton = ((TextView) popupView.findViewById(R.id.sign_in_button));
        if (isSignedIn)
            signInOrOutButton.setText(context.getResources().getText(R.string.sign_out));
        else
            signInOrOutButton.setText(context.getResources().getText(R.string.sign_in));
    }

    private void placePopupOnScreen() {
        popup = new PopupWindow(context);
        popup.setFocusable(true);
        setPopupAttributes(popup, popupView);
        int[] location = new int[2];
        view.findViewById(R.id.settings_button).getLocationOnScreen(location);
        popup.showAtLocation(popupView, Gravity.START,
                Math.abs(view.getWidth() - location[0]) < Math.abs(location[0]) ? view.getWidth() : 0, 0);
    }

    private void applyDim() {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.5));
        ViewOverlay overlay = view.getOverlay();
//        ViewOverlay headerOverlay = headerView.getOverlay();
//        headerOverlay.add(dim);
        overlay.add(dim);
    }

    private void setPopupAttributes(PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.719);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels);
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public View getPopupView() {
        return popupView;
    }

    public void changeTextForSignInButton(CharSequence text) {
        ((TextView) popupView.findViewById(R.id.sign_in_button)).setText(text);
    }

    public void openSearchBar(boolean isSearchOpen) {
        if (isSearchOpen)
            view.findViewById(R.id.search_input).setVisibility(View.GONE);

        else
            view.findViewById(R.id.search_input).setVisibility(View.VISIBLE);

    }
}
