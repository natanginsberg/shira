package com.function.karaoke.interaction.activities.Model;

public class SongRequest {

    private String title;
    private String artist;
    private String comments;

    public SongRequest() {
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

