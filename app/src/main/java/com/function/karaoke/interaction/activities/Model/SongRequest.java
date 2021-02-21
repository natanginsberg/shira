package com.function.karaoke.interaction.activities.Model;

public class SongRequest {

    private final String title;
    private final String artist;
    private String comments;

    public SongRequest(String title, String artist) {
        this.artist = artist;
        this.title = title;
    }

    public SongRequest(String title, String artist, String comments) {
        this.title = title;
        this.artist = artist;
        this.comments = comments;
    }

    public String getArtist() {
        return artist;
    }

    public String getComments() {
        return comments;
    }

    public String getTitle() {
        return title;
    }
}

