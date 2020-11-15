package com.function.karaoke.hardware.activities.Model;

public class RecordingDisplay implements SongDisplay{

    private String imageResourceFile;
    private String artist;
    private String title;

    public RecordingDisplay(String imageResourceFile, String artist, String title){
        this.imageResourceFile=imageResourceFile;
        this.artist=artist;
        this.title=title;
    }


    public String getImageResourceFile(){
        return imageResourceFile;
    }

    public String getArtist(){
        return artist;
    }

    public String getTitle(){
        return title;
    }


}
