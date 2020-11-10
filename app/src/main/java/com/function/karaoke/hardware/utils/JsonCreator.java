package com.function.karaoke.hardware.utils;

import android.net.Uri;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.activities.Model.Recording;
import com.function.karaoke.hardware.activities.Model.SaveItems;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class JsonCreator {

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

    public static void createJsonObject(String videoUriToString, Recording recording, String storageFilePath) {
        try {
            JSONObject saveItems = new JSONObject();
            saveItems.put("filePath", videoUriToString);
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

        Uri fileUri = Uri.parse(dbJsonRawObject.getString("filePath"));
        JSONObject recordingToAdd = dbJsonRawObject.getJSONObject("recording");
        DatabaseSong song = new DatabaseSong(recordingToAdd.getString("title"),
                recordingToAdd.getString("artist"),
                recordingToAdd.getString("imageFileUrl"),
                recordingToAdd.getString("audioFileUrl"));
        Recording recording = new Recording(song, recordingToAdd.getString("date"),
                recordingToAdd.getString("recorderId"),
                recordingToAdd.getString("recordingId"),
                recordingToAdd.getInt("delay"));
        return new SaveItems(fileUri, recording);
    }

}
