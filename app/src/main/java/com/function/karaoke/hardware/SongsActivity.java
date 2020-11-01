package com.function.karaoke.hardware;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.DatabaseSongsDB;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.fragments.SongsListFragment;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.ui.login.LoginActivity;

import java.util.Locale;

public class SongsActivity
        extends FragmentActivity
        implements SongsListFragment.OnListFragmentInteractionListener {

    private static final int AUDIO_CODE = 101;
    public String language;
    Locale myLocale;
    //    private SongsDB mSongs;
    private DatabaseSongsDB dbSongs;
    private String pack;
    private AuthenticationDriver authenticationDriver;
    private UserInfo userInfo;
    // GetContent creates an ActivityResultLauncher<String> to allow you to pass
    // in the mime type you'd like to allow the user to select
    private ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent intent = result.getData();
                        userInfo = (UserInfo) intent.getSerializableExtra("User");
                        if (userInfo != null) {
                            updateUI();
                        }
                    }
                }
            });
    private DatabaseSong songClicked;

    private void updateUI() {
//        findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
//        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbSongs = new DatabaseSongsDB();
        pack = this.getPackageName();

        showPromo();
        language = getResources().getConfiguration().locale.getDisplayLanguage();
    }


    private void addSignInClick() {
        findViewById(R.id.sign_in_button).setOnClickListener(view -> launchSignIn());
    }

    private void launchSignIn() {
        mGetContent.launch(new Intent(this, SignInActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showPromo() {
        setContentView(R.layout.promo);
        //todo set for when the app loads from the server
        setTimer();
    }

    private void setTimer() {
        new CountDownTimer(5000, 1) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                setContentView(R.layout.activity_songs);
                addSignInClick();
//                mTextureView = (TextureView) findViewById(R.id.camera_place);
//                cameraPreview = new CameraPreview(mTextureView, SongsActivity.this);
                String languageToDisplay = language.equals("English") ? "EN" : "עב";
                ((TextView) findViewById(R.id.language)).setText(languageToDisplay);
                checkForSignedInUser();
//                setFontCorrectly();
            }
        }.start();
    }

    private void checkForSignedInUser() {
        authenticationDriver = new AuthenticationDriver();
        if (authenticationDriver.getUserUid() != null) {
            updateUI();
        }
    }

    @Override
    public void onListFragmentInteraction(DatabaseSong item) {
        songClicked = item;
        askForAudioRecordPermission();
    }

    private void askForAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle(R.string.mic_access_title);
            alertBuilder.setMessage(R.string.mic_access_text);
            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(SongsActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            AUDIO_CODE);
                }
            });

            AlertDialog alert = alertBuilder.create();
            alert.show();

        } else
            openNewIntent();
    }

    @Override
    public void onListFragmentInteraction(Recording item) {
        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(SingActivity.RECORDING, item);
        startActivity(intent);
    }

    @Override
    public DatabaseSongsDB getSongs() {
        return dbSongs;
    }

    public void openLogInActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signOut(View view) {

        findViewById(R.id.sign_out_button).setVisibility(View.INVISIBLE);
//        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.personal_library).setVisibility(View.INVISIBLE);
        launchSignIn();

    }

    @Override
    public void changeLanguage() {
        if (!language.equals("English")) {
            setLocale("en");
        } else {
            setLocale("iw");
        }
    }

    @Override
    public void openSignUp() {
        launchSignIn();
    }


    /**
     * creates a dynamic link
     */
    public void uploadPdfFile(View view) {
//        StorageDriver storageDriver = new StorageDriver();
//        NetworkTasks.uploadToGoogleDrive(storageDriver, new NetworkTasks.UploadToGoogleDriveListener() {
//            @Override
//            public void onSuccess() {
//                int k = 0;
//            }
//
//            @Override
//            public void onFail() {
//                int k = 0;
//            }
//        });

    }


    private void setLocale(String lang) {

        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, SongsActivity.class);
        startActivity(refresh);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                openNewIntent();
        }
    }

    private void openNewIntent() {
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, songClicked);
        startActivity(intent);
    }
}
