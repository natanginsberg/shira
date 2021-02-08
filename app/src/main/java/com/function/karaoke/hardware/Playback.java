package com.function.karaoke.hardware;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.PresetReverb;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.function.karaoke.hardware.storage.RecordingService;
import com.function.karaoke.hardware.ui.IndicationPopups;
import com.function.karaoke.hardware.ui.PlaybackPopupOpen;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RendererConfiguration;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.AuxEffectInfo;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectorResult;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MediaClock;
import com.google.android.exoplayer2.util.MimeTypes;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;


public class Playback extends AppCompatActivity implements PlaybackStateListener, PlaybackPopupOpen.PlaybackPopupListener {

    public static final String RECORDING = "recording";
    private static final String PLAYBACK = "playback";
    private static final String AUDIO_FILE = "audio";
    private static final String DELAY = "delay";
    private static final String REPORT_EMAIL = "ashira.jewishkaraoke@gmail.com";
    private static final String EARPHONES_NOT_USED = "empty";
    private static final String LENGTH = "length";
    private static final String CAMERA_ON = "camera on";
    private static final String DIRECTORY_NAME = "playback";


    private final List<String> urls = new ArrayList<>();
    private final List<Uri> uris = new ArrayList<>();
    private String audioPath;
    RenderersFactory renderersFactory;
    private PlayerView playerView;
    private long playbackPosition = 0;
    private long length;
    private int currentWindow;
    private int delay;
    private boolean earphonesUsed = false;
    private MediaSource mediaSource;
    private SimpleExoPlayer player;
    private List<Renderer> renderers;
    private boolean cameraOn = true;
    private final TrackSelector trackSelector = new TrackSelector() {
        @Override
        public TrackSelectorResult selectTracks(RendererCapabilities[] rendererCapabilities,
                                                TrackGroupArray trackGroups, MediaSource.MediaPeriodId periodId, Timeline timeline) {
            Queue<Integer> audioRenderers = new ArrayDeque<>();
            RendererConfiguration[] configs = new RendererConfiguration[rendererCapabilities.length];
            TrackSelection[] selections = new TrackSelection[rendererCapabilities.length];
            for (int i = 0; i < rendererCapabilities.length; i++) {
                if (rendererCapabilities[i].getTrackType() == C.TRACK_TYPE_AUDIO) {
                    audioRenderers.add(i);
                    configs[i] = RendererConfiguration.DEFAULT;
                } else if (rendererCapabilities[i].getTrackType() == C.TRACK_TYPE_VIDEO) {
                    if (cameraOn) {
                        audioRenderers.add(i);
                        configs[i] = RendererConfiguration.DEFAULT;
                    }
                }
            }
            for (int i = 0; i < trackGroups.length; i++) {
                if (MimeTypes.isAudio(trackGroups.get(i).getFormat(0).sampleMimeType)) {
                    Integer index = audioRenderers.poll();
                    if (index != null) {
                        selections[index] = new FixedTrackSelection(trackGroups.get(i), 0);
                    }
                } else if (MimeTypes.isVideo(trackGroups.get(i).getFormat(0).sampleMimeType)) {
                    Integer index = audioRenderers.poll();
                    if (index != null) {
                        selections[index] = new FixedTrackSelection(trackGroups.get(i), 0);
                    }
                }
            }
            return new TrackSelectorResult(configs, selections, new Object());
        }

        @Override
        public void onSelectionActivated(Object info) {
        }
    };
    private int sessionId = -1;
    private AudioRendererWithoutClock earphoneRenderer;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        playerView = findViewById(R.id.surface_view);
        playbackPopupOpen = new PlaybackPopupOpen(findViewById(android.R.id.content).getRootView(), this, this);
        if (getIntent().getExtras() != null)
            if (getIntent().getExtras().containsKey(PLAYBACK)) {
                getUrisFromIntent();
            } else if (getIntent().getExtras().containsKey(RECORDING)) {
                getUrlsFromIntent();
            } else {
                getDynamicLink();
            }
    }

    private void getUrlsFromIntent() {
        Recording recording = (Recording) getIntent().getSerializableExtra(RECORDING);
        if (recording == null) finish();
        if (recording.isLoading()) {
            uris.add(Uri.parse(recording.getRecordingUrl()));
        } else {
            urls.add(recording.getRecordingUrl());
        }
        cameraOn = recording.isCameraOn();
        if (!recording.getAudioFileUrl().equals(EARPHONES_NOT_USED)) {
            if (recording.isLoading()) {
                uris.add(Uri.parse(recording.getAudioFileUrl()));
            } else {
                urls.add(recording.getAudioFileUrl());
            }
            earphonesUsed = true;
        } else {
//            findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
//            findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
        }
        delay = recording.getDelay();
        if (recording.getLength() != 0)
            length = recording.getLength();
        if (uris.size() > 0)
            buildMediaSourceFromUris(uris);
        else
            buildMediaSourceFromUrls(urls);
        createPlayer();
    }

    private void getUrisFromIntent() {
        if (getIntent().getExtras().containsKey(PLAYBACK)) {
            uris.add(Uri.parse(getIntent().getStringExtra(PLAYBACK)));
            cameraOn = getIntent().getExtras().getBoolean(CAMERA_ON);
            if (getIntent().getExtras().containsKey(AUDIO_FILE)) {
//                uris.add(Uri.parse(getIntent().getStringExtra(AUDIO_FILE)));

                audioPath = (String) getIntent().getStringExtra(AUDIO_FILE);
                earphonesUsed = true;
            } else {
//                findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
//                findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
            }
            delay = getIntent().getIntExtra(DELAY, 0);
            length = getIntent().getLongExtra(LENGTH, 10000);
//            buildMediaSourceFromUris(uris);
//            createPlayer();
            downloadFile(audioPath);
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
                validatePassword(popupView.findViewById(R.id.password) != null ? (String) ((EditText) popupView.findViewById(R.id.password)).getText().toString() : "");
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
                delWithRecording(recording);
            }
        });
