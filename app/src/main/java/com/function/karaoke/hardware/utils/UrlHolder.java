package com.function.karaoke.hardware.utils;

import android.graphics.Bitmap;

import com.function.karaoke.hardware.DatabaseSong;
import com.function.karaoke.core.model.Song;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;
import java.util.List;

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
