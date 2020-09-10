package com.function.karaoke.hardware;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * A Driver which is responsible for actions with Firebase Storage
 */
public class StorageAdder extends ViewModel {
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final String TAG = "StorageDriver";

    public StorageAdder() {
        this.storage = FirebaseStorage.getInstance();
        this.storageReference = storage.getReference();
    }

    public LiveData<String> uploadVideo(Uri videoUri) {
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
                    .addOnFailureListener(e -> Log.w(TAG, "Error on uploadImage", e));
        }
        return resultsLiveData;
    }

}
