package com.function.karaoke.hardware.activities.Model;

public class AudioUploaded {

    String url;
    Long size;

    public AudioUploaded(String url, Long size) {
        this.url = url;
        this.size = size;
    }

    public Long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }
}
