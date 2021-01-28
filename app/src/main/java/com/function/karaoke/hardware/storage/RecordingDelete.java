package com.function.karaoke.hardware.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import software.amazon.awssdk.regions.Region;

public class RecordingDelete {


    private static final String BUCKET_NAME = "recordings-of-songs";
    private final StorageReference storageReference;
    private final RecordingService recordingService;
    private final SetupListener setupListener;
    private final List<Recording> recordings;

    private AmazonS3Client s3Client;
    private boolean setUp;

    public RecordingDelete(SetupListener setupListener, List<Recording> recordings) {
        getKeys();
        storageReference = FirebaseStorage.getInstance().getReference();
        recordingService = new RecordingService();
        this.setupListener = setupListener;
        this.recordings = recordings;
    }

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
        setupListener.setup();
    }

    public void deleteRecording() {
        Long start = System.currentTimeMillis();
        while (!setUp) {
            long end = System.currentTimeMillis();
            if (end - start > 10000) {
                System.out.println("this is taking a long time");
                start = end;
            }
        }
        for (Recording recording : recordings) {
            String[] filePath = recording.getRecordingUrl().split("/");
            s3Client.deleteObject(BUCKET_NAME, filePath[filePath.length - 1]);
            recordingService.deleteDocument(recording.getRecordingId(), recording.getRecorderId());
        }
    }

    public interface SetupListener {
        void setup();
    }
}
