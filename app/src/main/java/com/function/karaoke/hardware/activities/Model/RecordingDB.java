package com.function.karaoke.hardware.activities.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RecordingDB {

    private List<Recording> recordings;


    public RecordingDB(List<Recording> recordings) {
//        mRoot = root;
        this.recordings = recordings;
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

    private void recordingsUpdated(List<Recording> recordingList) {
        if (recordings != null) {
            recordings.clear();
        } else {
            recordings = new ArrayList<>();
        }
        recordings.addAll(recordingList);
        notifyUpdated();
    }

    public void updateSongs(List<Recording> recordings) {
        recordingsUpdated(recordings);
    }


}
