package com.function.karaoke.interaction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class PolicyActivity extends AppCompatActivity {

    private static final String MENU = "open menu";
    private static final String PRIVACY_POLICY = "privacy policy";
    private static final String TERMS_OF_USE = "terms of use";
    private final StringBuilder text = new StringBuilder();
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        setPolicy(getIntent().getExtras().containsKey(PRIVACY_POLICY) ? PRIVACY_POLICY : TERMS_OF_USE);
        setSettingsListener();
    }

    private void setSettingsListener() {
        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu();
            }
        });
    }

    private void openMenu() {
        Intent intent = new Intent(this, SongsActivity.class);
        intent.putExtra(MENU, true);
        finish();
    }

    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        if (prefs != null) {
            String language = prefs.getString(langPref, "");
            if (language != null && !language.equalsIgnoreCase("")) {
                this.language = language;
            }
        }
        if (language == null)
            language = Locale.getDefault().getLanguage();
        setLocale(language);
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
    }

    private void setPolicy(String policy) {
        ((TextView) findViewById(R.id.menu_words)).setText(policy.equals(PRIVACY_POLICY) ? getString(R.string.privacy_policy) : getString(R.string.ashira_terms_of_use));

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(policy.equals(TERMS_OF_USE) ? "Contract" : "PrivacyPolicy")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
            text.append('\n');
            text.append('\n');
//            reader = new BufferedReader(
//                    new InputStreamReader(getAssets().open("PrivacyPolicy")));
//            while ((mLine = reader.readLine()) != null) {
//                text.append(mLine);
//                text.append('\n');
//            }
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error reading file!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            //log the exception

            TextView output = (TextView) findViewById(R.id.policy_words);
            output.setText((CharSequence) text);

        }
    }

}