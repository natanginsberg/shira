package com.function.karaoke.hardware.storage;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DatabaseDriver {

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
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error on getSingleDocumentByField", e));
        return resultsLiveData;
    }

    public <T> LiveData<Long> getFirstStorageReferenceSize(String reference) {
        final MutableLiveData<Long> resultsLiveData = new MutableLiveData<>();
        StorageReference gsReference = firebaseStorage.getReferenceFromUrl(reference);

        gsReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                resultsLiveData.setValue(storageMetadata.getSizeBytes());
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Error on getSingleDocumentByField", e));
        return resultsLiveData;
    }

    public <T> LiveData<Long> getSecondStorageReferenceSize(String reference) {
        final MutableLiveData<Long> resultsLiveData = new MutableLiveData<>();
        StorageReference gsReference = firebaseStorage.getReferenceFromUrl(reference);

        gsReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                resultsLiveData.setValue(storageMetadata.getSizeBytes());
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Error on getSingleDocumentByField", e));
        return resultsLiveData;
    }

    public <T> LiveData<List<T>> getDocumentsByField(String collectionName, String fieldName, Object fieldValue, final Class<T> typeParameterClass) {
        return getDocumentsByField(collectionName, fieldName, Collections.singletonList(fieldValue), typeParameterClass);
    }

}
