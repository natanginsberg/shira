package com.function.karaoke.interaction.storage;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongService {

    private final String COLLECTION_SONGS_NAME = "songs";
    private final String TIMES_DOWNLOADED = "timesDownloaded";
    private final String TIMES_PLAYED = "timesPlayed";
    private final String UPDATING = "updating";
    private CollectionReference songsCollectionRef;
    private final List<String> fields = new ArrayList<>();


    public void updateSongData(String title) {
        if (fields.size() > 0) {
            DatabaseDriver databaseDriver = new DatabaseDriver();
            songsCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_SONGS_NAME);
            DocumentReference document = songsCollectionRef.document(title);
            addOneMoreDownload(document);
        }
    }

    public void addSongTime(String title, long time){
        DatabaseDriver databaseDriver = new DatabaseDriver();
        songsCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_SONGS_NAME);
        DocumentReference document = songsCollectionRef.document(title);
        Map<String, Object> data = new HashMap<>();
        data.put("length", time);
        document.update(data);
    }

    private void addOneMoreDownload(DocumentReference document) {
        document.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document1 = task.getResult();
                if (document1 != null) {
                    long downloads = (Long) document1.get(TIMES_DOWNLOADED);
                    long shares = (Long) document1.get(TIMES_PLAYED);
                    updateDownloads(document, downloads + 1, shares + 1);
                }
            }
        });
    }

    private void updateDownloads(DocumentReference document, long downloads, long shares) {
        Map<String, Object> data = new HashMap<>();
        if (fields.contains(TIMES_DOWNLOADED))
            data.put(TIMES_DOWNLOADED, downloads);
        if (fields.contains(TIMES_PLAYED))
            data.put(TIMES_PLAYED, shares);
        document.update(data);
        fields.clear();
    }

    public void addFieldToUpdate(String field) {
        if (!fields.contains(field))
            fields.add(field);
    }
}

