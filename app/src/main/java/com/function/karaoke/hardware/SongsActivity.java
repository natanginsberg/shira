package com.function.karaoke.hardware;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.fragments.NetworkFragment;
import com.function.karaoke.hardware.fragments.SongsListFragment;
import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.ui.login.LoginActivity;
import com.function.karaoke.hardware.utils.MergeTake2;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SongsActivity
        extends FragmentActivity
        implements SongsListFragment.OnListFragmentInteractionListener, DownloadCallback {

    private static final int VIDEO_REQUEST = 1;
    private static final int CAMERA_CODE = 2;
    private static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 102;
    private static final int PICK_CONTACT_REQUEST = 100;

    DatabaseDriver databaseDriver = new DatabaseDriver();

    //    private SongsDB mSongs;
    private DatabaseSongsDB dbSongs;
    public String language;
    public String temporaryLanguage;
    Locale myLocale;
    private NetworkFragment networkFragment;
    private boolean downloading = false;
    private int INTERNET_CODE = 104;
    private long[] sizes = new long[2];
    List<String> urls = new ArrayList<>();
    private int prepared = 0;

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


    private void updateUI() {
        findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
    }


    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mSongs = new SongsDB(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        dbSongs = new DatabaseSongsDB();

        showPromo();
        language = getResources().getConfiguration().locale.getDisplayLanguage();
//        networkFragment = NetworkFragment.getUploadInstance(getSupportFragmentManager(), new StorageDriver());
//        setContentView(R.layout.activity_songs);
        getDynamicLink();

    }

    private void getDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
//                            findViewById(R.id.language).setVisibility(View.INVISIBLE);
                        }
                        else {
//                            findViewById(R.id.personal_library).setVisibility(View.VISIBLE);
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    private static final String TAG = "Error";

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
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
//                mTextureView = (TextureView) findViewById(R.id.camera_place);
//                cameraPreview = new CameraPreview(mTextureView, SongsActivity.this);
                String languageToDisplay = language.equals("English") ? "EN" : "עב";
                ((TextView) findViewById(R.id.language)).setText(languageToDisplay);
            }
        }.start();
    }

    @Override
    public void onListFragmentInteraction(DatabaseSong item) {
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, item);
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

    public void changeLanguage(View view) {
        if (language.equals("Hebrew")) {
            setLocale("en");
        } else {
            setLocale("iw");
        }
    }

    public void uploadPdfFile(View view) {
//        mGetContent.launch(new Intent(this, SignInActivity.class));
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
//                .setLink(Uri.parse("https://www.example.com/"))
//                .setDomainUriPrefix("https://singJewish.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
//                .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();

        String link = dynamicLinkUri.toString();


        String link_val = "http/example://ashira" + this.getPackageName();
        String body = "<a href=\"" + link + "\">" + link + "</a>";
        String data = " Hello I am using this App.\nIt has large numbers of Words meaning with synonyms.\nIts works offline very smoothly.\n\n" + body;
//        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
//        sendIntent.setAction(ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
//        sendIntent.setPackage("com.whatsapp");
//        sendIntent.setData(Uri.parse("smsto:"));
//        sendIntent.setType("text/html");
//        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, data);
//        sendIntent.putExtra(Intent.EXTRA_HTML_TEXT, body);
        sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                Html.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY));
//        emailIntent.setType("image/jpeg");
        //File bitmapFile = new File(Environment.getExternalStorageDirectory()+"DCIM/Camera/img.jpg");
        //myUri = Uri.fromFile(bitmapFile);
//        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///mnt/sdcard/DCIM/Camera/img.jpg"));
//        sendIntent.setDataAndType(Uri.parse("mailto:"), "text/html");
//        if (sendIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(sendIntent);
//        }

//        startActivityForResult(Intent.createChooser(sendIntent, "Complete action using:"), PICK_CONTACT_REQUEST);
        startActivity(sendIntent);
    }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
//            requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET_CODE);
//        else {
//            networkFragment = NetworkFragment.getUploadInstance(getSupportFragmentManager(), new StorageDriver());
//            networkFragment.startUpload();
//        }


//        Intent refresh = new Intent(this, SignInActivity.class);
//        startActivity(refresh);


//    }

    private void prepareBothAudios() {
        List<MediaPlayer> mediaPlayers = new ArrayList<>();
        mediaPlayers.add(new MediaPlayer());
        mediaPlayers.add(new MediaPlayer());
//        for (MediaPlayer mPlayer:mediaPlayers)
        for (int i = 0; i < 2; i++) {
            String url = dbSongs.getSongs().get(i).getSongResourceFile();
            MediaPlayer mPlayer = mediaPlayers.get(i);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build());
                } else {
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }
                mPlayer.setDataSource(url);
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        prepared++;
                        if (prepared == 2) {
                            for (MediaPlayer mPlayer : mediaPlayers) {
                                mPlayer.start();
                            }
                        }
                    }
                });
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAudioSizes(String url, int i) {
        final Observer<Long> searchObserver = size -> {
            if (sizes[i] == 0) {
                sizes[i] = (size - 44) / 2;
                prepared++;
            }
            if (prepared == 2) {

                MergeTake2 mergeAudioFiles = new MergeTake2(urls, sizes);
                networkFragment = NetworkFragment.getMergerInstance(getSupportFragmentManager(), mergeAudioFiles);
                networkFragment.getLifecycle().addObserver(new CreateObserver());

//                MergeTake2 mergeAudioFiles = new MergeTake2(urls, sizes);
//                mergeAudioFiles.SSoftAudCombine();
            }
        };
        if (i == 0)
            databaseDriver.getFirstStorageReferenceSize(url).observe(this, searchObserver);
        else
            databaseDriver.getSecondStorageReferenceSize(url).observe(this, searchObserver);
    }

    private class CreateObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void connectListener() {
            networkFragment.startMerge();
        }
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

    @Override
    public void updateFromDownload(Object result) {
        temporaryLanguage = (String) result;
        findViewById(R.id.language).setVisibility(View.INVISIBLE);
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch (progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:

                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void finishDownloading() {
        downloading = false;
        if (networkFragment != null) {
            networkFragment.cancelDownload();
        }
    }

    class SignUpContract extends ActivityResultContract<Integer, UserInfo> {


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
