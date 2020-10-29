package com.function.karaoke.hardware.storage;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

//    public LiveData<String> uploadRecording(Recording recording) {
//        final MutableLiveData<String> resultsLiveData = new MutableLiveData<>();
//        if (videoUri != null) {
//            String destFileName = "images/" + System.currentTimeMillis();
//            StorageReference ref = this.storageReference.child(destFileName);
//            ref.putFile(videoUri)
//                    .addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
//                        Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
//                        while (!downloadUri.isSuccessful()) {
//                        }
//                        resultsLiveData.setValue(downloadUri.getResult().toString());
//                        recording.setRecordingUrl(downloadUri.getResult().toString());
//                        addRecordingToFirestore(recording);
//                    })
//                    .addOnFailureListener(e -> Log.w(TAG, "Error on uploadImage", e));
//        }
//        return resultsLiveData;
//    }

    public void uploadRecording(Recording recording, UploadListener uploadListener) {
        if (videoUri != null) {
            String destFileName = "images/" + recording.getDate();
            StorageReference ref = this.storageReference.child(destFileName);
            ref.putFile(videoUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                            while (!downloadUri.isSuccessful()) {
                            }
                            recording.setRecordingUrl(downloadUri.getResult().toString());
                            addRecordingToFirestore(recording, uploadListener);
//                            uploadListener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> uploadListener.onFailure());
        }
    }

    private void addRecordingToFirestore(Recording recording, UploadListener uploadListener) {
        RecordingService recordingService = new RecordingService();
        recordingService.addRecordingToDataBase(recording);
        ArtistService artistService = new ArtistService(new ArtistService.ArtistServiceListener() {
            @Override
            public void onSuccess() {
                uploadListener.onSuccess();
            }

            @Override
            public void onFailure() {

            }
        });
        artistService.addDownloadToArtist(recording.getRecorderId(), recording.getArtist());
    }

    public interface UploadListener {
        void onSuccess();

        void onFailure();
    }
}
