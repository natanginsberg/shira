package com.function.karaoke.interaction.storage;

import androidx.annotation.NonNull;

import com.function.karaoke.interaction.activities.Model.SongRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

public class SongRequestAdder {

    private static final String REQUEST = "songRequests";
    private static CollectionReference requestCollectionRef;


    public static void addSongToDatabase(DatabaseDriver databaseDriver, SongRequest song, RequestListener requestListener) {
        requestCollectionRef = databaseDriver.getCollectionReferenceByName(REQUEST);
        requestCollectionRef.add(song).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                requestListener.onFailure();
            }
        }).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                requestListener.onSuccess();
            }
        });
    }

    public interface RequestListener {
        void onSuccess();

        void onFailure();
    }
}
