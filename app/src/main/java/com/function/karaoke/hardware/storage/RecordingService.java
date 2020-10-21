package com.function.karaoke.hardware.storage;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.LinkedList;
import java.util.List;

public class RecordingService {
    private AuthenticationDriver authenticationDriver;
    private CollectionReference recordingsCollectionRef;
    public static final String COLLECTION_USERS_NAME = "recordings";
    public static final String UID = "recorderId";
    public static final String RECORDER_ID = "recorderId";
    public static final String RECORDING_ID = "recordingId";
    private static final String TAG = RecordingService.class.getSimpleName();

    public RecordingService() {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        authenticationDriver = new AuthenticationDriver();
        recordingsCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_USERS_NAME);
    }

    public LiveData<List<Recording>> getRecordingFromUID() {
        MutableLiveData<List<Recording>> recordings = new MutableLiveData<>();
        final List<Recording> documentsList = new LinkedList<>();
        Query getRecordingsQuery = recordingsCollectionRef.whereEqualTo(UID, authenticationDriver.getUserUid());
        getRecordingsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    recordings.setValue(null);
                } else {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentsList.add(document.toObject(Recording.class));
                    }
                    recordings.setValue(documentsList);
                }
            } else {

            }
        });
        return recordings;
    }

    public LiveData<Recording> getSharedRecording(String recordingId, String recorderId) {
        MutableLiveData<Recording> recording = new MutableLiveData<>();
        final List<Recording> documentsList = new LinkedList<>();
        Query getRecordingsQuery = recordingsCollectionRef.whereEqualTo(RECORDING_ID, recordingId).whereEqualTo(RECORDER_ID, recorderId);
        getRecordingsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    recording.setValue(null);
                } else {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentsList.add(document.toObject(Recording.class));
                    }
                    recording.setValue(documentsList.get(0));
                }
            } else {

            }
        });
        return recording;
    }

    public void addRecordingToDataBase(Recording recording) {
        recordingsCollectionRef.add(recording);
    }

}



