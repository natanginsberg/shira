package com.function.karaoke.hardware;

import android.Manifest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import com.function.karaoke.hardware.fragments.NetworkFragment;
import com.function.karaoke.hardware.fragments.SongsListFragment;
import com.function.karaoke.hardware.ui.login.LoginActivity;
import com.function.karaoke.hardware.utils.MergeTake2;

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

    private static final int DOWNLOAD_WORDS = 100;
    private static final int GET_COVER_IMAGE = 101;
    private static final int GET_AUDIO = 102;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mSongs = new SongsDB(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        dbSongs = new DatabaseSongsDB();

        showPromo();
        language = getResources().getConfiguration().locale.getDisplayLanguage();
//        networkFragment = NetworkFragment.getUploadInstance(getSupportFragmentManager(), new StorageDriver());
//        setContentView(R.layout.activity_songs);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        startBackgroundThread();
//        if (mTextureView.isAvailable()) {
//            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
//            connectCamera();
//        } else {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        }
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
                String languageToDisplay = language.equals("English") ? "En" : "עב";
                ((TextView) findViewById(R.id.language)).setText(languageToDisplay);
            }
        }.start();
    }

//    @Override
//    public void onListFragmentInteraction(Song item) {
//        Intent intent = new Intent(this, SingActivity.class);
//        intent.putExtra(SingActivity.EXTRA_SONG, item.fullPath.toString());
//        startActivity(intent);
//    }

    @Override
    public void onListFragmentInteraction(DatabaseSong item) {
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, item);
        startActivity(intent);
//        Intent intent = new Intent(this, test.class);
//        intent.putExtra(SingActivity.EXTRA_SONG, item);
//        startActivity(intent);
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
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
//            requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET_CODE);
//        else
//            networkFragment.startUpload()
//        urls.add(dbSongs.getSongs().get(0).getSongResourceFile());
//        urls.add(dbSongs.getSongs().get(1).getSongResourceFile());
//
//        getAudioSizes(urls.get(0), 0);
//        getAudioSizes(urls.get(1), 1);
//        if (prepared == 2) {
//            MergeTake2 mergeAudioFiles = new MergeTake2(urls, sizes);
//            mergeAudioFiles.SSoftAudCombine();
//        }

        prepareBothAudios();
    }

    private void prepareBothAudios() {
        List<MediaPlayer> mediaPlayers = new ArrayList<>();
        mediaPlayers.add(new MediaPlayer());
        mediaPlayers.add(new MediaPlayer());
//        for (MediaPlayer mPlayer:mediaPlayers)
        for(int i=0;i<2;i++) {
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

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
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
}
