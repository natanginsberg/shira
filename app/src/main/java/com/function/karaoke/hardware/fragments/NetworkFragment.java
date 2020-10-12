package com.function.karaoke.hardware.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import com.function.karaoke.hardware.DownloadCallback;
import com.function.karaoke.hardware.ShortDynamicLinkCreator;
import com.function.karaoke.hardware.storage.StorageDriver;
import com.function.karaoke.hardware.utils.UrlHolder;
import com.function.karaoke.hardware.Testing.MergeTake2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class NetworkFragment extends Fragment implements LifecycleOwner {
    public static final String TAG = "NetworkFragment";

    private static final String URL_KEY = "UrlKey";
    private static final String STORAGE_DRIVER = "Storage";
    private static final String URL_PARSER = "Url";
    private static final String MERGER = "merger";

    private static final int DOWNLOAD_WORDS = 100;
    private static final int GET_COVER_IMAGE = 101;
    private static final int GET_AUDIO = 102;
    private static final String DYNAMIC_LINK_CREATOR = "linkCreator";


    private DownloadCallback<String> callback;
    private DownloadCallback<Uri> linkCallback;
    private UploadTask uploadTask;
    private DownloadTask downloadTask;
    private MergeTask merger;
    private UrlHolder urlParser;
    private ShortDynamicLinkCreator shortDynamicLinkCreator;
    private String urlString;
    private StorageDriver storageDriver;
    private int taskToOperate;
    private boolean finishedParsing = false;
    private MergeTake2 fileMerger;

//    /**
//     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
//     * from.
//     */
//    public static NetworkFragment getInstance(FragmentManager fragmentManager, String url) {
//        NetworkFragment networkFragment = new NetworkFragment();
//        Bundle args = new Bundle();
//        args.putString(URL_KEY, url);
//        networkFragment.setArguments(args);
//        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
//        return networkFragment;
//    }

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getUploadInstance(FragmentManager fragmentManager, StorageDriver storageDriver) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putSerializable(STORAGE_DRIVER, storageDriver);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        return networkFragment;
    }

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getDownloadInstance(FragmentManager fragmentManager, UrlHolder urlParser) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putSerializable(URL_PARSER, urlParser);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        return networkFragment;
    }

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getMergerInstance(FragmentManager fragmentManager, MergeTake2 merger) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putSerializable(MERGER, merger);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        return networkFragment;
    }

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getLinkingInstance(FragmentManager fragmentManager, ShortDynamicLinkCreator shortDynamicLinkCreator) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putSerializable(DYNAMIC_LINK_CREATOR, shortDynamicLinkCreator);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        return networkFragment;
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        urlString = getArguments().getString(URL_KEY);
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(STORAGE_DRIVER)) {
            storageDriver = (StorageDriver) getArguments().getSerializable(STORAGE_DRIVER);
        } else if (getArguments().containsKey(MERGER)) {
            fileMerger = (MergeTake2) getArguments().getSerializable(MERGER);
        } else
//            if (getArguments().containsKey(URL_PARSER)){
            urlParser = (UrlHolder) getArguments().getSerializable(URL_PARSER);
//        } else {
//            shortDynamicLinkCreator = (ShortDynamicLinkCreator)getArguments().getSerializable(DYNAMIC_LINK_CREATOR);
//        }
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Host Activity will handle callbacks from task.
//        if (urlParser == null) {
//            linkCallback = (DownloadCallback<Uri>) context;
//        } else {
            callback = (DownloadCallback<String>) context;
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity to avoid memory leak.
        callback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelDownload();
        super.onDestroy();
    }


    /**
     * Start non-blocking execution of DownloadTask.
     */
    public void startUpload() {

        cancelUpload();
        uploadTask = new UploadTask(callback);
        uploadTask.execute(storageDriver);
    }

    public boolean startDownload() {
        cancelDownload();
        downloadTask = new DownloadTask(callback);
        downloadTask.execute(urlParser);
        return true;
    }

    public void startMerge() {
        cancelMerge();
        merger = new MergeTask(callback);
        merger.execute(urlParser);
    }

    private void cancelMerge() {
        if (merger != null) {
            merger.cancel(true);
        }
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    public void cancelUpload() {
        if (uploadTask != null) {
            uploadTask.cancel(true);
        }
    }

    public void cancelDownload() {
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
    }

    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    private String downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
//            publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
//            publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
            if (stream != null) {
                // Converts Stream to String with max length of 500.
//                result = readStream(stream, 500);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private class UploadTask extends AsyncTask<StorageDriver, Integer, UploadTask.Result> {

        private DownloadCallback<String> callback;

        UploadTask(DownloadCallback<String> callback) {
            setCallback(callback);
        }

        void setCallback(DownloadCallback<String> callback) {
            this.callback = callback;
        }

        @Override
        protected Result doInBackground(StorageDriver... storageDrivers) {
            Result result = null;
            if (!isCancelled() && storageDrivers != null) {
                try {
                    storageDrivers[0].tryAndConnect();
                } catch (Exception e) {
                    result = new Result(e);
                }
            }
            return result;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (callback != null) {
                NetworkInfo networkInfo = callback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    callback.updateFromDownload(null);
                    cancel(true);
                }
            }
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        private class Result {
            public String resultValue;
            public Exception exception;

            public Result(String resultValue) {
                this.resultValue = resultValue;
            }

            public Result(Exception exception) {
                this.exception = exception;
            }
        }
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private class DownloadTask extends AsyncTask<UrlHolder, Integer, DownloadTask.Result> {

        private DownloadCallback<String> callback;

        DownloadTask(DownloadCallback<String> callback) {
            setCallback(callback);
        }

        void setCallback(DownloadCallback<String> callback) {
            this.callback = callback;
        }

        @Override
        protected DownloadTask.Result doInBackground(UrlHolder... urlParsers) {
            DownloadTask.Result result = null;
            if (!isCancelled() && urlParsers != null) {
                try {
                    urlParsers[0].parseSongWords();
                    result = new Result("Success");
                } catch (Exception e) {
                    result = new DownloadTask.Result(e);
                }
            }
            return result;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (callback != null) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    callback.updateFromDownload(null);
                    cancel(true);
                }
            }
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && callback != null) {
                if (result.exception != null) {
                    callback.updateFromDownload(result.exception.getMessage());
                } else if (result.resultValue != null) {
                    callback.updateFromDownload(result.resultValue);
                }
                callback.finishDownloading();
            }
        }

