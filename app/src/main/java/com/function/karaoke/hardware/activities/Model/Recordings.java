package com.function.karaoke.hardware.activities.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Recordings {
    private String recordingUrl;
    private String audioFileUrl;
    private @ServerTimestamp
    Date date;
    private String songName;
    private String coverImageUrl;
    private String artistName;

    public Recordings(String recordingUrl, String audioFileUrl, String artistName, String coverImageUrl, String songName, Date date) {
        this.songName = songName;
        this.recordingUrl = recordingUrl;
        this.artistName = artistName;
        this.audioFileUrl = audioFileUrl;
        this.coverImageUrl = coverImageUrl;
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}
