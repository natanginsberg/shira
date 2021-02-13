package com.function.karaoke.hardware.storage;

import androidx.annotation.NonNull;

import com.function.karaoke.hardware.activities.Model.SongRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;

public class SongRequestAdder {

    private static final String REQUEST = "songRequests";
    private static CollectionReference requestCollectionRef;


    public static void addSongToDatabase(DatabaseDriver databaseDriver, SongRequest song, RequestListener requestListener) {
        requestCollectionRef = databaseDriver.getCollectionReferenceByName(REQUEST);
        requestCollectionRef.document(song.getTitle()).set(song).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                requestListener.onFailure();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                requestListener.onSuccess();
            }
        });
    }

    public interface RequestListener {
        void onSuccess();

        void onFailure();
    }
}