//        /**
//         * Defines work to perform on the background thread.
//         */
//        @Override
//        protected DownloadTask.Result doInBackground(String... urls) {
//            Result result = null;
//            if (!isCancelled() && urls != null && urls.length > 0) {
//                String urlString = urls[0];
//                try {
//                    URL url = new URL(urlString);
//                    String resultString = downloadUrl(url);
//                    if (resultString != null) {
//                        result = new Result(resultString);
//                    } else {
//                        throw new IOException("No response received.");
//                    }
//                } catch (Exception e) {
//                    result = new Result(e);
//                }
//            }
//            return result;
//        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        private class Result {
            public String resultValue;
            public Exception exception;

            public Result(String resultValue) {
                this.resultValue = resultValue;
            }

            public Result(Exception exception) {
                this.exception = exception;
            }
        }
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private class MergeTask extends AsyncTask<UrlHolder, Integer, MergeTask.Result> {

        private DownloadCallback<String> callback;

        MergeTask(DownloadCallback<String> callback) {
            setCallback(callback);
        }

        void setCallback(DownloadCallback<String> callback) {
            this.callback = callback;
        }

        @Override
        protected MergeTask.Result doInBackground(UrlHolder... urlParsers) {
            MergeTask.Result result = null;
            if (!isCancelled() && urlParsers != null) {
                try {
                    fileMerger.SSoftAudCombine();
                } catch (Exception e) {
                    result = new MergeTask.Result(e);
                }
            }
            return result;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (callback != null) {
                NetworkInfo networkInfo = callback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    callback.updateFromDownload(null);
                    cancel(true);
                }
            }
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && callback != null) {
                if (result.exception != null) {
                    callback.updateFromDownload(result.exception.getMessage());
                } else if (result.resultValue != null) {
                    callback.updateFromDownload(result.resultValue);
                }
                callback.finishDownloading();
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(MergeTask.Result result) {
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        private class Result {
            public String resultValue;
            public Exception exception;

            public Result(String resultValue) {
                this.resultValue = resultValue;
            }

            public Result(Exception exception) {
                this.exception = exception;
            }
        }
    }


    /**
         * Implementation of AsyncTask designed to fetch data from the network.
         */
        private class ShortLinkTask extends AsyncTask<UrlHolder, Integer, ShortLinkTask.Result> {

            private DownloadCallback<String> callback;

            ShortLinkTask(DownloadCallback<String> callback) {
                setCallback(callback);
            }

            void setCallback(DownloadCallback<String> callback) {
                this.callback = callback;
            }

            @Override
            protected ShortLinkTask.Result doInBackground(UrlHolder... urlParsers) {
                ShortLinkTask.Result result = null;
                if (!isCancelled() && urlParsers != null) {
                    try {
                        fileMerger.SSoftAudCombine();
                    } catch (Exception e) {
                        result = new ShortLinkTask.Result(e);
                    }
                }
                return result;
            }

            /**
             * Cancel background network operation if we do not have network connectivity.
             */
            @Override
            protected void onPreExecute() {
                if (callback != null) {
                    NetworkInfo networkInfo = callback.getActiveNetworkInfo();
                    if (networkInfo == null || !networkInfo.isConnected() ||
                            (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                    && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                        // If no connectivity, cancel task and update Callback with null data.
                        callback.updateFromDownload(null);
                        cancel(true);
                    }
                }
            }

            /**
             * Updates the DownloadCallback with the result.
             */
            @Override
            protected void onPostExecute(Result result) {
                if (result != null && callback != null) {
                    if (result.exception != null) {
                        callback.updateFromDownload(result.exception.getMessage());
                    } else if (result.resultValue != null) {
                        callback.updateFromDownload(result.resultValue);
                    }
                    callback.finishDownloading();
                }
            }


//        /**
//         * Defines work to perform on the background thread.
//         */
//        @Override
//        protected DownloadTask.Result doInBackground(String... urls) {
//            Result result = null;
//            if (!isCancelled() && urls != null && urls.length > 0) {
//                String urlString = urls[0];
//                try {
//                    URL url = new URL(urlString);
//                    String resultString = downloadUrl(url);
//                    if (resultString != null) {
//                        result = new Result(resultString);
//                    } else {
//                        throw new IOException("No response received.");
//                    }
//                } catch (Exception e) {
//                    result = new Result(e);
//                }
//            }
//            return result;
//        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        private class Result {
            public String resultValue;
            public Exception exception;

            public Result(String resultValue) {
                this.resultValue = resultValue;
            }

            public Result(Exception exception) {
                this.exception = exception;
            }
        }
    }
}
