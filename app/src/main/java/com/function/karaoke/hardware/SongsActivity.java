package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.DatabaseSongsDB;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.Reocording;
import com.function.karaoke.hardware.activities.Model.SaveItems;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.fragments.SongsListFragment;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.StorageAdder;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.utils.Billing;
import com.function.karaoke.hardware.utils.JsonHandler;
import com.function.karaoke.hardware.utils.static_classes.ShareLink;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SongsActivity
        extends FragmentActivity
        implements SongsListFragment.OnListFragmentInteractionListener {

    private static final int AUDIO_CODE = 101;
    private static final String JSON_DIRECTORY_NAME = "jsonFile";
    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final int SHARING_ERROR = 100;
    private static final String FEEDBACK_EMAIL = "ashira.jewishkaraoke@gmail.com";

    private Billing billingSession;
    public String language;
    Locale myLocale;
    //    private SongsDB mSongs;
    private DatabaseSongsDB dbSongs;
    private AuthenticationDriver authenticationDriver;
    private UserInfo userInfo;
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
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
    private File artistFile;
    private TextView loadingText;
    private CountDownTimer cTimer = null;

    private void updateUI() {
//        findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
//        findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
//        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            double content = intent.getDoubleExtra("content", 0.0);
            loadingText.setText((int) content + "%");
            if (content >= 100) {
                startTimerForThreeSecondsToShow();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForFilesToUpload();
        dbSongs = new DatabaseSongsDB();
        checkForSignedInUser();

//        showPromo();
        language = Locale.getDefault().getLanguage();
//        language = getResources().getConfiguration().getLocales().get(0).getLanguage();
        setContentView(R.layout.activity_songs);
        loadingText = (TextView) (findViewById(R.id.loading_percent));
        billingSession = new Billing(SongsActivity.this, new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            billingSession.handlePurchase(purchase);
                        }
                        // the credit card is taking time
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Toast.makeText(getBaseContext(), "Purchase was cancelled", Toast.LENGTH_SHORT).show();
                    // the user pressed back
                    // Handle an error caused by a user cancelling the purchase flow.
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_TIMEOUT) {
                    Toast.makeText(getBaseContext(), "Service Timed out", Toast.LENGTH_SHORT).show();
                    // if the credit card was cancelled
                    // Handle any other error codes.
                } else {
                    Toast.makeText(getBaseContext(), "Credit card was declined", Toast.LENGTH_SHORT).show();
                    int k = 0;
                }
            }
        }, false, new Billing.ReadyListener() {
            @Override
            public void ready() {
                if (billingSession.isSubscribed()) {
                    File file = renamePendingFiles();
                    if (file != null)

                        checkForFilesToUpload();
                }
            }
        });
        billingSession.subscribeListener(new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                File file = renamePendingFiles();
                if (file != null)
                    checkForFilesToUpload();
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("changed"));
    }

    private File renamePendingFiles() {
        return JsonHandler.renameJsonPendingFile(this.getFilesDir());
    }

    private void checkForFilesToUpload() {
        File folder = new File(this.getFilesDir(), JSON_DIRECTORY_NAME);
        if (folder.exists()) {
            try {
                List<File> listOfAllFiles = Arrays.asList(Objects.requireNonNull(folder.listFiles()));
                for (File child : listOfAllFiles) {
                    if (child.getName().contains("Pending"))
                        continue;
                    if (child.getName().contains("artist")) {
                    } else {
                        SaveItems saveItems = JsonHandler.getDatabaseFromInputStream(getFileInputStream(child));
//                        if (artistFileExists(child, listOfAllFiles)) {
//                            CloudUpload cloudUpload = new CloudUpload(saveItems.getRecording(), this.getFilesDir(), saveItems.getArtist(), new CloudUpload.UploadListener() {
//                                @Override
//                                public void onSuccess(File file) {
//                                    deleteVideo(file);
//                                }
//
//                                @Override
//                                public void onFailure() {
//
//                                }
//
//                                @Override
//                                public void onProgress(int progress) {
//                                    ((TextView) findViewById(R.id.loading_percent)).setText(progress + "%");
//                                }
//                            });
//                            cloudUpload.saveToCloud(new File(saveItems.getFile()));
//
//                        } else {
                        StorageAdder storageAdder = new StorageAdder(new File(saveItems.getFile()));
                        uploadRecording(storageAdder, saveItems, folder);
//                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            deleteSongsFolder();
        }

    }

    private void deleteVideo(File file) {
        file.delete();
    }


    @Override
    public void openAdminSide() {
        Intent intent = new Intent(this, Admin.class);
        startActivity(intent);
    }


    private void deleteSongsFolder() {
        File dir = new File(this.getFilesDir(), DIRECTORY_NAME);
        if (dir.exists()) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                f.delete();
            }
        }
    }

    private void uploadRecording(StorageAdder storageAdder, SaveItems saveItems, File folder) {
        storageAdder.uploadRecording(saveItems.getRecording(), new StorageAdder.UploadListener() {
            @Override
            public void onSuccess() {
                NetworkTasks.uploadToWasabi(storageAdder, new NetworkTasks.UploadToWasabiListener() {
                    @Override
                    public void onSuccess() {
                        storageAdder.updateRecordingUrl(saveItems.getRecording(), new StorageAdder.UploadListener() {
                            @Override
                            public void onSuccess() {
                                File recordingFile = (new File(saveItems.getFile()));
                                recordingFile.delete();
                                deleteJsonFile(folder, recordingFile.getName());
//                        deleteJsonFolder(folder);
//                    parentView.findViewById(R.id.upload_progress_wheel).setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onFailure() {
//                    ((ProgressBar) parentView.findViewById(R.id.upload_progress_wheel)).setBackgroundColor(Color.BLACK);
                            }

                            @Override
                            public void progressUpdate(double progress) {
                                if (findViewById(R.id.loading_percent) != null) {
                                    findViewById(R.id.loading_percent).setVisibility(View.VISIBLE);
                                    ((TextView) (findViewById(R.id.loading_percent))).setText((int) progress + "%");
                                    if (progress == 100.0) {
                                        findViewById(R.id.loading_percent).setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        });
                    }

                    //
                    @Override
                    public void onFail() {
                        int k = 0;
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgress(int percent) {
                        if (findViewById(R.id.loading_percent) != null)
                            loadingText.setText(percent + "%");
                        if (percent >= 100) {
                            startTimerForThreeSecondsToShow();
                        }
                    }
                });
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void progressUpdate(double progress) {

            }
        });


    }

    private void startTimerForThreeSecondsToShow() {
        if (cTimer == null) {
            cTimer = new CountDownTimer(2000, 500) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    cTimer.cancel();
                    loadingText.setText("");
                    cTimer = null;
                }
            };
            cTimer.start();
        }
    }

    private void deleteJsonFile(File folder, String name) {
        (new File(folder, name + ".json")).delete();
        File jsonFileFolder = new File(folder, JSON_DIRECTORY_NAME);
        if (jsonFileFolder.list() == null || Objects.requireNonNull(jsonFileFolder.list()).length == 0)
            jsonFileFolder.delete();
    }

    private InputStream getFileInputStream(File file) throws IOException {
        return new FileInputStream(file);
    }


    private void launchSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("callback", true);
        intent.putExtra("language", Locale.getDefault().getLanguage());
        mGetContent.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //todo check to see if json folder is empty and delete all songs
    }

    private void checkForSignedInUser() {
        authenticationDriver = new AuthenticationDriver();
        if (authenticationDriver.getUserUid() == null) {
            updateUI();
        }
    }

    @Override
    public void alertUserToSignIn() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.sign_in_title);
        alertBuilder.setMessage(R.string.recording_sign_in_text);
        alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> {

        });
        AlertDialog alert = alertBuilder.create();
        alert.show();

    }

    @Override
    public void signOut() {
        authenticationDriver.signOut();
        launchSignIn();

    }

    @Override
    public void onListFragmentInteractionPlay(Reocording item) {
        songClicked = (DatabaseSong) item;
        askForAudioRecordPermission();
    }

    @Override
    public void openEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));
        intent.setType("message/rfc822");
