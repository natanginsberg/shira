package com.function.karaoke.hardware.storage;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private AmazonS3Client s3Client;
    private boolean setUp;
    private long bytesRead = 0;
    private final StorageReference storageReference;
    private RecordingService recordingService;

    public StorageAdder(File file) {
        this.file = file;
        getKeys();
        storageReference = FirebaseStorage.getInstance().getReference();
        recordingService = new RecordingService();
    }

    public void uploadRecording(Recording recording, UploadListener uploadListener) {
        recordingService.isRecordingInDatabase(new RecordingService.RecordingInDatabaseListener() {
            @Override
            public void isInDatabase(boolean isIn) {
                if (isIn) {
                    uploadListener.onSuccess();
                } else {
                    recording.setRecordingUrl((Uri.fromFile(file)).toString());
                    addRecordingToFirestore(recording, uploadListener);
                }
            }
        }, recording.getRecordingId(), recording.getRecorderId());
    }

    public void updateRecordingUrl(Recording recording, UploadListener uploadListener) {
        Uri downloadUri = Uri.parse("https://s3.wasabisys.com/recordings-of-songs/" + file.getName());
        recordingService = new RecordingService();
        recordingService.updateUrlToRecording(recording.getRecordingId(), recording.getRecorderId(), downloadUri.toString(), uploadListener);
    }

//    public void uploadRecording2(Recording recording, UploadListener uploadListener) {
//        String destFileName = "images/" + recording.getDate();
//        StorageReference ref = this.storageReference.child(destFileName);
//        StorageTask<UploadTask.TaskSnapshot> uploadTask = ref.putFile(Uri.fromFile(file));
//        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
////                bytesRead += ;
//                double percent = 100 * (double) snapshot.getBytesTransferred() / (double) snapshot.getTotalByteCount();
//                uploadListener.progressUpdate(percent);
//            }
//        });
//        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
//            if (!task.isSuccessful()) {
//                throw task.getException();
//            }
//
//            // Continue with the task to get the download URL
//            return ref.getDownloadUrl();
//        }).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Uri downloadUri = task.getResult();
//                recording.setRecordingUrl(downloadUri.toString());
//                addRecordingToFirestore(recording, uploadListener);
//            } else {
//                // Handle failures
//                // ...
//                uploadListener.onFailure();
//            }
//        });
//    }


    private void getKeys() {
        DatabaseDriver dd = new DatabaseDriver();
        dd.getKeys(this::finishSetUp);
    }

    private void finishSetUp(String accessKey, String secretKey) {

        Region region = Region.US_EAST_1;

        final String END_POINT = "https://s3.wasabisys.com";
        AWSCredentials myCredentials = new BasicAWSCredentials(
                accessKey, secretKey);

        s3Client = new AmazonS3Client(myCredentials);
        s3Client.setRegion(com.amazonaws.regions.Region.getRegion(region.toString()));
        s3Client.setEndpoint(END_POINT);
        setUp = true;
    }

    public String uploadFile(UploadProgressListener listener) {
        long start = System.currentTimeMillis();
        while (!setUp) {
            long end = System.currentTimeMillis();
            if (end - start > 10000)
                throw new IndexOutOfBoundsException("the wasabi connection is not set up");
        }
        PutObjectRequest por = new PutObjectRequest(BUCKET_NAME, file.getName(), file).withCannedAcl(CannedAccessControlList.PublicRead);
        por.setGeneralProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                bytesRead += progressEvent.getBytesTransferred();
                double progress = Math.min(1.0, (double) bytesRead / (double) file.length());
                System.out.println("this is the progress of the upload " + progress);
                listener.progressUpdate((progress));


            }
        });
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
        recordingService = new RecordingService();
        recordingService.addRecordingToDataBase(recording, uploadListener);
    }


    public interface UploadListener {
        void onSuccess();

        void onFailure();

        void progressUpdate(double progress);
    }

    public interface UploadProgressListener {
        void progressUpdate(double progress);
    }
}
