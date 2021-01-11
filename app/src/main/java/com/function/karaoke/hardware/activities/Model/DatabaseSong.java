package com.function.karaoke.hardware.activities.Model;

import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSong implements Serializable, Reocording {

    private String artist;
    private String imageResourceFile;
    private String songResourceFile;
    private String textResourceFile;
    private String womanToneResourceFile;
    private String kidToneResourceFile;
    private int timesDownloaded;
    private int timesPlayed;
    private String title;
    private List<String> lines;
    private Bitmap image = null;
    private String genre;
    private String songReference;

    public DatabaseSong() {

    }

    public DatabaseSong(String artist, String imageResourceFile, String songResourceFile, String textResourceFile, int timesDownloaded,
                        int timesPlayed, String title, String genre, String songReference) {
        this.artist = artist;
        this.imageResourceFile = imageResourceFile;
        this.songResourceFile = songResourceFile;
        this.textResourceFile = textResourceFile;
        this.timesDownloaded = timesDownloaded;
        this.timesPlayed = timesPlayed;
        this.title = title;
        this.genre = genre;
        this.songReference = songReference;
    }

    public DatabaseSong(String artist, String imageResourceFile, String songResourceFile, String textResourceFile, int timesDownloaded,
                        int timesPlayed, String title, String genre, String songReference, String womanToneResourceFile, String kidToneResourceFile) {
        this.artist = artist;
        this.imageResourceFile = imageResourceFile;
        this.songResourceFile = songResourceFile;
        this.textResourceFile = textResourceFile;
        this.timesDownloaded = timesDownloaded;
        this.timesPlayed = timesPlayed;
        this.title = title;
        this.genre = genre;
        this.songReference = songReference;
        this.womanToneResourceFile = womanToneResourceFile;
        this.kidToneResourceFile = kidToneResourceFile;
    }

    public DatabaseSong(String title, String artist, String imageResourceFile, String songResourceFile) {
        this.title = title;
        this.artist = artist;
        this.imageResourceFile = imageResourceFile;
        this.songResourceFile = songResourceFile;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageResourceFile() {
        return imageResourceFile;
    }

    public String getSongResourceFile() {
        return songResourceFile;
    }

    // do not delete, neseccary for the parsing.
    public String getTextResourceFile() {
        return textResourceFile;
    }

    public int getTimesDownloaded() {
        return timesDownloaded;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public String getTitle() {
        return title;
    }

    public void setLines() {
        try {
            if (textResourceFile == null)
                throw new RuntimeException("np text resource file");
            URL url = new URL(textResourceFile);
            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            lines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
            if (lines.size() == 0)
                throw new OutOfMemoryError("lines size is 0");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getLines() {
        return lines;
    }

    public String getGenre() {
        return genre;
    }

    public String getSongReference() {
        return songReference;
    }

    public boolean hasDifferentTones() {
        return !(womanToneResourceFile == null && kidToneResourceFile == null);
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
}
