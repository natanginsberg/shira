package com.function.karaoke.hardware.activities.Model;

import android.os.AsyncTask;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DatabaseSongsDB {

    private final List<DatabaseSong> mSongs = new LinkedList<>();

//    private File mRoot;

    private AsyncTask mScanTask;

    public DatabaseSongsDB() {
//        mRoot = root;
    }

    public DatabaseSongsDB(DatabaseSongsDB songsDB) {
//        mRoot = songsDB.mRoot;
        mScanTask = songsDB.mScanTask;
    }

    public boolean containsSong(String songName, String artistName) {
        for (DatabaseSong song : mSongs) {
            if (song.getTitle().equalsIgnoreCase(songName) && artistName.equalsIgnoreCase(song.getArtist()))
                return true;
        }
        return false;
    }

    public double getAverageSongsPlayed() {
        long number = 0;
        for (DatabaseSong song : mSongs) {
            number += song.getTimesPlayed();
        }
        if (mSongs.size() > 0)
            return (double) number / (double) mSongs.size();
        else
            return 0.0;
    }

    public interface IListener {
        void onListUpdated();
    }

    public List<DatabaseSong> getSongs() {
        return mSongs;
    }

    private final HashSet<IListener> mListeners = new HashSet<>();

    public void subscribe(DatabaseSongsDB.IListener listener) {
        mListeners.add(listener);
    }

    private void notifyUpdated() {
        // make a copy before iterating
        for (IListener l : new HashSet<>(mListeners))
            l.onListUpdated();
    }

    private void songsUpdated(List<DatabaseSong> songs) {
        mScanTask = null;
        if (mSongs.size() == 0)
            mSongs.addAll(songs);
        else
            for (DatabaseSong song : songs)
                if (mSongs.contains(song))
                    continue;
                else
                    mSongs.add(song);
//        mSongs.clear();

        notifyUpdated();
    }

    public void updateSongs(List<DatabaseSong> songs) {
        songsUpdated(songs);
    }
}
