package com.function.karaoke.hardware.utils;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;

import java.io.Serializable;

public class UrlHolder implements Serializable {
    DatabaseSong dbSong;

    public UrlHolder(DatabaseSong dbSong) {
        this.dbSong = dbSong;

    }

    public void parseSongWords() {
        dbSong.setLines();
    }


//    public List<Song.Line> getSongLines(){
//        return dbSong.getLines();
//    }
}
