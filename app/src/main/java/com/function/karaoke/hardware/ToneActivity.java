package com.function.karaoke.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.function.karaoke.core.controller.Recorder;
import com.function.karaoke.core.controller.processing.BaseToneDetector;

public class ToneActivity extends AppCompatActivity implements Recorder.IToneListener {

    private static final String[] Notes = new String[]{
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    private TextView mToneLabel;
    private Recorder mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_activity_tone);
        mToneLabel = findViewById(R.id.lbl_tone);
        mRecorder = new Recorder(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        else
            mRecorder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecorder.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            finish();
        else
            mRecorder.start();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void toneChanged(int tone, long duration) {
        if (tone < 0)
            mToneLabel.setText("No signal");
        else if (BaseToneDetector.getTones().length > tone) {
            String t = Notes[tone % Notes.length] + (2 + tone / Notes.length);
            mToneLabel.setText(t + ": " + BaseToneDetector.getTones()[tone] + " Hz");
        } else
            mToneLabel.setText("Above range:" + tone);
    }
}
