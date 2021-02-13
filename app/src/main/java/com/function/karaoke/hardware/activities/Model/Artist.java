package com.function.karaoke.hardware.activities.Model;

public class Artist {
    private String artistName;
    private int singleDownloads;

    public Artist() {
    }

    public Artist(String artistName, int singleDownloads) {
        this.singleDownloads = singleDownloads;
        this.artistName = artistName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getSingleDownloads() {
        return singleDownloads;
    }

    public void setSingleDownloads(int singleDownloads) {
        this.singleDownloads = singleDownloads;
    }
}
