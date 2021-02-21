package com.function.karaoke.interaction.storage;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.function.karaoke.interaction.activities.Model.Recording;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecordingService {
    public static final String COLLECTION_USERS_NAME = "recordings";
    public static final String UID = "recorderId";
    public static final String RECORDER_ID = "recorderId";
    public static final String RECORDING_ID = "recordingId";
    private static final String TAG = RecordingService.class.getSimpleName();
    private static final String LOADING = "loading";
    private static final String RECORDING_URL = "recordingUrl";
    private static final String REPORTS = "reports";
    private final AuthenticationDriver authenticationDriver;
    private final CollectionReference recordingsCollectionRef;
    private DocumentReference recodingDocument;
    private Recording recording;

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

    public void getNumberOfRecordingsFromUID(NumberListener numberListener) {
        MutableLiveData<List<Recording>> recordings = new MutableLiveData<>();
        final List<Recording> documentsList = new LinkedList<>();
        Query getRecordingsQuery = recordingsCollectionRef.whereEqualTo(UID, authenticationDriver.getUserUid());
        getRecordingsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    numberListener.recordings(new ArrayList<>());
                } else {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentsList.add(document.toObject(Recording.class));
                    }
                    numberListener.recordings(documentsList);
                }
            } else {
                numberListener.failure();
            }
        });
    }

    public void getSharedRecording(String recordingId, String recorderId, GetRecordingListener getRecordingListener) {
//        MutableLiveData<Recording> recordings = new MutableLiveData<>();
        final List<Recording> documentsList = new LinkedList<>();
        Query getRecordingsQuery = recordingsCollectionRef.whereEqualTo(RECORDING_ID, recordingId).whereEqualTo(RECORDER_ID, recorderId);
        getRecordingsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
//                    recordings.setValue(null);
                    getRecordingListener.recording(null);
                } else {
                    recodingDocument = task.getResult().getDocuments().get(0).getReference();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentsList.add(document.toObject(Recording.class));
                    }
                    recording = documentsList.get(0);
//                    recordings.setValue(documentsList.get(0));
                    getRecordingListener.recording(recording);
                }
            } else {

            }
        });
    }

    public void isRecordingInDatabase(RecordingInDatabaseListener listener, String recordingId, String recorderId) {
        Query getRecordingsQuery = recordingsCollectionRef.whereEqualTo(RECORDING_ID, recordingId).whereEqualTo(RECORDER_ID, recorderId);
        getRecordingsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.isInDatabase(!task.getResult().isEmpty());
            } else {
                listener.isInDatabase(false);
            }
        });
    }

    public void addRecordingToDataBase(Recording recording, StorageAdder.UploadListener uploadListener) {
        recordingsCollectionRef.add(recording).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                uploadListener.onSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadListener.onFailure();
            }
        });
    }

    public void updateUrlToRecording(String recordingId, String recorderId, String url, StorageAdder.UploadListener uploadListener) {
        Query getSingleRecording = recordingsCollectionRef.whereEqualTo(UID, recorderId).whereEqualTo(RECORDING_ID, recordingId);
        getSingleRecording.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().getDocuments().size() > 0)
                        changeLoadingAndUrlForRecording(task.getResult().getDocuments().get(0).getReference(), url, uploadListener);
                } else {
                    uploadListener.onFailure();
                    //todo deal with failure better
                }
            }
        });
    }

    private void changeLoadingAndUrlForRecording(DocumentReference document, String url, StorageAdder.UploadListener uploadListener) {
        Map<String, Object> data = new HashMap<>();
        data.put(LOADING, false);
        data.put(RECORDING_URL, url);
        document.update(data).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        uploadListener.onSuccess();
                    }
                }).

                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadListener.onFailure();
                    }
                });
    }

    public void deleteDocument(String recordingId, String recorderId) {
        MutableLiveData<Recording> recording = new MutableLiveData<>();
        Query getRecordingsQuery = recordingsCollectionRef.whereEqualTo(RECORDING_ID, recordingId).whereEqualTo(RECORDER_ID, recorderId);
        getRecordingsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    recording.setValue(null);
                } else {
                    task.getResult().getDocuments().get(0).getReference().delete();
                }
            } else {

            }
        });
    }

    public void addReport() {
        Map<String, Object> data = new HashMap<>();
        data.put(REPORTS, recording.getReports() + 1);
        recodingDocument.update(data);
    }

    public interface RecordingInDatabaseListener {
        void isInDatabase(boolean isIn);
    }

    public interface NumberListener {
        void recordings(List<Recording> recordings);

        void failure();
    }

    public interface GetRecordingListener {
        void recording(Recording recording);
    }
}



