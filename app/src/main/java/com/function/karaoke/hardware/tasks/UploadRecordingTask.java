//package com.function.karaoke.hardware.tasks;
//
//import android.content.Context;
//import android.net.Uri;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.Lifecycle;
//import androidx.lifecycle.LifecycleOwner;
//import androidx.lifecycle.Observer;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import com.function.karaoke.hardware.storage.StorageAdder;
//
//public class UploadRecordingTask extends Worker implements LifecycleOwner {
//
//    private StorageAdder storageAdder;
//
//    public UploadRecordingTask(
//
//            @NonNull Context context,
//            @NonNull WorkerParameters params) {
//        super(context, params);
//    }
//
//    @Override
//    public Result doWork() {
//
//        String recordingUriInput = getInputData().getString("path");
//        if (recordingUriInput == null) {
//            return Result.failure();
//        }
//
//        // Do the work here--in this case, upload the images.
//        return uploadRecordings(recordingUriInput);
//
//        // Indicate whether the work finished successfully with the Result
//    }
//
//    private Result uploadRecordings(String path) {
//        storageAdder = new StorageAdder(Uri.parse(path));
//        boolean attempted = false;
//        final Observer<String> urlObserver = url -> {
//            if (url==null){
//
//            }
//        };
//        storageAdder.uploadVideo().observe(this, urlObserver);
//        while (!attempted){}
//
//    }
//
//    @NonNull
//    @Override
//    public Lifecycle getLifecycle() {
//        return null;
//    }
//}
