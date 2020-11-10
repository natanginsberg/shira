package com.function.karaoke.hardware.storage;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ArtistService {

    public static final String COLLECTION_USERS_NAME = "artists";
    public static final String SINGLE_DOWNLOADS = "singleDownloads";
    private static final String UPDATING = "updating";
    private final ArtistServiceListener artistServiceListener;
    private CollectionReference artistsCollectionRef;

    public ArtistService(ArtistServiceListener artistServiceListener) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        artistsCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_USERS_NAME);
        this.artistServiceListener = artistServiceListener;
    }

    public void addDownloadToArtist(String artistName) {
        DocumentReference document = artistsCollectionRef.document(artistName);
        addOneMoreDownload(document);
    }

    private void addOneMoreDownload(DocumentReference document) {
        document.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document1 = task.getResult();
                if (document1 != null) {
                    long singleDownloads = (Long) document1.get(SINGLE_DOWNLOADS);
                    updateDownloads(document, singleDownloads + 1);
                } else {

                }
            } else {

            }
        });
    }

    private void updateDownloads(DocumentReference document, long i) {
        Map<String, Object> data = new HashMap<>();
        data.put(SINGLE_DOWNLOADS, i);
//        data.put(LAST_USER, recorderId);
        document.update(data).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        artistServiceListener.onSuccess();
//
                    }
                }).

                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e.getMessage().equals("PERMISSION_DENIED: Missing or insufficient permissions.")) {
                            addOneMoreDownload(document);
                        }
                        artistServiceListener.onFailure();
                    }
                });
    }

    public interface ArtistServiceListener {
        void onSuccess();

        void onFailure();
    }

}
