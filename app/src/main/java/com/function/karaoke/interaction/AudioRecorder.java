package com.function.karaoke.interaction;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;

import com.function.karaoke.interaction.utils.AudioLatencyTuner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioRecorder {

    private static final int SAMPLE_RATE_IN_HZ = 20000;

    private static final String DIRECTORY_NAME = "camera2videoImageNew";


    private AudioRecord mRecorder;
    private AudioTrack mPlayer;
    private int bufferSize;
    private boolean isRecording;
    private Thread recordingThread;
    private String filePath;
    private File mAudioFile;
    private boolean paused = false;
    private int id;
    private AudioLatencyTuner latencyTuner;

    public AudioRecorder(Context context) {
        createFile(context);
        setRecorder();
        setPlayer();

    }

    private void createFile(Context context) {
        File mVideoFolder = createVideoFolder(context);
        String timeStamp = new SimpleDateFormat("yyyyMM_HHmmss").format(new Date());
        String prepend = "AUDIO" + timeStamp + "_";
        File videoFile = new File(mVideoFolder, prepend + ".mp3");
        filePath = videoFile.getAbsolutePath();
        mAudioFile = videoFile;
    }

    private File createVideoFolder(Context context) {
//        File movieFile = activity.getCacheDir();
//        File movieFile = context.getFilesDir();
        File mVideoFolder = new File(context.getFilesDir(), DIRECTORY_NAME);
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
        return mVideoFolder;
    }

    private void setRecorder() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
//        bufferSize = 128;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_PERFORMANCE,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        } else
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
//        if (NoiseSuppressor.isAvailable()) {
//            NoiseSuppressor ns = NoiseSuppressor.create(mRecorder.getAudioSessionId());
//            ns.setEnabled(true);
//        }
//
//        if (AcousticEchoCanceler.isAvailable()) {
//            AcousticEchoCanceler aec = AcousticEchoCanceler.create(mRecorder.getAudioSessionId());
//            aec.setEnabled(true);
//        }

    }

    private void setPlayer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize, AudioTrack.PERFORMANCE_MODE_LOW_LATENCY);
        } else {
            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize, AudioTrack.MODE_STREAM);
        }

    }

    public void startRecorder() {
        mRecorder.startRecording();
        latencyTuner = new AudioLatencyTuner(mPlayer, bufferSize);
//        PresetReverb reverb = new PresetReverb(1, 0);
//        mPlayer.attachAuxEffect(reverb.getId());
//        reverb.setPreset(PresetReverb.PRESET_LARGEHALL);
//        reverb.setEnabled(true);
//        mPlayer.setAuxEffectSendLevel(1.0f);
//        mPlayer.setVolume(1f);

        mPlayer.play();
//        warmUpPlayer();
        isRecording = true;
        recordingThread = new RecordingThread(mRecorder, mPlayer, bufferSize);
        recordingThread.start();
    }

    private int warmUpPlayer(byte[] buffer) {
        int i = 0;
        while (i < 60) {
            mPlayer.write(buffer, 0, bufferSize);
            latencyTuner.update();
            i++;
        }
        return latencyTuner.getBufferSizeInFrames();
    }

    public void pauseRecorder() {
        paused = true;
    }

    public void resumeRecording() {
        paused = false;
    }

    public void stopRecording() {
        isRecording = false;
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile(byte[] buffer) {
//        short sData[] = new short[bufferSize];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int bufSize = warmUpPlayer(buffer);

        buffer = new byte[bufSize];

        while (isRecording) {
            while (!(paused)) {
                // gets the voice output from microphone to byte format
                mRecorder.read(buffer, 0, buffer.length);
                mPlayer.write(buffer, 0, buffer.length);

//                System.out.println("Short writing to file" + buffer.toString());
//                try {
//                    // // writes the data to file from buffer
//                    // // stores the voice buffer
////                byte[] bData = short2byte(sData);
//                    os.write(buffer, 0, buffer.length);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile() {
        mAudioFile.delete();
    }

    private class RecordingThread extends Thread {
        private final int mBufferSize;

        public RecordingThread(AudioRecord recorder, AudioTrack player, int bufferSize) {
            mRecorder = recorder;
            mPlayer = player;
            mBufferSize = bufferSize;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[mBufferSize];

            writeAudioDataToFile(buffer);

            mRecorder.release();
            mPlayer.release();
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
