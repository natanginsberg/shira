package com.function.karaoke.hardware.storage;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class SongService {

    public static final String COLLECTION_SONGS_NAME = "songs";
    public static final String TIMES_DOWNLOADED = "timesDownloaded";
    private static final String UPDATING = "updating";
    private static CollectionReference songsCollectionRef;

    public static void addDownloadToSong(String title) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        songsCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_SONGS_NAME);
        DocumentReference document = songsCollectionRef.document(title);
        addOneMoreDownload(document);
    }

    private static void addOneMoreDownload(DocumentReference document) {
        document.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document1 = task.getResult();
                if (document1 != null) {
                    long singleDownloads = (Long) document1.get(TIMES_DOWNLOADED);
                    updateDownloads(document, singleDownloads + 1);
                }
            }
        });
    }

    private static void updateDownloads(DocumentReference document, long i) {
        Map<String, Object> data = new HashMap<>();
        data.put(TIMES_DOWNLOADED, i);
        document.update(data);
    }

}

