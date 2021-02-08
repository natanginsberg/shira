package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.function.karaoke.core.utility.BlurBuilder;
import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class SettingUI {

    private final View view;
    private final Context context;
    private View popupView;
    private PopupWindow popup;
    private ImageView profilePic;
    private final ClosePopListener closePopListener = new ClosePopListener();

    public SettingUI(View view, Context c) {
        this.view = view;
        this.context = c;
    }

    public void openSettingsPopup(boolean isUserSignedIn, UserInfo user) {
        RelativeLayout viewGroup = view.findViewById(R.id.settings_popup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.settings_popup, viewGroup);
        profilePic = popupView.findViewById(R.id.user_picture);
        if (isUserSignedIn)
            showSignedInTheme(user);
        else
            showNotSignedInTheme();
//        if (contentDisplayed == PERSONAL_RECORDING_DISPLAYED) {
////        if (((TextView) view.findViewById(R.id.display_text)).getText() == context.getResources().getString(R.string.my_recordings)) {
//            ((TextView) popupView.findViewById(R.id.my_recordings)).setTextColor(context.getResources().getColor(R.color.gold));
//        } else
//            ((TextView) popupView.findViewById(R.id.home_button)).setTextColor(context.getResources().getColor(R.color.gold));


        placePopupOnScreen();
        applyDim();
        addCloseListeners();

    }

    private void showNotSignedInTheme() {
        setGmailPic();
        showSignInText();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        popupView.findViewById(R.id.user_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        popupView.findViewById(R.id.sign_in_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void showSignInText() {
        popupView.findViewById(R.id.sign_in_invite).setVisibility(View.VISIBLE);
        popupView.findViewById(R.id.email_address).setVisibility(View.INVISIBLE);
    }

    private void showSignedInTheme(UserInfo user) {
        setProfilePic(user != null ? user.getPicUrl() : " ");
        setSignOutButton();
        setEmailAddressIfSignedIn(user != null ? user.getUserEmail() : "");
        popupView.findViewById(R.id.user_picture).setOnClickListener(null);
    }


    private void addCloseListeners() {
        popupView.findViewById(R.id.close_button_holder).setOnClickListener(closePopListener);
        popupView.findViewById(R.id.settings_button).setOnClickListener(closePopListener);
        popupView.findViewById(R.id.close_text).setOnClickListener(closePopListener);
        popupView.findViewById(R.id.close_x).setOnClickListener(closePopListener);
        popupView.findViewById(R.id.menu_words).setOnClickListener(closePopListener);

    }

    private void setSignOutButton() {
        ((TextView) popupView.findViewById(R.id.sign_out_button)).setVisibility(View.VISIBLE);
        popupView.findViewById(R.id.sign_out_line).setVisibility(View.VISIBLE);
    }

    public void setEmailAddressIfSignedIn(String emailAddressIfSignedIn) {
        popupView.findViewById(R.id.sign_in_invite).setVisibility(View.GONE);
        ((TextView) popupView.findViewById(R.id.email_address)).setText(emailAddressIfSignedIn);
    }

    private void placePopupOnScreen() {
        popup = new PopupWindow(context);
        popup.setFocusable(true);
        popup.setOnDismissListener(() -> view.getOverlay().clear());
        setPopupAttributes(popup, popupView);
        popup.showAtLocation(popupView, Gravity.TOP | Gravity.CENTER, 0, 0);
    }

    private void applyDim() {
        ViewOverlay overlay = view.getOverlay();
        Drawable colorDim = new ColorDrawable(Color.WHITE);
        colorDim.setBounds(0, 0, view.getWidth(), view.getHeight());
        colorDim.setAlpha((int) (255 * (float) 0.7));
//
        Drawable dim = new BitmapDrawable(context.getResources(), BlurBuilder.blur(view));
        dim.setBounds(0, 0, view.getWidth(), view.getHeight());
        dim.setAlpha((int) (255 * (float) 0.7));
//        ViewOverlay headerOverlay = headerView.getOverlay();
//        headerOverlay.add(dim);
        overlay.add(colorDim);
        overlay.add(dim);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setPopupAttributes(PopupWindow popup, View layout) {
        int width = (int) (view.getWidth());
        popup.setContentView(layout);
        popup.setWidth(width);
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.unclicked_recording_background));
        popup.setHeight((int) (view.getHeight() * 0.8));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setProfilePic(String picUrl) {
        if (!(picUrl == null || picUrl.equalsIgnoreCase(" ")))
            Picasso.get()
                    .load(picUrl)
                    .placeholder(context.getResources().getDrawable(R.mipmap.ic_gmail_open))
                    .fit()
                    .transform(new CropCircleTransformation())
                    .into(profilePic);

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setGmailPic() {
        Picasso.get()
                .load(String.valueOf(context.getResources().getDrawable(R.mipmap.ic_gmail_open)))
                .placeholder(context.getResources().getDrawable(R.mipmap.ic_gmail_open))
                .fit()
                .transform(new CropCircleTransformation())
                .into(profilePic);
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public View getPopupView() {
        return popupView;
    }

    private class ClosePopListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            popup.dismiss();
        }
    }


}
