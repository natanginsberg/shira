package com.function.karaoke.interaction;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;

import com.function.karaoke.core.controller.KaraokeController;
import com.function.karaoke.interaction.utils.CameraPreview;
import com.function.karaoke.interaction.utils.static_classes.SyncFileData;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoHolder {

    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private final CameraPreview cameraPreview;
    private final KaraokeController mKaraokeController;
    private final File cacheDir;
    private int delay;
    private String timeStamp;
    private boolean ending;
    private boolean isRecording;
    private long lengthOfAudioPlayed;
    private final WeakReference<Activity> activityWeakReference;
    private File postParseVideoFile;

    public VideoHolder(CameraPreview cameraPreview, KaraokeController karaokeController, File cacheDir, Activity activity) {
        this.cameraPreview = cameraPreview;
        this.mKaraokeController = karaokeController;
        isRecording = true;
        this.cacheDir = cacheDir;
        activityWeakReference = new WeakReference<>(activity);
    }

    public void pauseSong(){
        if (mKaraokeController != null)
            mKaraokeController.onPause();
        if (Util.SDK_INT >= 24) {
            if (isRecording) {
                cameraPreview.pauseRecording();
            }
        } else {
            finishSong();
        }
    }

    public void finishSong() {
        if (isRecording) {
            if (postParseVideoFile == null) {
                lengthOfAudioPlayed = mKaraokeController.getmPlayer().getCurrentPosition();
                postParseVideoFile = wrapUpSong();
            }
        }
        //todo openEndOptions removed from here
    }

    private void stopRecordingAndSong() {
        ending = true;
        if (isRecording) {
            cameraPreview.stopRecording();
            isRecording = false;
            cameraPreview.closeCamera();
        }
        if (mKaraokeController.isPlaying()) {

            mKaraokeController.onStop();
//            customMediaPlayer.onStop();
        }
    }

    public File wrapUpSong() {
        try {
            stopRecordingAndSong();
            File file = cameraPreview.getVideo();
            File newlyParsedFile = SyncFileData.parseVideo(file, getOutputMediaFile());
            setDelay(Uri.fromFile(newlyParsedFile));
            return newlyParsedFile;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setDelay(Uri uriFromFile) {
        MediaPlayer mp = MediaPlayer.create(activityWeakReference.get(), uriFromFile);
        int duration = mp.getDuration();
        delay = (int) (duration - lengthOfAudioPlayed);
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(cacheDir, DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setLengthOfAudioPlayed(long length) {
        this.lengthOfAudioPlayed = length;
    }
}
