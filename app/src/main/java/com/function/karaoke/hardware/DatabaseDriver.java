package com.function.karaoke.hardware;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DatabaseDriver {

    private FirebaseFirestore db;
    private static final String TAG = "DatabaseDriver";

    public DatabaseDriver() {
        this.db = FirebaseFirestore.getInstance();
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

    public <T> LiveData<List<T>> getDocumentsByField(String collectionName, String fieldName, Object fieldValue, final Class<T> typeParameterClass) {
        return getDocumentsByField(collectionName, fieldName, Collections.singletonList(fieldValue), typeParameterClass);
    }

}
