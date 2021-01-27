package com.function.karaoke.core.controller;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.media.audiofx.PresetReverb;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by ink on 2017-06-12.
 */

public class Recorder {

    private AudioRecord mRecorder;
    private AudioTrack mPlayer;

    public interface IToneListener {
        void toneChanged(int tone, long duration);
    }

    public static final int SAMPLE_RATE_IN_HZ = 16000;

    public static final int SENSITIVITY_NORMAL = 750;
    public static final int SENSITIVITY_AMBIENT = 80;

    private final Handler mHandler;
    private volatile AudioRecordingThread mThread;

    public Recorder(final IToneListener controller) {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                controller.toneChanged(msg.what, msg.arg1);
            }
        };
    }

    public Recorder() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

            }
        };
    }

    public boolean start() {
        if (null != mThread)
            return true; // running
        try {
            final int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            if (NoiseSuppressor.isAvailable()) {
                NoiseSuppressor ns = NoiseSuppressor.create(mRecorder.getAudioSessionId());
                ns.setEnabled(true);
            }

            if (AcousticEchoCanceler.isAvailable()) {
                AcousticEchoCanceler aec = AcousticEchoCanceler.create(mRecorder.getAudioSessionId());
                aec.setEnabled(true);
            }

            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize, AudioTrack.MODE_STREAM);
            PresetReverb reverb = new PresetReverb(1, mPlayer.getAudioSessionId());
            reverb.setPreset(PresetReverb.PRESET_LARGEHALL);
            reverb.setEnabled(true);

            mPlayer.setAuxEffectSendLevel(1.f);

            mRecorder.startRecording();
            mPlayer.play();


            mThread = new AudioRecordingThread(mRecorder, mPlayer, bufferSize);
            mThread.start();
        } catch (Exception e) {
            mThread = null;
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        AudioRecordingThread thread = this.mThread;
        mRecorder.release();
        mPlayer.release();
        if (null == thread)
            return;
        try {
            thread.stopRecording();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return null != mThread;
    }

    private class AudioRecordingThread extends Thread {

        private short[] mAudioBuffer;

        private volatile boolean mIsRecording = true;
        private final AudioRecord mRecorder;
        private final AudioTrack mPlayer;
        private final int mBufferSize;

        AudioRecordingThread(AudioRecord audioRecord, AudioTrack player, int bufferSize) throws Exception {
            mRecorder = audioRecord;
            mPlayer = player;
            mBufferSize = bufferSize;

//            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
//                    AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize <= 0)
                throw new Exception("Unsupported record profile");
            mAudioBuffer = new short[bufferSize];
        }

        @Override
        public void run() {
//            AudioRecord record = null;
//            NoiseSuppressor noise = null;
//            AcousticEchoCanceler echo = null;
//            try {
//                do {
            // should be in constructor
//                    mre = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                            SAMPLE_RATE_IN_HZ,
//                            AudioFormat.CHANNEL_IN_MONO,
//                            AudioFormat.ENCODING_PCM_16BIT,
//                            mAudioBuffer.length);

//                    if (record.getRecordingState() != AudioRecord.STATE_INITIALIZED)
//                        break;
//
//                    if (NoiseSuppressor.isAvailable()) {
//                        noise = NoiseSuppressor.create(record.getAudioSessionId());
//                        if (null != noise && !noise.getEnabled())
//                            noise.setEnabled(true);
//                    }
//
//                    if (AcousticEchoCanceler.isAvailable()) {
//                        echo = AcousticEchoCanceler.create(record.getAudioSessionId());
//                        if (null != echo && !echo.getEnabled())
//                            echo.setEnabled(true);
//                    }

//                    IToneDetector detector = new GoertzelToneDetectorJNI(SAMPLE_RATE_IN_HZ, SENSITIVITY_NORMAL, 10);
//                    record.startRecording();

//                    int currentTone = -1;
//                    while (mIsRecording) {
//                        int read = record.read(mAudioBuffer, 0, mAudioBuffer.length);
//                        if (read <= 0) {
//                            continue;
//                        }
//                        int tone = detector.analyze(mAudioBuffer, read);
//                        if (currentTone != tone) {
//                            currentTone = tone;
//                            mHandler.obtainMessage(currentTone, 1000 * read / SAMPLE_RATE_IN_HZ, 0).sendToTarget();
//                        }
//                    }
//
//                    record.stop();
//
//                } while (false);
            while (mIsRecording) {
                byte[] buffer = new byte[mBufferSize];

                int read;
                while ((read = mRecorder.read(buffer, 0, buffer.length)) > 0) {
                    mPlayer.write(buffer, 0, buffer.length);
                }
                onPlayed();
            }

//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                if (null != record)
//                    record.release();
//                if (null != echo)
//                    echo.release();
//                if (null != noise)
//                    noise.release();
//            }
//            mThread = null;
        }

        public void stopRecording() throws InterruptedException {
            mIsRecording = false;
            join();
        }
    }

    private void onPlayed() {
    }

}
