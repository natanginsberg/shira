package com.function.karaoke.hardware.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.PopupWindow;

import com.function.karaoke.hardware.R;
import com.function.karaoke.hardware.ui.IndicationPopups;

public class Checks {

    private static CountDownTimer cTimer;

    /**
     * Check if this device has a camera
     */
    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean checkForInternetConnection(View view, Context applicationContext) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            // If no connectivity, cancel task and update Callback with null data.
//            DialogBox dialogBox = DialogBox.newInstance(activity, INTERNET_CODE);
//            dialogBox.show(supportFragmentManager, "NoticeDialogFragment");
            PopupWindow popupWindow = IndicationPopups.openXIndication(applicationContext, view, applicationContext.getResources().getString(R.string.no_internet_alert));
            showPopupForOneSecond(popupWindow);
            return false;
        }
        return true;
    }

    private static void showPopupForOneSecond(PopupWindow popupWindow) {
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
}
