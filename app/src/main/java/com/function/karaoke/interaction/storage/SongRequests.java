package com.function.karaoke.interaction.storage;

import androidx.annotation.NonNull;

import com.function.karaoke.interaction.activities.Model.SongRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SongRequests {

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

    public static void getAllRequestedSongs(DatabaseDriver databaseDriver, RequestGetter requestGetter) {
        requestCollectionRef = databaseDriver.getCollectionReferenceByName(REQUEST);
        List<SongRequest> documentsList = new ArrayList<>();
        requestCollectionRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            documentsList.add(document.toObject(SongRequest.class));
                        }
                        requestGetter.getSongs(documentsList);
                    }
                });
    }

    public interface RequestListener {
        void onSuccess();

        void onFailure();
    }

    public interface RequestGetter {
        void getSongs(List<SongRequest> songs);
    }
}
