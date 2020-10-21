package com.function.karaoke.hardware.activities.Model;

import com.function.karaoke.hardware.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class RecordingDB {

    private List<Recording> recordings;
    private String recorderId;


    public RecordingDB(List<Recording> recordings) {
//        mRoot = root;
        this.recordings = recordings;
        recorderId = recordings.get(0).getRecorderId();
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

//    public void updateSongs(List<Recording> recordingList) {
//        for (Recording recording: recordingList){
//            if (recordingList.contains())
//        }
//        if (recordings != null) {
//            recordings.clear();
//        } else {
//            recordings = new ArrayList<>();
//        }
//        recordings.addAll(recordingList);
//        notifyUpdated();
//    }
//
//    public void deleteRecordings() {
//        recordings.clear();
//    }

    public void updateRecordings(List<Recording> recordingsList){
        recordings.addAll(recordingsList);
    }

}
