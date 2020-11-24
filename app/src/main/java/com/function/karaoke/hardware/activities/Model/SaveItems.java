package com.function.karaoke.hardware.activities.Model;

public class SaveItems {

    private String file = null;
    private Recording recording = null;

    public SaveItems(String file, Recording recording) {
        this.file = file;
        this.recording = recording;
    }

    public SaveItems() {
    }

    public Recording getRecording() {
        return recording;
    }

    public String getArtist() {
        return recording.getArtist();
    }

    public String getFile() {
        return file;
    }
}
