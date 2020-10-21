package com.function.karaoke.hardware;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentActivity;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.DatabaseSongsDB;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.fragments.SongsListFragment;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.StorageAdder;
import com.function.karaoke.hardware.storage.StorageDriver;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.ui.login.LoginActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

public class SongsActivity
        extends FragmentActivity
        implements SongsListFragment.OnListFragmentInteractionListener {

    private static final int VIDEO_REQUEST = 1;
    private static final int CAMERA_CODE = 2;
    private static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 102;
    private static final int PICK_CONTACT_REQUEST = 100;

    DatabaseDriver databaseDriver = new DatabaseDriver();

    //    private SongsDB mSongs;
    private DatabaseSongsDB dbSongs;
    public String language;
    Locale myLocale;
    private String pack;

    // GetContent creates an ActivityResultLauncher<String> to allow you to pass
// in the mime type you'd like to allow the user to select
    private ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent intent = result.getData();
                        userInfo = (UserInfo) intent.getSerializableExtra("User");
                        updateUI();
                    }
                }
            });
    private AuthenticationDriver authenticationDriver;


    private void updateUI() {
        findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }


    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mSongs = new SongsDB(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
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
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, item);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction(Recording item) {
        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(SingActivity.RECORDING, item);
        startActivity(intent);
    }

    //    @Override
//    public SongsDB getSongs() {
//        return mSongs;
//    }
    @Override
    public DatabaseSongsDB getSongs() {
        return dbSongs;
    }

    public void openLogInActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signOut(View view) {
//        authenticationDriver = new AuthenticationDriver();
//        authenticationDriver.signOut();


        findViewById(R.id.sign_out_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.personal_library).setVisibility(View.INVISIBLE);
        launchSignIn();

    }

    public void changeLanguage(View view) {
        if (language.equals("Hebrew")) {
            setLocale("en");
        } else {
            setLocale("iw");
        }
    }


    /**
     * creates a dynamic link
     */
    public void uploadPdfFile(View view) {
        StorageDriver storageDriver =  new StorageDriver();
        NetworkTasks.uploadToGoogleDrive(storageDriver, new NetworkTasks.UploadToGoogleDriveListener() {
            @Override
            public void onSuccess() {
                int k =0;
            }

            @Override
            public void onFail() {
int k=0;
            }
        });

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
//        cameraPreview.closeCamera();
//        cameraPreview.stopBackgroundThread();
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_CODE:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
//                    cameraPreview.connectCamera();
                    break;
            case EXTERNAL_STORAGE_WRITE_PERMISSION:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                }
                break;
        }
    }


    static class SignUpContract extends ActivityResultContract<Integer, UserInfo> {


        private static final int ONE_TIME_SUBSCRIPTION = 100;
        private static final int MONTHLY_SUBSCRIPTION = 101;
        private static final int YEARLY_SUBSCRIPTION = 102;

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Integer input) {
            return null;
        }

        @Override
        public UserInfo parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode != RESULT_OK || intent == null) {
                return null;
            }
            return (UserInfo) intent.getSerializableExtra("User");
        }
    }
}
