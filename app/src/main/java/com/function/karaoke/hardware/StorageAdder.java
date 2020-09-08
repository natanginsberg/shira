package com.function.karaoke.hardware;

import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.function.karaoke.hardware.activities.Model.AudioUploaded;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

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

    public LiveData<AudioUploaded> uploadAudio(byte[] bytes) {
        final MutableLiveData<AudioUploaded> resultsLiveData = new MutableLiveData<>();
        if (bytes != null) {
            String destFileName = "user_videos/" + System.currentTimeMillis();
            StorageReference ref = this.storageReference.child(destFileName);
            ref.putBytes(bytes)
                    .addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
                        Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                        while (!downloadUri.isSuccessful()) {
                        }
                        resultsLiveData.setValue(new AudioUploaded(downloadUri.getResult().toString(), taskSnapshot.getMetadata().getSizeBytes()));
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error on uploadImage", e));
        }
        return resultsLiveData;
    }
}
