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

public class SettingUI {

    private static final int PERSONAL_RECORDING_DISPLAYED = 100;
    private final View view;
    private final Context context;
    private View popupView;
    private PopupWindow popup;

    public SettingUI(View view, Context c){
        this.view=view;
        this.context=c;
    }

    public void openSettingsPopup(boolean isUserSignedIn, int contentDisplayed) {
        RelativeLayout viewGroup = view.findViewById(R.id.settings_popup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.settings_popup, viewGroup);
        if (contentDisplayed == PERSONAL_RECORDING_DISPLAYED) {
//        if (((TextView) view.findViewById(R.id.display_text)).getText() == context.getResources().getString(R.string.my_recordings)) {
            ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(context.getResources().getColor(R.color.gold));
        } else
//            ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(context.getResources().getColor(R.color.gold));
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

    public void setEmailAddressIfSignedIn(String emailAddressIfSignedIn) {
        ((TextView) popupView.findViewById(R.id.email_address)).setText(emailAddressIfSignedIn);
    }

    private void placePopupOnScreen() {
        popup = new PopupWindow(context);
        popup.setFocusable(true);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                view.getOverlay().clear();

            }
        });
        setPopupAttributes(popup, popupView);
//        int[] location = new int[2];
//        view.findViewById(R.id.settings_button).getLocationOnScreen(location);
        popup.showAtLocation(popupView, Gravity.TOP|Gravity.CENTER, 0, 0);
//                Math.abs(view.getWidth() - location[0]) < Math.abs(location[0]) ? view.getWidth() : 0, 0);
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
        int width = (int) (view.getWidth());
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setHeight((int) (view.getHeight() * 0.8));
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public View getPopupView() {
        return popupView;
    }

}
