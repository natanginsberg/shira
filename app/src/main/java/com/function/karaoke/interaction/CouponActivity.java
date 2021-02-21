package com.function.karaoke.interaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.interaction.activities.Model.UserInfo;
import com.function.karaoke.interaction.storage.AuthenticationDriver;
import com.function.karaoke.interaction.storage.CouponService;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.storage.UserService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.function.karaoke.interaction.ui.IndicationPopups.openCheckIndication;
import static com.function.karaoke.interaction.ui.IndicationPopups.openXIndication;

public class CouponActivity extends AppCompatActivity {


    private static final String USER_INFO = "User";

    private static final int NOT_EXIST = -1;
    private static final int MONTHLY = 1;
    private static final int YEARLY = 2;
    private static final int FREE_SHARES = 3;


    private UserInfo user;
    private Calendar calendar;
    private CountDownTimer cTimer;
    private boolean clickAllowed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        Date today = new Date();
        calendar = Calendar.getInstance();
        calendar.setTime(today);
        getUser();
    }

    private void getUser() {
        if (getIntent().getExtras().containsKey(USER_INFO)) {
            user = (UserInfo) getIntent().getSerializableExtra(USER_INFO);
        }
    }

    public void sendCode(View view) {
        if (!clickAllowed)
            return;
        CouponService couponService = new CouponService(new DatabaseDriver());
        String code = (String) ((EditText) findViewById(R.id.code)).getText().toString();
        UserService userService = new UserService(new DatabaseDriver(), new AuthenticationDriver());
        if (code.length() > 6)
            couponService.validateCoupon(new CouponService.GetCouponType() {
                @Override
                public void type(int type) {
                    switch (type) {
                        case NOT_EXIST:
                            invalidCodeIndication();
                            break;
                        case MONTHLY:
                            calendar.add(Calendar.MONTH, 1);
                            String expirationDate = String.valueOf(new SimpleDateFormat("yyyyMMdd_HHmmss",
                                    Locale.getDefault()).format(calendar.getTime().getTime()));
                            userService.changeExpirationDate(expirationDate);
                            user.setExpirationDate(expirationDate);
                            showSuccessPopup(getResources().getString(R.string.month_coupon));
                            break;
                        case YEARLY:
                            calendar.add(Calendar.YEAR, 1);
                            expirationDate = String.valueOf(new SimpleDateFormat("yyyyMMdd_HHmmss",
                                    Locale.getDefault()).format(calendar.getTime().getTime()));
                            userService.changeExpirationDate(expirationDate);
                            user.setExpirationDate(expirationDate);
                            showSuccessPopup(getResources().getString(R.string.yearly_coupon));
                            break;
                    }
                }

                @Override
                public void freeShares(int type, int freeShares) {
                    userService.changeFreeShares(freeShares);
                    user.addFreeShares(freeShares);
                    showSuccessPopup(getResources().getString(R.string.free_shares_awarded, freeShares));
                }
            }, code, user.getUserEmail());
        else
            invalidCodeIndication();
    }

    private void showSuccessPopup(String string) {
        PopupWindow popupWindow = openCheckIndication(this, findViewById(android.R.id.content).getRootView(), string);
        showPopupForOneSecond(popupWindow, true);
    }

    private void invalidCodeIndication() {
        PopupWindow popupWindow = openXIndication(this, findViewById(android.R.id.content).getRootView(), getResources().getString(R.string.incorrect_coupon));
        showPopupForOneSecond(popupWindow, false);

    }

    private void showPopupForOneSecond(PopupWindow popupWindow, boolean closeWindow) {
        clickAllowed = false;
        if (cTimer == null) {
            cTimer = new CountDownTimer(1500, 500) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    cTimer.cancel();
                    popupWindow.dismiss();
                    cTimer = null;
                    clickAllowed = true;
                    if (closeWindow)
                        putUserInIntentAndFinish();
                }
            };
            cTimer.start();
        }
    }

    private void putUserInIntentAndFinish() {
        Intent intent = new Intent(this, SongsActivity.class);
        intent.putExtra(USER_INFO, user);
        setResult(RESULT_OK, intent);
        finish();
    }
}