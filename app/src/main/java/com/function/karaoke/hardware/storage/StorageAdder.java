package com.function.karaoke.hardware.storage;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.function.karaoke.hardware.activities.Model.Recording;

import java.io.File;
import java.io.Serializable;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;

/**
 * A Driver which is responsible for actions with Firebase Storage
 */
public class StorageAdder extends ViewModel implements Serializable {
    private final String BUCKET_NAME = "recordings-of-songs";
    private final File file;
    //    private FirebaseStorage storage;
//    private StorageReference storageReference;
    private static final String TAG = "StorageDriver";
    private String videoUri;
    private Context context;
    private AmazonS3Client s3Client;
    private boolean setUp;
    private String accessKey;
    private String sKey;

    public StorageAdder(File videoUri, Context context) {
//        this.storage = FirebaseStorage.getInstance();
//        this.storageReference = storage.getReference();
        this.file = videoUri;
        this.context = context;
        getKeys();
    }

//    public LiveData<String> uploadVideo() {
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
//
//
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//
//                        }
//                    });
////                            Log.w(TAG, "Error on uploadImage", e));
//        }
//        return resultsLiveData;
//    }


    public void uploadRecording(Recording recording, UploadListener uploadListener) {
        Uri downloadUri = Uri.parse("https://s3.wasabisys.com/recordings-of-songs/" + file.getName());
        recording.setRecordingUrl(downloadUri.toString());
        addRecordingToFirestore(recording, uploadListener);
//        if (videoUri != null) {
//            String destFileName = "images/" + recording.getDate();
//            StorageReference ref = this.storageReference.child(destFileName);
//            StorageTask<UploadTask.TaskSnapshot> uploadTask = ref.putFile(videoUri);
//            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
//                if (!task.isSuccessful()) {
//                    throw task.getException();
//                }
//
//                // Continue with the task to get the download URL
//                return ref.getDownloadUrl();
//            }).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    Uri downloadUri = task.getResult();
//                    recording.setRecordingUrl(downloadUri.toString());
//                    addRecordingToFirestore(recording, uploadListener);
//                } else {
//                    // Handle failures
//                    // ...
//                    uploadListener.onFailure();
//                }
//            });
//        }
    }


    private void getKeys() {
        DatabaseDriver dd = new DatabaseDriver();
        dd.getKeys(new DatabaseDriver.KeyListener() {
            @Override
            public void onSuccess(String id, String secretKey) {
                accessKey = id;
                sKey = secretKey;
                finishSetUp();
            }
        });
    }

    public void finishSetUp() {

        Region region = Region.US_EAST_1;

        final String END_POINT = "https://s3.wasabisys.com";
        while (accessKey == null) {
        }
        AWSCredentials myCredentials = new BasicAWSCredentials(
                accessKey, sKey);

        s3Client = new AmazonS3Client(myCredentials);
        s3Client.setRegion(com.amazonaws.regions.Region.getRegion(region.toString()));
        s3Client.setEndpoint(END_POINT);
        setUp = true;
    }

    public String uploadFile() {
        while (!setUp) {
        }
        PutObjectRequest por = new PutObjectRequest(BUCKET_NAME, file.getName(), file).withCannedAcl(CannedAccessControlList.PublicRead);
        s3Client.putObject(por);
        s3Client.setObjectAcl(BUCKET_NAME, file.getName(), CannedAccessControlList.PublicRead);
        return null;
    }

    /**
     * Deletes the bucket specified, given the bucket is empty.
     *
     * @param s3
     * @param bucketName
     */
    public static void deleteEmptyBucket(final S3Client s3, final String bucketName) {
        DeleteBucketRequest req = DeleteBucketRequest.builder().bucket(bucketName).build();
        System.out.println(s3.deleteBucket(req).toString());
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

    public interface SetUpListener {
        void onSuccess();
    }

}
