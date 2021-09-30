package com.function.karaoke.interaction.storage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.function.karaoke.interaction.activities.Model.DatabaseSong;
import com.function.karaoke.interaction.activities.Model.Genres;
import com.function.karaoke.interaction.activities.Model.Keys;
import com.function.karaoke.interaction.activities.Model.Recording;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseDriver {

    private static final int NUMBER_OF_FREE_SHARES = 1;
    private static final String TAG = "DatabaseDriver";
    private final FirebaseFirestore db;
    private final FirebaseStorage firebaseStorage;
    private final StorageReference storageReference;

    public DatabaseDriver() {
        this.db = FirebaseFirestore.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = firebaseStorage.getReference();
    }

    public FirebaseFirestore getDb() {
        return this.db;
    }

    public CollectionReference getCollectionReferenceByName(String name) {
        return this.db.collection(name);
    }

    public void getAllSongsInCollection(SongListener songListener) {
        final List<DatabaseSong> documentsList = new ArrayList<>();
        getCollectionReferenceByName("songsNew")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            documentsList.add(document.toObject(DatabaseSong.class));
                        }
                        songListener.onSuccess(documentsList);
                    }
                }).addOnFailureListener(e -> {
            songListener.onFail();
        });
    }

    public void getAllRecordings(RecordingL recordingListener) {
        final List<Recording> documentsList = new ArrayList<>();
        getCollectionReferenceByName("recordings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            documentsList.add(document.toObject(Recording.class));
                        }
                        recordingListener.onSuccess(documentsList);
                    }
                }).addOnFailureListener(e -> {
        });
    }

    public void getSong(String title, SongListener songListener) {
        final List<DatabaseSong> documentsList = new ArrayList<>();
        getCollectionReferenceByName("songsNew")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (((String) document.get("title")).equals(title))
                                documentsList.add(document.toObject(DatabaseSong.class));
                        }
                        songListener.onSuccess(documentsList);
                    }
                }).addOnFailureListener(e -> {
            songListener.onFail();
        });
    }


    public LiveData<Genres> getAllGenresInCollection() {
        final List<Genres> documentsList = new LinkedList<>();
        final MutableLiveData<Genres> resultsLiveData = new MutableLiveData<>();
        getCollectionReferenceByName("genres")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            documentsList.add(document.toObject(Genres.class));
                        }
                        if (!documentsList.isEmpty()) {
                            resultsLiveData.setValue(documentsList.get(0));
                        }
                    }
                }).addOnFailureListener(e -> {
            int k = 0;
        });
        return resultsLiveData;
    }

    public void getAllDemoSongsInCollection(DemoSongListener demoSongListener) {
        final List<String> songsList = new LinkedList<>();
        getCollectionReferenceByName("randomFields").document("phoneDemoSongs")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                demoSongListener.onSuccess((List<String>) task.getResult().get("songs"));
            }
        }).addOnFailureListener(e -> {
            int k = 0;
        });
    }

    public void getKeys(KeyListener keyListener) {
        final MutableLiveData<Genres> resultsLiveData = new MutableLiveData<>();
        AtomicReference<Keys> keys = new AtomicReference<>();
        getCollectionReferenceByName("keys")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            keys.set(document.toObject(Keys.class));
                        }
                        if (keys.get().getAccessKeyId() != null) {
                            keyListener.onSuccess(keys.get().getAccessKeyId(), keys.get().getPrivateKey());
                        }
                    }
                }).addOnFailureListener(e -> {
            int k = 0;
        });
    }

    public void getVersionWord(VersionListener vl) {
        getCollectionReferenceByName("randomFields").document("version")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                vl.onSuccess((String) task.getResult().get("word"));
            }
        }).addOnFailureListener(e -> {
            int k = 0;
        });
    }


    public interface KeyListener {
        void onSuccess(String id, String secretKey);
    }

    public interface SongListener {
        void onSuccess(List<DatabaseSong> songs);

        void onFail();
    }

    public interface VersionListener {
        void onSuccess(String word);
    }

    public interface RecordingL {
        void onSuccess(List<Recording> recordings);
    }

    public interface DemoSongListener {
        void onSuccess(List<String> songs);
    }

}
