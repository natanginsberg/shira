package com.function.karaoke.interaction.activities.Model;

import java.io.Serializable;

public class FirestoreSong implements Serializable {

    private String artist;
    private String imageResourceFile = "https://s3.wasabisys.com/playbacks/%D7%A2%D7%9E%D7%9E%D7%99/%D7%94%D7%A0%D7%A9%D7%9E%D7%94%20%D7%9C%D7%9A/Play.jpg";
    private String songResourceFile = "";
    private String textResourceFile = "";
    private String womanToneResourceFile = "";
    private String kidToneResourceFile = "";
    private int timesDownloaded = 0;
    private int timesPlayed = 0;
    private String title;
    private String genre;
    private String date;
    private int length = 0;

    public FirestoreSong() {

    }

    public FirestoreSong(String songName, String artistName, String genre, String date) {
        this.title = songName;
        this.artist = artistName;
        this.genre = genre;
        this.date = date;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getImageResourceFile() {
        return imageResourceFile;
    }

    public void setImageResourceFile(String imageResourceFile) {
        this.imageResourceFile = imageResourceFile;
    }

    public String getSongResourceFile() {
        return songResourceFile;
    }

    public void setSongResourceFile(String songResourceFile) {
        this.songResourceFile = songResourceFile;
    }

    public String getTextResourceFile() {
        return textResourceFile;
    }

    public void setTextResourceFile(String textResourceFile) {
        this.textResourceFile = textResourceFile;
    }

    public String getWomanToneResourceFile() {
        return womanToneResourceFile;
    }

    public void setWomanToneResourceFile(String womanToneResourceFile) {
        this.womanToneResourceFile = womanToneResourceFile;
    }

    public String getKidToneResourceFile() {
        return kidToneResourceFile;
    }

    public void setKidToneResourceFile(String kidToneResourceFile) {
        this.kidToneResourceFile = kidToneResourceFile;
    }

    public int getTimesDownloaded() {
        return timesDownloaded;
    }

    public void setTimesDownloaded(int timesDownloaded) {
        this.timesDownloaded = timesDownloaded;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
