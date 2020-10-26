package com.function.karaoke.hardware.storage;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;

public class ArtistService {

    public static final String COLLECTION_USERS_NAME = "artists";
    public static final String SINGLE_DOWNLOADS = "singleDownloads";
    public static final String RECORDER_ID = "recorderId";
    public static final String RECORDING_ID = "recordingId";
    private static final String TAG = RecordingService.class.getSimpleName();
    private AuthenticationDriver authenticationDriver;
    private CollectionReference artistsCollectionRef;

    public ArtistService() {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        artistsCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_USERS_NAME);
    }

    public void addDownloadToArtist(String artistName, ArtistServiceListener artistServiceListener) {
        artistsCollectionRef.document(artistName).update(SINGLE_DOWNLOADS, 1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                artistServiceListener.onSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                artistServiceListener.onFailure();
            }
        });
    }

    public interface ArtistServiceListener {
        void onSuccess();

        void onFailure();
    }

}