//        .observe(this, recordingObserver);
    }

    private void delWithRecording(Recording recording) {
        if (recording != null) {
            if (recording.isLoading()) {
                alertUserThatVideoIsNotLoaded();
            } else {
                if (recording.getReports() > 3) {
                    IndicationPopups.openXIndication(this, findViewById(android.R.id.content).getRootView(), getString(R.string.inapproriate_under_review));
                    startTimerToFinish();
                } else {
                    urls.add(recording.getRecordingUrl());
                    if (!recording.getAudioFileUrl().equals(EARPHONES_NOT_USED)) {
                        urls.add(recording.getAudioFileUrl());
                        earphonesUsed = true;
                    } else {
//                        findViewById(R.id.playback_spinner).setVisibility(View.INVISIBLE);
//                        findViewById(R.id.playback_word).setVisibility(View.INVISIBLE);
                    }
                    buildMediaSourceFromUrls(urls);
                    createPlayer();
                }
//                initializePlayer();
            }
        } else {
            IndicationPopups.openXIndication(this, findViewById(android.R.id.content).getRootView(), getString(R.string.recording_does_not_exist));
        }
    }

    private void alertUserThatVideoIsNotLoaded() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.video_loading);
        alertBuilder.setMessage(R.string.video_loading_body);
        alertBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            finish();
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void createPlayer() {
        renderersFactory = new CustomRendererFactory(this);
        player =
                new SimpleExoPlayer.Builder(this, renderersFactory)
                        .setTrackSelector(trackSelector)
                        .build();
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setPlayer(player);
        player.setMediaSource(mediaSource);
//        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
//        player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        findViewById(R.id.exo_pr_circle).setVisibility(View.INVISIBLE);
        player.addListener(new Player.EventListener() {

            //            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
//                if (playbackState == ExoPlayer.STATE_ENDED) {
////                    releasePlayer();
////                    finish();
//                }
//                if (playbackState == ExoPlayer.STATE_READY) {
//                    if (sessionId == -1)
//                        sessionId = player.getAudioSessionId();
//                    addReverb();
//                }
//            }
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == ExoPlayer.STATE_ENDED) {
                    releasePlayer();
                    finish();
                }
                if (state == ExoPlayer.STATE_READY) {
                    if (sessionId == -1)
                        sessionId = player.getAudioSessionId();
                    addReverb();
                }
            }
        });
        player.prepare();
        player.seekTo(currentWindow, playbackPosition);
        player.setSeekParameters(SeekParameters.EXACT); // accurate seeking
        player.setPlayWhenReady(true);
