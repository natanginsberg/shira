package com.function.karaoke.hardware.activities.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Recording implements Serializable, Reocording {
    private static final String EARPHONES_NOT_USED = "empty";
    private boolean freeShareUsed;
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
    private boolean loading = false;
    private int reports = 0;

    public Recording() {
    }

    public Recording(String recordingUrl, String audioFileUrl, String artistName,
                     String imageResourceFIle, String title, String date,
                     String recorderId, String recordingId, int delay, long length, boolean cameraOn, boolean loading) {
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
        this.loading = loading;
    }

    public Recording(DatabaseSong song, String date,
                     String recorderId, String recordingId, int delay, boolean cameraOn, boolean loading) {
        this.title = song.getTitle();
        this.artist = song.getArtist();
        this.audioFileUrl = song.getSongResourceFile();
        this.imageResourceFile = song.getImageResourceFile();
        this.date = date;
        this.recorderId = recorderId;
        this.recordingId = recordingId;
        this.delay = delay;
        this.cameraOn = cameraOn;
        this.loading = loading;
    }

    public Recording(DatabaseSong song, String songPlayed, String date,
                     String recorderId, String recordingId, int delay, long length, boolean cameraOn, boolean loading) {
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
        this.loading = loading;
    }

    public Recording(boolean freeShareUsed, DatabaseSong song, String songPlayed, String date,
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
        this.freeShareUsed = freeShareUsed;
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
        recording.put("freeShareUsed", freeShareUsed);
        return recording;

    }

    public void earphonesNotUsed() {
        audioFileUrl = EARPHONES_NOT_USED;
    }

    public boolean isCameraOn() {
        return cameraOn;
    }

    public void setCameraOn(boolean cameraOn) {
        this.cameraOn = cameraOn;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Recording)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Recording r = (Recording) o;

        // Compare the data members and return accordingly
        return r.getTitle().equalsIgnoreCase(title)
                && r.getArtist().equalsIgnoreCase(artist);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((artist == null) ? 0 : artist.hashCode());
        result = prime * result
                + ((imageResourceFile == null) ? 0 : imageResourceFile.hashCode());
        return result;
    }

    public int getReports() {
        return reports;
    }

    public void setReports(int reports) {
        this.reports = reports;
    }

    public boolean isFreeShareUsed() {
        return freeShareUsed;
    }

    public void setFreeShareUsed(boolean freeShareUsed) {
        this.freeShareUsed = freeShareUsed;
    }
}
