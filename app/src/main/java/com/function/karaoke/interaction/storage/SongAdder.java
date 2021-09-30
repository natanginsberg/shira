package com.function.karaoke.interaction.storage;

import androidx.annotation.NonNull;

import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.FirestoreSong;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;

public class SongAdder {

    private static final String SONG_NAME = "songsNew";
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
