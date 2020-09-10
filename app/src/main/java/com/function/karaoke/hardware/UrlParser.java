package com.function.karaoke.hardware;

import android.graphics.Bitmap;

import com.function.karaoke.hardware.DatabaseSong;
import com.function.karaoke.core.model.Song;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;
import java.util.List;

public class UrlParser implements Serializable {
    DatabaseSong dbSong;

    public UrlParser(DatabaseSong dbSong) {
        this.dbSong = dbSong;

    }

    public void parseSongWords() {
        dbSong.setLines();
    }


//    public List<Song.Line> getSongLines(){
//        return dbSong.getLines();
//    }

    public DatabaseSong getDbSong(){
        return dbSong;
    }
}
