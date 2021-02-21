package com.function.karaoke.interaction.activities.Model;

public class RecordingDisplay implements Reocording {

    private final String imageResourceFile;
    private final String artist;
    private final String title;

    public RecordingDisplay(String imageResourceFile, String artist, String title) {
        this.imageResourceFile = imageResourceFile;
        this.artist = artist;
        this.title = title;
    }


    public String getImageResourceFile() {
        return imageResourceFile;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }


}
