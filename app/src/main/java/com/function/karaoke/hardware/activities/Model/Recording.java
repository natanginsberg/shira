package com.function.karaoke.hardware.activities.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Recording implements Serializable, SongDisplay {
    private static final String EARPHONES_USED = "empty";
    private String recordingId;
    private String recorderId;
    private String recordingUrl;
    private String audioFileUrl;
    private String date;
    private String title;
    private String imageResourceFile;
    private String artist;
    private int delay;
    private long length = 0;
    private boolean cameraOn = true;

    public Recording() {
    }

    public Recording(String recordingUrl, String audioFileUrl, String artistName,
                     String imageResourceFIle, String title, String date,
                     String recorderId, String recordingId, int delay, long length, boolean cameraOn) {
        this.title = title;
        this.recordingUrl = recordingUrl;
        this.artist = artistName;
        this.audioFileUrl = audioFileUrl;
        this.imageResourceFile = imageResourceFIle;
        this.date = date;
        this.recorderId = recorderId;
        this.recordingId = recordingId;
        this.delay = delay;
        this.length = length;
        this.cameraOn = cameraOn;
    }

    public Recording(DatabaseSong song, String date,
                     String recorderId, String recordingId, int delay, boolean cameraOn) {
        this.title = song.getTitle();
        this.artist = song.getArtist();
        this.audioFileUrl = song.getSongResourceFile();
        this.imageResourceFile = song.getImageResourceFile();
        this.date = date;
        this.recorderId = recorderId;
        this.recordingId = recordingId;
        this.delay = delay;
        this.cameraOn = cameraOn;
    }

    public Recording(DatabaseSong song, String songPlayed, String date,
                     String recorderId, String recordingId, int delay, long length, boolean cameraOn) {
        this.title = song.getTitle();
        this.artist = song.getArtist();
        this.audioFileUrl = songPlayed;
        this.imageResourceFile = song.getImageResourceFile();
        this.date = date;
        this.recorderId = recorderId;
        this.recordingId = recordingId;
        this.delay = delay;
        this.length = length;
        this.cameraOn = cameraOn;
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

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public JSONObject putRecordingInJsonObject() throws JSONException {
        JSONObject recording = new JSONObject();
        recording.put("recordingId", recordingId);
        recording.put("recorderId", recorderId);
        recording.put("title", title);
        recording.put("artist", artist);
        recording.put("date", date);
        recording.put("audioFileUrl", audioFileUrl);
        recording.put("imageFileUrl", imageResourceFile);
        recording.put("delay", delay);
        recording.put("length", length);
        recording.put("cameraOn", cameraOn);
        return recording;

    }

    public void earphonesUsed() {
        audioFileUrl = EARPHONES_USED;
    }

    public boolean isCameraOn() {
        return cameraOn;
    }

    public void setCameraOn(boolean cameraOn) {
        this.cameraOn = cameraOn;
    }
}
