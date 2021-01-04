package com.function.karaoke.hardware.utils;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.SaveItems;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

public class JsonHandler {

    private static final String JSON_FILE_NAME = "savedJson";
    private static final String ARTIST_FILE = "artistUpdated";
    private static final String JSON_DIRECTORY_NAME = "jsonFile";

    private static void putJsonInFile(String storageFilePath, JSONObject jsonObject) {
        String userString = jsonObject.toString();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(storageFilePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(userString);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void createTempJsonObject(File videoPath, Recording recording, File appFolder) {
        try {
            deletePreviousPendingFiles(appFolder);
            String storageFilePath = createTempFiles(appFolder, videoPath.getName() + "Pending");
            JSONObject saveItems = new JSONObject();
            saveItems.put("filePath", videoPath.getPath());
            saveItems.put("recording", recording.putRecordingInJsonObject());
            putJsonInFile(storageFilePath, saveItems);
        } catch (JSONException e) {

        }
    }


    public static String readInputStreamToString(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }

    public static SaveItems getDatabaseFromInputStream(InputStream jsonFileInputStream) {
        SaveItems saveItems;
        try {
            saveItems = generateSavedItemsFromInputStream(jsonFileInputStream);
        } catch (JSONException | IOException e) {
            saveItems = new SaveItems();
        }
        return saveItems;
    }

    private static SaveItems generateSavedItemsFromInputStream(InputStream jsonFileInputStream)
            throws JSONException, IOException {
        JSONObject dbJsonRawObject = new JSONObject(readInputStreamToString(jsonFileInputStream));

        String fileUri = dbJsonRawObject.getString("filePath");
        JSONObject recordingToAdd = dbJsonRawObject.getJSONObject("recording");
        DatabaseSong song = new DatabaseSong(recordingToAdd.getString("title"),
                recordingToAdd.getString("artist"),
                recordingToAdd.getString("imageFileUrl"),
                recordingToAdd.getString("audioFileUrl"));
        Recording recording = new Recording(song, recordingToAdd.getString("date"),
                recordingToAdd.getString("recorderId"),
                recordingToAdd.getString("recordingId"),
                recordingToAdd.getInt("delay"));
        if (recordingToAdd.has("length"))
            recording.setLength(recordingToAdd.getLong("length"));
        return new SaveItems(fileUri, recording);
    }

    private static void createEmptyFileForArtist(File jsonFolder, String videoPath) throws IOException {
        File artistFile = new File(jsonFolder, videoPath + "artist.txt");
        FileWriter writer = new FileWriter(artistFile);
        writer.write("32");
        writer.close();
    }

    private static String createTempFiles(File appFolder, String videoPath) {
        File jsonFileFolder = new File(appFolder, JSON_DIRECTORY_NAME);
        if (!jsonFileFolder.exists())
            jsonFileFolder.mkdirs();
        try {
            createEmptyFileForArtist(jsonFileFolder, videoPath);
            return createJsonFile(jsonFileFolder, videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createJsonFile(File jsonFolder, String videoPath) throws IOException {
        File videoFile = new File(jsonFolder, videoPath + ".json");
        return videoFile.getAbsolutePath();
    }

    public static File renameJsonPendingFile(File appFolder) {
        File jsonFileFolder = new File(appFolder, JSON_DIRECTORY_NAME);
        File fileToReturn = null;
        if (jsonFileFolder.listFiles() != null)
            for (File child : Objects.requireNonNull(jsonFileFolder.listFiles()))
                if (child.getName().contains("Pending")) {
                    File secondName = new File(jsonFileFolder, child.getName().replaceAll("Pending", ""));
                    child.renameTo(secondName);
                    if (!child.getName().contains("artist")) {
                        fileToReturn = secondName;
                    }
                }
        return fileToReturn;
    }

    public static void deletePendingJsonFile(File appFolder) {

        File jsonFileFolder = new File(appFolder, JSON_DIRECTORY_NAME);
        if (jsonFileFolder.listFiles() != null)
            for (File child : Objects.requireNonNull(jsonFileFolder.listFiles()))
                if (child.getName().contains("Pending")) {
                    child.delete();
                }
    }

    private static void deletePreviousPendingFiles(File appFolder) {
        File jsonFileFolder = new File(appFolder, JSON_DIRECTORY_NAME);
        if (jsonFileFolder.listFiles() != null)
            for (File child : Objects.requireNonNull(jsonFileFolder.listFiles()))
                if (child.getName().contains("Pending")) {
                    child.delete();
                }
    }

    public static void deleteArtistFile(File appFolder, String name) {
        File jsonFileFolder = new File(appFolder, JSON_DIRECTORY_NAME);
        if (jsonFileFolder.listFiles() != null)
            for (File child : Objects.requireNonNull(jsonFileFolder.listFiles()))
                if (child.getName().contains(name) && child.getName().contains("artist")) {
                    if (!child.delete())
                        throw new OutOfMemoryError("artist file not deleted");
                }
    }


}
