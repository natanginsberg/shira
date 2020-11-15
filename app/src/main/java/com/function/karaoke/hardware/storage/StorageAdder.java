package com.function.karaoke.hardware.storage;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;

/**
 * A Driver which is responsible for actions with Firebase Storage
 */
public class StorageAdder extends ViewModel implements Serializable {
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final String TAG = "StorageDriver";
    private Uri videoUri;

    public StorageAdder(Uri videoUri) {
        this.storage = FirebaseStorage.getInstance();
        this.storageReference = storage.getReference();
        this.videoUri = videoUri;
    }

    public LiveData<String> uploadVideo() {
        final MutableLiveData<String> resultsLiveData = new MutableLiveData<>();
        if (videoUri != null) {
            String destFileName = "images/" + System.currentTimeMillis();
            StorageReference ref = this.storageReference.child(destFileName);
            ref.putFile(videoUri)
                    .addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
                        Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                        while (!downloadUri.isSuccessful()) {
                        }
                        resultsLiveData.setValue(downloadUri.getResult().toString());


                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
//                            Log.w(TAG, "Error on uploadImage", e));
        }
        return resultsLiveData;
    }


    public void uploadRecording(Recording recording, UploadListener uploadListener) {
        if (videoUri != null) {
            String destFileName = "images/" + recording.getDate();
            StorageReference ref = this.storageReference.child(destFileName);
            StorageTask<UploadTask.TaskSnapshot> uploadTask = ref.putFile(videoUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    recording.setRecordingUrl(downloadUri.toString());
                    addRecordingToFirestore(recording, uploadListener);
                } else {
                    // Handle failures
                    // ...
                    uploadListener.onFailure();
                }
            });
        }
    }

    private void addRecordingToFirestore(Recording recording, UploadListener uploadListener) {
        RecordingService recordingService = new RecordingService();
        recordingService.addRecordingToDataBase(recording);
        uploadListener.onSuccess();
    }

    public interface UploadListener {
        void onSuccess();

        void onFailure();
    }
}
