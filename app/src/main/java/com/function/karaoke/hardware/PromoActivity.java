package com.function.karaoke.hardware;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.Locale;

public class PromoActivity extends AppCompatActivity {


    private AuthenticationDriver authenticationDriver;
    private SignInViewModel signInViewModel;
    private final int code = -1;
    private Locale myLocale;
    private boolean appStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        showPromo();
    }

    @Override
    public void onResume() {
        if (appStarted)
            finish();
        else
            appStarted = true;
        super.onResume();
    }


    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        if (prefs != null) {
            String language = prefs.getString(langPref, "");
            if (language != null && !language.equalsIgnoreCase("")) {
                setLocale(language);
            }
        }
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
    }

    private void showPromo() {
        setContentView(R.layout.promo);
        authenticationDriver = new AuthenticationDriver();
        setTimer();
    }

    private void setTimer() {
        new CountDownTimer(500, 500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                continueAsGuest();
            }
        }.start();
    }

    private void continueAsGuest() {
        if (authenticationDriver.isSignIn()) {
            openMain();
        } else {
            signInViewModel = new SignInViewModel();
            signInViewModel.createGuestId(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        openMain();
                    } else {
                        makeToastForError();
                    }
                }
            });
        }
    }

    private void openMain() {
        Intent intent = new Intent(this, SongsActivity.class);
        startActivity(intent);
    }

    private void makeToastForError() {
        Toast.makeText(this, getString(R.string.error_from_the_beginning_of_sign_in), Toast.LENGTH_LONG).show();
    }

}