//        addSpinnerListeners();
        if (earphonesUsed) player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
                sessionId = audioSessionId;
            }
        });
        if (!cameraOn)
            findViewById(R.id.logo).setVisibility(View.VISIBLE);
    }

    private void addReverb() {
        PresetReverb pReverb = new PresetReverb(1, sessionId);
        pReverb.setPreset(PresetReverb.PRESET_SMALLROOM);
        pReverb.setEnabled(true);
        AuxEffectInfo effect = new AuxEffectInfo(pReverb.getId(), 1f);
        player.createMessage(renderers.get(1)).setType(Renderer.MSG_SET_AUX_EFFECT_INFO).setPayload(effect).send();
        player.prepare();
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
        hideSystemUi();
//        if (player == null && (urls.size() > 0 || uris.size() > 0)) {
//            createPlayer();
//            initializePlayer();
//        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
            if (mVideoFile != null)
                mVideoFile.delete();
        }
    }

    private void startBuild() {
        findViewById(R.id.loading_progress).setVisibility(View.INVISIBLE);
        uris.add(Uri.fromFile(mVideoFile));
        buildMediaSourceFromUris(uris);
        createPlayer();
    }

    private void buildMediaSourceFromUris(List<Uri> uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Shira"));
        MediaItem mediaItem = new MediaItem.Builder().setUri(uri.get(0)).build();
        MediaSource mediaSource1 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        if (delay != 0) {
            mediaSource1 = new ClippingMediaSource(mediaSource1, delay * 1000, 1000000000, false, true, true);
        }
        if (uris.size() > 1) {
            MediaItem mediaItem1 = new MediaItem.Builder().setUri(uri.get(1)).build();
            MediaSource mediaSource2 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem1);
            if (length != 0)
                mediaSource2 = new ClippingMediaSource(mediaSource2, 0, length * 1000);
            MediaSource[] mediaSources = new MediaSource[]{mediaSource1, mediaSource2};
            mediaSource = new MergingMediaSource(true, mediaSources);
        } else
            mediaSource = mediaSource1;
    }

    private void buildMediaSourceFromUrls(List<String> urls) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Shira"));
        Uri song = Uri.parse(urls.get(0));
        MediaItem mediaItem = new MediaItem.Builder().setUri(song).build();
        MediaSource mediaSource1 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);

        if (delay != 0) {
            mediaSource1 = new ClippingMediaSource(mediaSource1, delay * 1000, 1000000000, false, true, true);

        }
        if (urls.size() > 1) {
            Uri song1 = Uri.parse(urls.get(1));
            MediaItem mediaItem1 = new MediaItem.Builder().setUri(song1).build();
            MediaSource mediaSource2 = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem1);
            if (length != 0)
                mediaSource2 = new ClippingMediaSource(mediaSource2, 0, length * 1000);
            MediaSource[] mediaSources = new MediaSource[]{mediaSource1, mediaSource2};
            mediaSource = new MergingMediaSource(true, mediaSources);
        } else
            mediaSource = mediaSource1;
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
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

    public void turnReverbOnOrOff(View view) {
        if (sessionId == -1)
            sessionId = player.getAudioSessionId();
        addReverb();
    }

    public void pauseVideo(View view) {
        if (player != null && player.getPlaybackState() == Player.STATE_READY) {
            player.setPlayWhenReady(false);
            View popupView = playbackPopupOpen.openPopup(R.id.pause_playback_video, R.layout.pause_playback_video);
            playbackPopupOpen.getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (player != null)
                        player.setPlayWhenReady(true);
                }
            });
            addPopupListeners(popupView);
        }
    }

    private void addPopupListeners(View popupView) {
        popupView.findViewById(R.id.continue_watching).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playbackPopupOpen.dismissPopup();

            }
        });

        popupView.findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReport();

            }
        });

        popupView.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStop();
                playbackPopupOpen.dismissPopup();
                if (externalView) {
                    startPromo();
                } else {
                    finish();
                }

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
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{REPORT_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_inappropriate_content));
        intent.setData(Uri.parse("mailto:"));
        if (deviceHasGoogleAccount())
            intent.setPackage("com.google.android.gm");
        startActivity(intent);
    }

    private boolean deviceHasGoogleAccount() {
        AccountManager accMan = AccountManager.get(this);
        Account[] accArray = accMan.getAccountsByType("com.google");
        return accArray.length >= 1;
    }


    private static final class AudioRendererWithoutClock extends MediaCodecAudioRenderer {
        public AudioRendererWithoutClock(Context context,
                                         MediaCodecSelector mediaCodecSelector) {
            super(context, mediaCodecSelector);
        }

        @Override
        public MediaClock getMediaClock() {
            return null;
        }
    }

    private class CustomRendererFactory extends DefaultRenderersFactory {

        public CustomRendererFactory(Context context) {
            super(context);
        }

        // it is called internally. Do not delete!
        protected void buildAudioRenderers​(Context context,
                                            int extensionRendererMode,
                                            MediaCodecSelector mediaCodecSelector,
                                            boolean enableDecoderFallback, AudioSink audioSink,
                                            Handler eventHandler, AudioRendererEventListener eventListener,
                                            ArrayList<Renderer> out) {
            super.buildAudioRenderers(context, extensionRendererMode, mediaCodecSelector, enableDecoderFallback, audioSink, eventHandler, eventListener, out);

            if (earphonesUsed)
                earphoneRenderer = new AudioRendererWithoutClock(context, mediaCodecSelector);
            out.add(earphoneRenderer);
            renderers = out;
        }
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
                byte data[] = new byte[1024];
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

    @SuppressLint("SetTextI18n")
    private void showProgress(String progress) {
        ((TextView) findViewById(R.id.loading_progress)).setText(progress + "%");
    }


}
