package com.function.karaoke.hardware;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.ui.IndicationPopups;
import com.function.karaoke.hardware.ui.PlaybackPopupOpen;
import com.function.karaoke.hardware.utils.PlaybackPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Playback extends AppCompatActivity implements PlaybackStateListener, PlaybackPopupOpen.PlaybackPopupListener {

    public static final String RECORDING = "recording";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final String DELAY = "delay";
    private static final String EARPHONES_NOT_USED = "empty";
    private static final String LENGTH = "length";
    private static final String CAMERA_ON = "camera on";
    private static final String DIRECTORY_NAME = "playback";


    private final List<Uri> uris = new ArrayList<>();
    private long length;
    private int delay;
    private boolean earphonesUsed = false;
    private boolean cameraOn = true;
    private String password;
    private File mVideoFolder;
    private String fileName;
    private File mVideoFile;
    private CountDownTimer cTimer;
    private String recordingId;
    private String recorderId;
    private PlaybackPopupOpen playbackPopupOpen;
    private boolean externalView = false;
    private RecordingService recordingService;
    private boolean validated = false;
    private PlaybackPlayer playbackPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PlayerView playerView = findViewById(R.id.surface_view);
        playbackPlayer = new PlaybackPlayer(this, playerView);
        playbackPlayer.assignView(findViewById(android.R.id.content).getRootView());
        playbackPopupOpen = new PlaybackPopupOpen(findViewById(android.R.id.content).getRootView(), this, this);
        if (getIntent().getExtras() != null)
            if (getIntent().getExtras().containsKey(PLAYBACK)) {
                getUrisFromIntent();
            } else if (getIntent().getExtras().containsKey(RECORDING)) {
                Recording recording = (Recording) getIntent().getSerializableExtra(RECORDING);
                getUrisFromRecording(recording);
            } else {
                getDynamicLink();
            }
        setSeekBarListener();
    }

    private void setSeekBarListener() {
        SeekBar seekBar = findViewById(R.id.seekBar);
        int midProgress = 5;
        int originalDelay = Integer.parseInt(String.valueOf(delay));
        Log.i("bug78", "these are the seconds in the middle" + midProgress);
        seekBar.setMax(2 * midProgress);
        seekBar.setProgress(midProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.i("bug78", "this is the old delay" + delay);
                Log.i("bug78", "this is the progress" + i);
                delay = Math.max(0, originalDelay + (i - midProgress) * 100);
                Log.i("bug78", "this is the new delay" + delay);
                ((TextView) findViewById(R.id.sync)).setText((i > midProgress ? "+" : "-") + Math.abs(i - midProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playbackPlayer.getPlayer().setPlayWhenReady(false);
                playbackPlayer.releasePlayer();
                playbackPlayer.buildMediaSourceFromUris(uris, delay, length);
                playbackPlayer.createPlayer(Playback.this);
            }
        });
    }

    private void getUrisFromRecording(Recording recording) {
        if (recording == null) {
            if (externalView) {
                PopupWindow popupWindow = IndicationPopups.openXIndication(this, findViewById(android.R.id.content).getRootView(), getString(R.string.recording_does_not_exist));
                startTimerToFinish();
                return;
            } else {
                finish();
            }
        }
        if (externalView) {
            if (recording.isLoading()) {
                alertUserThatVideoIsNotLoaded();
                return;
            } else if (externalView && recording.getReports() > 3) {
                IndicationPopups.openXIndication(this, findViewById(android.R.id.content).getRootView(), getString(R.string.inapproriate_under_review));
                startTimerToFinish();
                return;
            }
        }
        uris.add(Uri.parse(recording.getRecordingUrl()));
        cameraOn = recording.isCameraOn();
        if (!recording.getAudioFileUrl().equals(EARPHONES_NOT_USED)) {
            uris.add(Uri.parse(recording.getAudioFileUrl()));
            earphonesUsed = true;
        }
        delay = recording.getDelay();
        if (recording.getLength() != 0)
            length = recording.getLength();
        playbackPlayer.buildMediaSourceFromUris(uris, delay, length);
        playbackPlayer.assignEarphonesAndCamera(cameraOn, earphonesUsed);
        playbackPlayer.createPlayer(this);

    }

    private void getUrisFromIntent() {
        if (getIntent().getExtras().containsKey(PLAYBACK)) {
            uris.add(Uri.parse(getIntent().getStringExtra(PLAYBACK)));
            cameraOn = getIntent().getExtras().getBoolean(CAMERA_ON);
            if (getIntent().getExtras().containsKey(AUDIO_FILE)) {
                uris.add(Uri.parse(getIntent().getStringExtra(AUDIO_FILE)));

                String audioPath = getIntent().getStringExtra(AUDIO_FILE);
                earphonesUsed = true;
            } else {
//                findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
//                findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
            }
            delay = getIntent().getIntExtra(DELAY, 0);
            length = getIntent().getLongExtra(LENGTH, 10000);
            playbackPlayer.buildMediaSourceFromUris(uris, delay, length);
            playbackPlayer.assignEarphonesAndCamera(cameraOn, earphonesUsed);
            playbackPlayer.createPlayer(this);
//            downloadFile(audioPath);
        }
    }


    private void getDynamicLink() {
        externalView = true;
        continueAsGuest();
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {

                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
//                            String originalUrl = deepLink.toString();

//                            addUrls(deepLink.toString());

                        if (deepLink == null)
                            finish();
                        else {
                            recordingId = deepLink.getQueryParameter("recId");
                            recorderId = deepLink.getQueryParameter("uid");
                            delay = Integer.parseInt(Objects.requireNonNull(deepLink.getQueryParameter("delay")));
                            if (deepLink.getQueryParameter("length") != null)
                                length = Long.parseLong(Objects.requireNonNull(deepLink.getQueryParameter("length")));
                            if (deepLink.getQueryParameter("cameraOn") != null)
                                cameraOn = Boolean.parseBoolean(deepLink.getQueryParameter("cameraOn"));
                            if (deepLink.getQueryParameter("password") != null) {
                                password = deepLink.getQueryParameter("password");
                                showPasswordBox();
                            } else
                                addUrls(recordingId, recorderId);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    private static final String TAG = "Error";

                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void continueAsGuest() {
        AuthenticationDriver authenticationDriver = new AuthenticationDriver();
        if (authenticationDriver.isSignIn())
            return;
        SignInViewModel signInViewModel = new SignInViewModel();
        signInViewModel.createGuestId(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    IndicationPopups.openXIndication(getBaseContext(), findViewById(android.R.id.content).getRootView(), getResources().getString(R.string.server_is_down));
                    startTimerToFinish();
                }

            }
        });
    }

    private void startTimerToFinish() {
        cTimer = new CountDownTimer(1500, 500) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                startPromo();
            }

        };
        cTimer.start();
    }

    private void showPasswordBox() {

        View popupView = playbackPopupOpen.openPopup(R.id.password_protected_popup, R.layout.password_protected_popup);
        popupView.findViewById(R.id.enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePassword(popupView.findViewById(R.id.password) != null ? ((EditText) popupView.findViewById(R.id.password)).getText().toString() : "");
            }
        });

        playbackPopupOpen.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!validated)
                    startPromo();
            }
        });

    }

    @Override
    public void validatePassword(String password) {
        if (password.equalsIgnoreCase(this.password)) {
            addUrls(recordingId, recorderId);
            validated = true;
            playbackPopupOpen.dismissPopup();
        } else
            invalidCodeIndication();
    }


    private void invalidCodeIndication() {
        playbackPopupOpen.showInvalidPassword();
    }


    private void addUrls(String recordingId, String recorderId) {
        recordingService = new RecordingService();
        recordingService.getSharedRecording(recordingId, recorderId, new RecordingService.GetRecordingListener() {
            @Override
            public void recording(Recording recording) {
                getUrisFromRecording(recording);
            }
        });
//        .observe(this, recordingObserver);
    }

    private void alertUserThatVideoIsNotLoaded() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setMessage(R.string.video_loading_body_if_video_not_loaded);
        alertBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            finish();
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public void showEndPopup() {
        View popupView = playbackPopupOpen.openPopup(R.id.end_playback_video, R.layout.end_playback_video);
        playbackPopupOpen.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (playbackPlayer.getPlayer() == null)
                    endVideo();
            }
        });
        addEndPopupListeners(popupView);
    }

    private void addEndPopupListeners(View popupView) {
        addRestartListener(popupView);
        if (externalView)
            addReportListener(popupView);
        addExitListener(popupView);
    }

    private void addRestartListener(View popupView) {
        popupView.findViewById(R.id.continue_watching).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playbackPlayer.restart();
                playbackPlayer.createPlayer(Playback.this);
                playbackPopupOpen.dismissPopup();
            }
        });
    }

    private void endVideo() {
        if (externalView) {
            startPromo();
        } else {
            Intent intent = new Intent(this, SingActivity.class);
            intent.putExtra("delay", delay);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (playbackPopupOpen.getPopup() != null) {
            playbackPopupOpen.dismissPopup();
        } else {
            onStop();
            endVideo();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
//        if (Util.SDK_INT >= 24 && players.size() == 0) {
//            initializePlayer();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        playbackPlayer.hideSystemUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        playbackPlayer.releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            playbackPlayer.releasePlayer();
            if (mVideoFile != null)
                mVideoFile.delete();
        }
    }

    private void startBuild() {
        findViewById(R.id.loading_progress).setVisibility(View.INVISIBLE);
        uris.add(Uri.fromFile(mVideoFile));
        playbackPlayer.buildMediaSourceFromUris(uris, delay, length);
        playbackPlayer.assignEarphonesAndCamera(cameraOn, earphonesUsed);

        playbackPlayer.createPlayer(this);
    }

    private void addSpinnerListeners() {
//        Spinner recordingSpinner = findViewById(R.id.recording_spinner);
//        recordingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String volume = adapterView.getItemAtPosition(i).toString();
//                if (!volume.equals(getResources().getString(R.string.default_value))) {
//                    player.createMessage(renderers.get(1)).setType(Renderer.MSG_SET_VOLUME).setPayload(Float.parseFloat(volume)).send();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//        Spinner playbackSpinner = findViewById(R.id.playback_spinner);
//        playbackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if (earphonesUsed) {
//                    String volume = adapterView.getItemAtPosition(i).toString();
//                    if (!volume.equals(getResources().getString(R.string.default_value))) {
//                        player.createMessage(renderers.get(2)).setType(Renderer.MSG_SET_VOLUME).setPayload(Float.parseFloat(volume)).send();
//                    }
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
    }

//    public void turnReverbOnOrOff(View view) {
//        if (sessionId == -1)
//            sessionId = player.getAudioSessionId();
//        addReverb();
//    }

    //todo deal with this one
    public void pauseVideo(View view) {
        if (playbackPlayer.getPlayer() != null && playbackPlayer.getPlayer().getPlaybackState() == Player.STATE_READY) {
            playbackPlayer.setPlayerToStart(false);
            View popupView = playbackPopupOpen.openPopup(R.id.pause_playback_video, R.layout.pause_playback_video);
            playbackPopupOpen.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (playbackPlayer.getPlayer() != null)
                        playbackPlayer.setPlayerToStart(true);
                }
            });
            addPopupListeners(popupView);
        }
    }

    private void addPopupListeners(View popupView) {
        addContinueListener(popupView);
        if (externalView)
            addReportListener(popupView);

        addExitListener(popupView);


    }

    private void addContinueListener(View popupView) {
        popupView.findViewById(R.id.continue_watching).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playbackPopupOpen.dismissPopup();

            }
        });
    }

    private void addReportListener(View popupView) {
        popupView.findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReport();

            }
        });
    }

    private void addExitListener(View popupView) {
        popupView.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStop();
                playbackPopupOpen.dismissPopup();
                endVideo();

            }
        });
    }

    private void startPromo() {
        Intent intent = new Intent(this, PromoActivity.class);
        startActivity(intent);
    }

    private void sendReport() {
        if (recordingService != null)
            recordingService.addReport();
        IndicationPopups.openCheckIndication(this, findViewById(android.R.id.content).getRootView(), getString(R.string.report_received));
        startTimerToFinish();
    }

    private void downloadFile(String audioPath) {
        createVideoFolder();
        try {
            createVideoFileName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new DownloadFileAsync().execute(audioPath);
    }

    ////////////////
    /// this is for if need to download file from cloud
    ////////////////

    private void createVideoFolder() {
//        File movieFile = activity.getCacheDir();
//        File movieFile = context.getFilesDir();
        mVideoFolder = new File(getFilesDir(), DIRECTORY_NAME);
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private void createVideoFileName() throws IOException {
        String prepend = "exoplayer";
        File videoFile = new File(mVideoFolder, prepend + ".mp4");
        fileName = videoFile.getAbsolutePath();
        mVideoFile = videoFile;
    }

    @SuppressLint("SetTextI18n")
    private void showProgress(String progress) {
        ((TextView) findViewById(R.id.loading_progress)).setText(progress + "%");
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(fileName);
                byte[] data = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            showProgress(progress[0]);
//            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            startBuild();
//            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }


    }


}
