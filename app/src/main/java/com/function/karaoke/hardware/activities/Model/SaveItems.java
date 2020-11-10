package com.function.karaoke.hardware.activities.Model;

import android.net.Uri;

public class SaveItems {

    private Uri file = null;
    private Recording recording = null;

    public SaveItems(Uri file, Recording recording) {
        this.file = file;
        this.recording = recording;
    }

    public SaveItems() {
    }

    public Recording getRecording() {
        return recording;
    }

    public String getArtist(){
        return recording.getArtist();
    }

    public Uri getFile() {
        return file;
    }
}
