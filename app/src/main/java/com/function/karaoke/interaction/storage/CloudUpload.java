package com.function.karaoke.interaction.storage;

import com.function.karaoke.interaction.activities.Model.Recording;
import com.function.karaoke.interaction.tasks.NetworkTasks;

import java.io.File;
import java.util.Objects;

public class CloudUpload {

    private static final String JSON_DIRECTORY_NAME = "jsonFile";

    private final Recording recording;
    private final File appFolder;
    private final String artist;
    private final File jsonFileFolder;
    private final UploadListener uploadListener;
    private StorageAdder storageAdder;

    public CloudUpload(Recording recording, File appFolder, String artist, UploadListener uploadListener) {
        this.recording = recording;
        this.appFolder = appFolder;
        this.artist = artist;
        this.uploadListener = uploadListener;
        jsonFileFolder = new File(appFolder, JSON_DIRECTORY_NAME);
    }


    public void saveToCloud(File path) {
//        JsonCreator.createJsonObject(path, recording, folder);
        storageAdder = new StorageAdder(path);
//        ArtistService artistService = new ArtistService(new ArtistService.ArtistServiceListener() {
//            @Override
//            public void onSuccess() {
//                deleteArtistFile(path);
        storageAdder.uploadRecording(recording, new StorageAdder.UploadListener() {
            @Override
            public void onSuccess() {
                NetworkTasks.uploadToWasabi(storageAdder, new NetworkTasks.UploadToWasabiListener() {
                    @Override
                    public void onSuccess() {
                        storageAdder.updateRecordingUrl(recording, new StorageAdder.UploadListener() {
                            @Override
                            public void onSuccess() {
                                deleteJsonFile(path.getName());
                                uploadListener.onSuccess(path);
                            }

                            @Override
                            public void onFailure() {

                            }

                            @Override
                            public void progressUpdate(double progress) {
                                uploadListener.onProgress((int) progress);
                            }

                        });
                    }

                    @Override
                    public void onFail() {
//                    ((ProgressBar) parentView.findViewById(R.id.upload_progress_wheel)).setBackgroundColor(Color.BLACK);
                    }

                    @Override
                    public void onProgress(int percent) {
                        uploadListener.onProgress(percent);
                    }

                });
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void progressUpdate(double progress) {

            }
        });
    }


    private void deleteJsonFile(String name) {
        (new File(jsonFileFolder, name + ".json")).delete();
        if (jsonFileFolder.list() == null || Objects.requireNonNull(jsonFileFolder.list()).length == 0)
            jsonFileFolder.delete();
    }


    public interface UploadListener {
        void onSuccess(File file);

        void onFailure();

        void onProgress(int progress);
    }

}
