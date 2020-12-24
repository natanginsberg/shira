package com.function.karaoke.hardware.storage;

import androidx.annotation.NonNull;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.FirestoreSong;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;

public class SongAdder {

    private static final String SONG_NAME = "songs";
    private final CollectionReference songsCollectionRef;

    public SongAdder(DatabaseDriver databaseDriver) {
        songsCollectionRef = databaseDriver.getCollectionReferenceByName(SONG_NAME);
    }

    public void addSongToDatabase(FirestoreSong song) {
        songsCollectionRef.document(song.getTitle()).set(song).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                throw new RuntimeException("failed to add to firestore");
            }
        });
    }
}
