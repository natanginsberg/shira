package com.function.karaoke.hardware.storage;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.tasks.NetworkTasks;
import com.function.karaoke.hardware.utils.JsonCreator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class CloudUpload {

    private static final String JSON_FILE_NAME = "savedJson";
    private static final String ARTIST_FILE = "artistUpdated";
    private static final String JSON_DIRECTORY_NAME = "jsonFile";

    private final Recording recording;
    private final DatabaseSong song;
    private final File folder;
    private StorageAdder storageAdder;
    private File artistFile;
    private File jsonFileFolder;

    public CloudUpload(Recording recording, File folder, DatabaseSong song) {
        this.recording = recording;
        this.folder = folder;
        this.song = song;
    }

    //    private void saveToCloud(Uri path, View view1) {
    public void saveToCloud(File path) {
        String jsonFilePath = createTempFiles();
        JsonCreator.createJsonObject(path, recording, jsonFilePath);
        storageAdder = new StorageAdder(path);
        ArtistService artistService = new ArtistService(new ArtistService.ArtistServiceListener() {
            @Override
            public void onSuccess() {
                artistFile.delete();
                NetworkTasks.uploadToWasabi(storageAdder, new NetworkTasks.UploadToWasabiListener() {
                    @Override
                    public void onSuccess() {
                        storageAdder.uploadRecording(recording, new StorageAdder.UploadListener() {
                            @Override
                            public void onSuccess() {
                                deleteJsonFolder();
                            }

                            @Override
                            public void onFailure() {

                            }
                        });
                    }

                    @Override
                    public void onFail() {
//                    ((ProgressBar) parentView.findViewById(R.id.upload_progress_wheel)).setBackgroundColor(Color.BLACK);
                    }
                });
            }

            @Override
            public void onFailure() {
                int k = 0;
            }
        });
        artistService.addDownloadToArtist(song.getArtist());
    }

    private void deleteJsonFolder() {
        for (File child : Objects.requireNonNull(jsonFileFolder.listFiles()))
            child.delete();
        jsonFileFolder.delete();
    }

    private void createEmptyFileForArtist(File folder) throws IOException {
        artistFile = new File(folder, ARTIST_FILE + ".txt");
        FileWriter writer = new FileWriter(artistFile);
        writer.write("32");
        writer.close();
    }

    private String createTempFiles() {
        jsonFileFolder = new File(folder, JSON_DIRECTORY_NAME);
        if (!jsonFileFolder.exists())
            jsonFileFolder.mkdirs();
        try {
            createEmptyFileForArtist(jsonFileFolder);
            return createJsonFile(jsonFileFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String createJsonFile(File folder) throws IOException {
        File videoFile = new File(folder, JSON_FILE_NAME + ".json");
        return videoFile.getAbsolutePath();
    }

}
