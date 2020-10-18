package com.function.karaoke.hardware.activities.Model;

import java.io.Serializable;

public class Recording implements Serializable, SongDisplay {
    private String recordingId;
    private String recorderId;
    private String recordingUrl;
    private String audioFileUrl;
    String date;
    private String title;
    private String imageResourceFile;
    private String artist;

    public Recording() {
    }

    public Recording(String recordingUrl, String audioFileUrl, String artistName,
                     String imageResourceFIle, String title, String date,
                     String recorderId, String recordingId) {
        this.title = title;
        this.recordingUrl = recordingUrl;
        this.artist = artistName;
        this.audioFileUrl = audioFileUrl;
        this.imageResourceFile = imageResourceFIle;
        this.date = date;
        this.recorderId = recorderId;
        this.recordingId = recordingId;
    }

    public Recording(DatabaseSong song, String date,
                     String recorderId, String recordingId) {
        this.title = song.getTitle();
        this.artist = song.getArtist();
        this.audioFileUrl = song.getSongResourceFile();
        this.imageResourceFile = song.getImageResourceFile();
        this.date = date;
        this.recorderId = recorderId;
        this.recordingId = recordingId;
    }


    public String getAudioFileUrl() {
        return audioFileUrl;
    }

    public void setAudioFileUrl(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
    }

    public String getRecordingUrl() {
        return recordingUrl;
    }

    public void setRecordingUrl(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageResourceFile() {
        return imageResourceFile;
    }

    public void setImageResourceFile(String imageResourceFIle) {
        this.imageResourceFile = imageResourceFIle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtistName(String artist) {
        this.artist = artist;
    }

    public String getRecorderId() {
        return recorderId;
    }

    public void setRecorderId(String recorderId) {
        this.recorderId = recorderId;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = recordingId;
    }
}