//        intent.setPackage("com.google.android.gm");
        //todo check to see if there is a gmail account
        startActivity(intent);
    }

    private void askForAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle(R.string.mic_access_title);
            alertBuilder.setMessage(R.string.mic_access_text);
            alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) ->
                    ActivityCompat.requestPermissions(SongsActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            AUDIO_CODE));

            AlertDialog alert = alertBuilder.create();
            alert.show();

        } else
            openNewIntent();
    }

    @Override
    public void onListFragmentInteractionPlay(Recording item) {
        Intent intent = new Intent(this, Playback.class);
        intent.putExtra(SingActivity.RECORDING, item);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteractionShare(Recording item) {
        Task<ShortDynamicLink> link = ShareLink.createLink(item);
        link.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                Uri flowchartLink = task.getResult().getPreviewLink();
                String link1 = shortLink.toString();
                sendDataThroughIntent(link1);


            } else {
                showFailure();
                // Error
                // ...
            }
        });

    }

    private void sendDataThroughIntent(String link) {
        String data = getString(R.string.email_prompt) + link;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(
                Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void showFailure() {
        Toast.makeText(this, getString(R.string.sharing_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public DatabaseSongsDB getSongs() {
        return dbSongs;
    }


    @Override
    public void changeLanguage() {
        if (language.equals("en")) {
            setLocale("iw");
        } else {
            setLocale("en");
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
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, SongsActivity.class);
        startActivity(refresh);
    }


    @Override
    protected void onPause() {
        saveCurrentState();
        super.onPause();
    }

    private void saveCurrentState() {

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
        intent.putExtra("language", Locale.getDefault().getLanguage());
        startActivity(intent);
    }

    @Override
    public void colorNextGenre() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        SongsListFragment fragment = (SongsListFragment) fragments.get(0);
        fragment.colorNextGenre();
    }

    @Override
    public void colorPreviousGenre() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        SongsListFragment fragment = (SongsListFragment) fragments.get(0);
        fragment.colorPreviousGenre();
    }
}
