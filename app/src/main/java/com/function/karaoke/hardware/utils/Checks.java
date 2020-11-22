package com.function.karaoke.hardware.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.fragment.app.FragmentManager;

import com.function.karaoke.hardware.DialogBox;
import com.function.karaoke.hardware.SingActivity;

public class Checks {

    private static final int INTERNET_CODE = 102;

    /**
     * Check if this device has a camera
     */
    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean checkForInternetConnection(SingActivity activity, FragmentManager supportFragmentManager, Context applicationContext) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            // If no connectivity, cancel task and update Callback with null data.
            DialogBox dialogBox = DialogBox.newInstance(activity, INTERNET_CODE);
            dialogBox.show(supportFragmentManager, "NoticeDialogFragment");
            return false;
        }
        return true;
    }
}
