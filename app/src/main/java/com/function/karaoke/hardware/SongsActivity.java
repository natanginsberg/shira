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
import com.function.karaoke.hardware.activities.Model.SaveItems;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.fragments.SongsListFragment;
import com.function.karaoke.hardware.storage.ArtistService;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.StorageAdder;
import com.function.karaoke.hardware.utils.JsonCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

//import com.function.karaoke.hardware.ui.login.LoginActivity;

public class SongsActivity
        extends FragmentActivity
        implements SongsListFragment.OnListFragmentInteractionListener {

    private static final int AUDIO_CODE = 101;
    private static final String JSON_FILE_NAME = "savedJson";
    private static final String JSON_DIRECTORY_NAME = "jsonFile";
    private static final String ARTIST_FILE = "artistUpdated";

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
//        findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
//        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForFilesToUpload();
        dbSongs = new DatabaseSongsDB();
        pack = this.getPackageName();

        showPromo();
        language = getResources().getConfiguration().locale.getDisplayLanguage();
    }

    private void checkForFilesToUpload() {
        File folder = new File(this.getFilesDir(), JSON_DIRECTORY_NAME);
        if (folder.exists()) {
            try {
                boolean artistFileExists = artistFileExists(folder);
                SaveItems saveItems = JsonCreator.getDatabaseFromInputStream(getFileInputStream(folder));
                StorageAdder storageAdder = new StorageAdder(saveItems.getFile());
                if (artistFileExists) {
                    ArtistService artistService = new ArtistService(new ArtistService.ArtistServiceListener() {
                        @Override
                        public void onSuccess() {
                            //todo delete temp artist file
                            storageAdder.uploadRecording(saveItems.getRecording(), new StorageAdder.UploadListener() {
                                @Override
                                public void onSuccess() {
                                    //todo delete all json file
//                    parentView.findViewById(R.id.upload_progress_wheel).setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onFailure() {
//                    ((ProgressBar) parentView.findViewById(R.id.upload_progress_wheel)).setBackgroundColor(Color.BLACK);
                                }
                            });
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                    artistService.addDownloadToArtist(saveItems.getArtist());
                } else {
                    storageAdder.uploadRecording(saveItems.getRecording(), new StorageAdder.UploadListener() {
                        @Override
                        public void onSuccess() {
                            //todo delete all json file
//                    parentView.findViewById(R.id.upload_progress_wheel).setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onFailure() {
//                    ((ProgressBar) parentView.findViewById(R.id.upload_progress_wheel)).setBackgroundColor(Color.BLACK);
                        }
                    });

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private InputStream getFileInputStream(File folder) throws IOException {
        File videoFile = new File(folder, JSON_FILE_NAME + ".json");
        return new FileInputStream(videoFile);
    }

    private boolean artistFileExists(File folder) throws IOException {
        File artistFile = new File(folder, ARTIST_FILE + ".txt");
        return artistFile.exists();
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
//                String languageToDisplay = language.equals("English") ? "EN" : "עב";
//                ((TextView) findViewById(R.id.language)).setText(languageToDisplay);
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
    public void alertUserToSignIn() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.sign_in_title);
        alertBuilder.setMessage(R.string.recording_sign_in_text);
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();

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
