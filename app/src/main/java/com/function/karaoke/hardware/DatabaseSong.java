package com.function.karaoke.hardware;

import android.graphics.Bitmap;

import com.function.karaoke.core.model.Song;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSong implements Serializable {

    private String artist;
    private String imageResourceFile;
    private String songResourceFile;
    private String textResourceFile;
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

    public String getArtist() {
        return artist;
    }

    public String getImageResourceFile() {
        return imageResourceFile;
    }

    public String getSongResourceFile() {
        return songResourceFile;
    }

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
            URL url = new URL(textResourceFile);
            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            lines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
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
}
