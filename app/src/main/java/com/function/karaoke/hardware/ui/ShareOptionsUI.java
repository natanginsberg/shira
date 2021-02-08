package com.function.karaoke.hardware.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.activities.Model.UserInfo;

import org.w3c.dom.Text;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ShareOptionsUI {

    private static final int NUMBER_OF_FREE_SHARES = 3;
    private final View view;
    private View thirdPopupView;
    private PopupWindow thirdPopup;
    private boolean clear = true;
    private boolean video = true;

    public ShareOptionsUI(View view, UserInfo user) {
        this.view = view;
        this.user = user;
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
        TextView textView = (TextView) thirdPopupView.findViewById(R.id.remaining_free_shares);
        if (user.getFreeShares() > 0) {
            String textToDisplay = context.getResources().getString(R.string.share_left_label, user.getFreeShares());
            textView.setText(textToDisplay);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else
            textView.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void setThirdPopupOnClickListeners(Context context) {
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
            openShareWithPassword(context);
            tempPopup.dismiss();
        });

        thirdPopupView.findViewById(R.id.no_video_send).setOnClickListener(view -> {
            if (video) {
                video = false;
                TextView headerText = (TextView) thirdPopupView.findViewById(R.id.header);
                headerText.setText(context.getResources().getString(R.string.share_save) + context.getResources().getString(R.string.no_video_share));
                headerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
                ((TextView) thirdPopupView.findViewById(R.id.no_video_send)).setText(context.getResources().getString(R.string.with_video_share));
            } else {
                video = true;
                ((TextView) thirdPopupView.findViewById(R.id.header)).setText(context.getResources().getString(R.string.share_save));
                ((TextView) thirdPopupView.findViewById(R.id.no_video_send)).setText(context.getResources().getString(R.string.send_without_video));
            }
        });

    }

    private void openShareWithPassword(Context context) {
        clear = true;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.share_options_3_small);
        thirdPopupView = layoutInflater.inflate(R.layout.share_options_popup_3_smaller, viewGroup);
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
        if (!video)
            setNoVideoShare(context);
    }

    @SuppressLint("SetTextI18n")
    private void setNoVideoShare(Context context) {
        ((TextView) thirdPopupView.findViewById(R.id.header)).setText(context.getResources().getString(R.string.share_save) + context.getResources().getString(R.string.no_video_share));
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
            ClipData clip = ClipData.newPlainText("Url and password",
                    context.getResources().getString(R.string.link) + " " +
                            mListener.getLink() + " " +
                            context.getResources().getString(R.string.password) + " " +
                            mListener.getPassword());
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
        mListener.createShareLink(((TextView) thirdPopupView.findViewById(R.id.link)), video);
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
