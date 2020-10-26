package com.function.karaoke.hardware.storage;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ArtistService {

    public static final String COLLECTION_USERS_NAME = "artists";
    public static final String SINGLE_DOWNLOADS = "singleDownloads";
    private static final String LAST_USER = "lastUserToUpdate";
    private final ArtistServiceListener artistServiceListener;
    private CollectionReference artistsCollectionRef;

    public ArtistService(ArtistServiceListener artistServiceListener) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        artistsCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_USERS_NAME);
        this.artistServiceListener = artistServiceListener;
    }

    public void addDownloadToArtist(String recorderId, String artistName) {
        DocumentReference document = artistsCollectionRef.document(artistName);
        addOneMoreDownload(recorderId, document);
    }

    private void addOneMoreDownload(String recorderId, DocumentReference document) {
        document.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document1 = task.getResult();
                if (document1 != null) {
                    long singleDownloads = (Long) document1.get(SINGLE_DOWNLOADS);
                    updateDownloads(document, recorderId, singleDownloads + 1);
                } else {

                }
            } else {

            }
        });
    }

    private void updateDownloads(DocumentReference document, String recorderId, long i) {
        Map<String, Object> data = new HashMap<>();
        data.put(SINGLE_DOWNLOADS, i);
        data.put(LAST_USER, recorderId);
        document.update(data).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkThatThereWasNoInterference(document, recorderId);
                    }
                }).

                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        artistServiceListener.onFailure();
                    }
                });
    }

    private void checkThatThereWasNoInterference(DocumentReference document, String recorderId) {
        document.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document1 = task.getResult();
                if (document1 != null) {
                    if (Objects.equals((String) document1.get(LAST_USER), recorderId)) {
                        artistServiceListener.onSuccess();
                    }
                } else {
                    addOneMoreDownload(recorderId, document);
                }
            } else {

            }
        });
    }

    public interface ArtistServiceListener {
        void onSuccess();

        void onFailure();
    }

}
