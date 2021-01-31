package com.function.karaoke.hardware.activities.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RecordingDB {

    private List<Recording> recordings;
    private HashMap<Reocording, List<Recording>> recordingsPerSong = new HashMap<>();
    private String recorderId;


    public RecordingDB(List<Recording> recordings) {
//        mRoot = root;
        this.recordings = recordings;
        if (recordings != null && recordings.size() > 0)
            createMap();
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

    public interface IListener {
        void onListUpdated();
    }

    public List<Recording> getRecordings() {
        return recordings;
    }

    private final HashSet<IListener> mListeners = new HashSet<>();

    public void subscribe(RecordingDB.IListener listener) {
        mListeners.add(listener);
    }

    private void notifyUpdated() {
        for (IListener l : new HashSet<>(mListeners))
            l.onListUpdated();
    }

    public void updateRecordings(List<Recording> recordingsList) {
        recordingsPerSong.put(recordingsList.get(0), recordingsList);
        notifyUpdated();
    }

    public void updateSingleRecordingAfterDeleting(List<Recording> recordings) {
        recordingsPerSong.put(recordings.get(0), recordings);
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
}
