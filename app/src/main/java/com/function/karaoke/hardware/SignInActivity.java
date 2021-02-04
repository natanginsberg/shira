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
import androidx.lifecycle.Observer;

import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.Locale;

public class SignInActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 101;
    private static final String RESULT_CODE = "code";
    private static final String SING_ACTIVITY = "sing activity";
    private static final String CALLBACK = "callback";
    private GoogleSignInClient mGoogleSignInClient;
    private AuthenticationDriver authenticationDriver;
    private UserInfo user;
    private Observer<Boolean> gettingNewUserSucceeded;
    private SignInViewModel signInViewModel;
    private int code = -1;
    private boolean callback;
    private Locale myLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        showPromo();
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

    public void continueAsGuest() {
        if (authenticationDriver.isSignIn()) {
            openMain();
        } else
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

    private void openMain() {
        Intent intent = new Intent(this, SongsActivity.class);
        startActivity(intent);
    }

    private void makeToastForError() {
        Toast.makeText(this, getString(R.string.error_from_the_beginning_of_sign_in), Toast.LENGTH_LONG).show();
    }

}