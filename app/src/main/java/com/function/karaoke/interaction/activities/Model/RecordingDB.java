package com.function.karaoke.interaction.activities.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RecordingDB {

    private final HashSet<IListener> mListeners = new HashSet<>();
    private List<Recording> recordings = new ArrayList<>();
    private final HashMap<Reocording, List<Recording>> recordingsPerSong = new HashMap<>();
    private String recorderId;


    public RecordingDB() {
    }

    public RecordingDB(List<Recording> recordings) {
//        mRoot = root;
        this.recordings = recordings;
        if (recordings != null && recordings.size() > 0)
            createMap();
        if (recordings != null && recordings.size() > 0)
            recorderId = recordings.get(0).getRecorderId();
    }

    private void createMap() {
        for (Recording recording : recordings) {
            List<Recording> r;
            if (recordingsPerSong.isEmpty() || !recordingsPerSong.containsKey(recording)) {
                r = new ArrayList<>();
            } else {
                r = recordingsPerSong.get(recording);
                if (r == null)
                    r = new ArrayList<>();
            }
            r.add(recording);
            recordingsPerSong.put(recording, r);
        }
    }

    public String getRecorderId() {
        return recorderId;
    }

    public void changeSongsAfterDelete(List<Recording> currentRecordings) {
        recordingsPerSong.put(currentRecordings.get(0), currentRecordings);
        notifyUpdated();
    }

    public List<Recording> getRecordings() {
        return recordings;
    }

    public void subscribe(RecordingDB.IListener listener) {
        mListeners.add(listener);
    }

    private void notifyUpdated() {
        for (IListener l : new HashSet<>(mListeners))
            l.onListUpdated();
    }

    public void updateRecordings(List<Recording> recordingsList) {
//        recordings.remove(recordingsList.get(0));
        recordings.clear();
        recordingsPerSong.clear();
//        recordingsPerSong.put(recordingsList.get(0), recordingsList);
        recordings.addAll(recordingsList);
        createMap();
        notifyUpdated();
    }

    public void updateSingleRecordingAfterDeleting(List<Recording> recordings) {
        recordingsPerSong.put(recordings.get(0), recordings);
    }

    public void addRecordings(List<Recording> recordings) {
        boolean updated = false;
        for (Recording recording : recordings)
            for (Recording recording1 : this.recordings)
                if (recording.getDate().equalsIgnoreCase(recording1.getDate()) && recording.equals(recording1))
                    break;
                else {
                    this.recordings.add(recording);
                    updated = true;
                }
        if (updated) {
            createMap();
            notifyUpdated();
        }
    }

    public void removeDeletedRecordings(List<Recording> deletedRecordings) {
        List<Recording> newList = new ArrayList<>();
        for (Recording deletedRecording : deletedRecordings)
            for (Recording recording : recordings) {
                if (!(recording.equals(deletedRecording) && recording.getDate().equalsIgnoreCase(deletedRecording.getDate())))
                    newList.add(recording);
            }
        recordings = newList;
    }

    public HashMap<Reocording, List<Recording>> getRecordingsPerSong() {
        return recordingsPerSong;
    }

    public interface IListener {
        void onListUpdated();
    }
}
