package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.UserInfo;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ShareOptionsUI {

    private static final int NUMBER_OF_FREE_SHARES = 3;
    private final View view;
    private View thirdPopupView;
    private PopupWindow thirdPopup;
    private boolean clear = true;
    private final int shares;

    public ShareOptionsUI(View view, UserInfo user) {
        this.view = view;
        this.user = user;
        shares = user.getShares();
    }

    private UserInfo user;
    private SingActivityUI.ShareListener mListener;

    public void openShareOptions(Context context, SingActivityUI.ShareListener shareListener) {
        this.mListener = shareListener;
        clear = true;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.share_options_1);
        thirdPopupView = layoutInflater.inflate(R.layout.share_options_popup_1, viewGroup);
        placeShareOptionsOnScreen(context);
        thirdPopup.setFocusable(true);
        setFreeShares(context);
        setThirdPopupOnClickListeners(context);
    }

    private void setFreeShares(Context context) {
        String textToDisplay = shares >= NUMBER_OF_FREE_SHARES ? "0" : NUMBER_OF_FREE_SHARES - shares + context.getResources().getString(R.string.share_left_label);
        TextView textView = (TextView) thirdPopupView.findViewById(R.id.remaining_free_shares);
        textView.setText(textToDisplay);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void setThirdPopupOnClickListeners(Context context) {
        thirdPopupView.findViewById(R.id.no_send).setOnClickListener(view -> {
            clear = true;
            thirdPopup.dismiss();
        });
        thirdPopupView.findViewById(R.id.open_share).setOnClickListener(view -> {
            clear = false;
            PopupWindow tempPopup = thirdPopup;
//            thirdPopup.dismiss();
            openShareWithoutPassWord(context);
            tempPopup.dismiss();
        });

        thirdPopupView.findViewById(R.id.password_share).setOnClickListener(view -> {
            clear = false;
            PopupWindow tempPopup = thirdPopup;
//            thirdPopup.dismiss();
            openShareWithPassWord(context);
            tempPopup.dismiss();
        });
    }

    private void openShareWithPassWord(Context context) {
        clear = true;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.share_options_3);
        thirdPopupView = layoutInflater.inflate(R.layout.share_options_popup_3, viewGroup);
        setPassword();
        setCommonShareFields(context);
    }

    private void setCommonShareFields(Context context) {
        placeShareOptionsOnScreen(context);
        thirdPopup.setFocusable(true);
        setFreeShares(context);
        setLink();
        setFooterListener(context);
        setCopyListener(context);
        setShareListener();
    }

    private void setShareListener() {
        thirdPopupView.findViewById(R.id.share_button).setOnClickListener(view -> {
            mListener.share(view);
            clear = true;
            thirdPopup.dismiss();
        });
    }

    private void setCopyListener(Context context) {
        thirdPopupView.findViewById(R.id.copy_link).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", mListener.getLink());
            clipboard.setPrimaryClip(clip);
        });
    }

    private void setFooterListener(Context context) {
        thirdPopupView.findViewById(R.id.footer).setOnClickListener(view -> {
            clear = false;
            PopupWindow tempPopup = thirdPopup;
            openShareOptions(context, mListener);
            tempPopup.dismiss();
        });
    }

    private void setLink() {
        mListener.createShareLink(((TextView) thirdPopupView.findViewById(R.id.link)));
    }

    private void setPassword() {
        mListener.setPassword(((TextView) thirdPopupView.findViewById(R.id.password)));
    }

    private void openShareWithoutPassWord(Context context) {
        clear = true;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.share_options_2);
        thirdPopupView = layoutInflater.inflate(R.layout.share_options_popup_2, viewGroup);
        setCommonShareFields(context);
    }

    private void placeShareOptionsOnScreen(Context context) {
        thirdPopup = new PopupWindow(context);
        setSharePopupAttributes(context, thirdPopup, thirdPopupView);
        view.post(() -> thirdPopup.showAtLocation(thirdPopupView, Gravity.CENTER, 0, 0));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setSharePopupAttributes(Context context, PopupWindow popup, View layout) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8);
        int height = (int) (width * 1.6);
        popup.setContentView(layout);
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.unclicked_recording_background));
        popup.setWidth(width);
        popup.setHeight(height);
    }

    public PopupWindow getThirdPopup() {
        return thirdPopup;
    }

    public boolean isClear() {
        return clear;
    }
}
