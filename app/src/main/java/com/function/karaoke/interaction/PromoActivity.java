package com.function.karaoke.interaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.interaction.activities.Model.SignInViewModel;
import com.function.karaoke.interaction.storage.AuthenticationDriver;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.ui.ShowPrivacyPolicy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.io.File;
import java.util.Locale;

public class PromoActivity extends AppCompatActivity {


    private static final String VERSION_WORD = "pricing";
    private AuthenticationDriver authenticationDriver;
    private SignInViewModel signInViewModel;
    private boolean appStarted = false;
    private boolean websiteOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        showPromo();
    }

    private boolean policyFileExists() {
        File file = new File(this.getCacheDir(), "policy1.txt");
        return file.exists();
    }

    private void showPolicy() {
        ShowPrivacyPolicy.showPolicy(this, findViewById(R.id.promo), new ShowPrivacyPolicy.AgreeListener() {
            @Override
            public void agreed(Context context) {
                continueAsGuest();
            }
        });
    }

    @Override
    public void onResume() {
        if (websiteOpened) {
            websiteOpened = false;
            setTimer();
        } else if (appStarted)
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
        findViewById(R.id.acum_sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                websiteOpened = true;
                openAcumWebsite();
            }
        });
        authenticationDriver = new AuthenticationDriver();
        DatabaseDriver databaseDriver = new DatabaseDriver();
        databaseDriver.getVersionWord(new DatabaseDriver.VersionListener() {
            @Override
            public void onSuccess(String word) {
                if (word.equals(VERSION_WORD))
                    setTimer();
                else {
                    badVersionNotification();
                }
            }
        });
    }

    private void badVersionNotification() {
        findViewById(R.id.logo).setVisibility(View.INVISIBLE);
        findViewById(R.id.bad_version).setVisibility(View.VISIBLE);
    }

    private void openAcumWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://acum.org.il/"));
        startActivity(browserIntent);
    }

    private void setTimer() {
        new CountDownTimer(1000, 500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (!websiteOpened) {
                    if (policyFileExists()) continueAsGuest();
                    else showPolicy();
                }
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