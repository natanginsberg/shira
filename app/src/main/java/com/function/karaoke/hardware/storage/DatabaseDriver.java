package com.function.karaoke.hardware.storage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.function.karaoke.hardware.activities.Model.Genres;
import com.function.karaoke.hardware.activities.Model.Keys;
import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseDriver {

    private static final int NUMBER_OF_FREE_SHARES = 1;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private static final String TAG = "DatabaseDriver";

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

    public <T> LiveData<List<T>> getAllSongsInCollection(final Class<T> typeParameterClass) {
        final List<T> documentsList = new LinkedList<>();
        final MutableLiveData<List<T>> resultsLiveData = new MutableLiveData<>();
        getCollectionReferenceByName("songs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            documentsList.add(document.toObject(typeParameterClass));
                        }
                        if (!documentsList.isEmpty()) {
                            resultsLiveData.setValue(documentsList);
                        }
                    }
                }).addOnFailureListener(e -> {
            int k = 0;
        });
        return resultsLiveData;
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



    public interface KeyListener {
        void onSuccess(String id, String secretKey);
    }

}
