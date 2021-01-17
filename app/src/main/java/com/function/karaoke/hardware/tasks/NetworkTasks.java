package com.function.karaoke.hardware.tasks;

import android.os.AsyncTask;

import androidx.fragment.app.Fragment;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;
import com.function.karaoke.hardware.storage.RecordingDelete;
import com.function.karaoke.hardware.storage.StorageAdder;
//import com.function.karaoke.hardware.storage.StorageDriver;

public class NetworkTasks extends Fragment {

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static void parseWords(DatabaseSong dbSong, ParseListener parseListener) {
        ParseWordsTask parseWordsTask = new ParseWordsTask(parseListener);
        parseWordsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dbSong);
    }


    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private static class ParseWordsTask extends AsyncTask<DatabaseSong, Integer, ParseWordsTask.Result> {

        private ParseListener listener;

        ParseWordsTask(ParseListener listener) {
            setListener(listener);
        }

        void setListener(ParseListener listener) {
            this.listener = listener;
        }

        @Override
        protected ParseWordsTask.Result doInBackground(DatabaseSong... dbSongs) {
            ParseWordsTask.Result result = null;
            if (!isCancelled() && dbSongs != null) {
                try {
                    dbSongs[0].setLines();
                    result = new Result("Success");
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
            super.onPreExecute();
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(ParseWordsTask.Result result) {
            if (result != null && listener != null) {
                if (result.exception != null) {
                    listener.onFail();
                } else if (result.resultValue != null) {
                    listener.onSuccess();
                }
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(ParseWordsTask.Result result) {
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        private static class Result {
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

    public interface ParseListener {
        void onSuccess();

        void onFail();
    }

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static UploadToWasabi uploadToWasabi(StorageAdder driver, UploadToWasabiListener listener) {
        UploadToWasabi uploadToWasabi = new UploadToWasabi(listener);
        uploadToWasabi.execute(driver);
        return uploadToWasabi;
    }


    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private static class UploadToWasabi extends AsyncTask<StorageAdder, Integer, UploadToWasabi.Result> {

        private UploadToWasabiListener listener;

        UploadToWasabi(UploadToWasabiListener listener) {
            setListener(listener);
        }

        void setListener(UploadToWasabiListener listener) {
            this.listener = listener;
        }

        @Override
        protected UploadToWasabi.Result doInBackground(StorageAdder... drivers) {
            UploadToWasabi.Result result = null;
            if (!isCancelled() && drivers != null) {
                drivers[0].uploadFile(new StorageAdder.UploadProgressListener() {
                    @Override
                    public void progressUpdate(double progress) {
                        publishProgress((int) (100 * progress));
                    }
                });
                result = new UploadToWasabi.Result("Success");

            }
            return result;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            listener.onProgress(percent[0]);

        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(UploadToWasabi.Result result) {
            if (result != null && listener != null) {
                if (result.exception != null) {
                    listener.onFail();
                } else if (result.resultValue != null) {
                    listener.onSuccess();
                }
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(UploadToWasabi.Result result) {
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
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static DeleteFromWasabi deleteFromWasabi(RecordingDelete recordingDelete, DeleteListener listener) {
        DeleteFromWasabi deleteFromWasabi = new DeleteFromWasabi(listener);
        deleteFromWasabi.execute(recordingDelete);
        return deleteFromWasabi;
    }

    public interface UploadToWasabiListener {
        void onSuccess();

        void onFail();

        void onProgress(int progress);
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private static class DeleteFromWasabi extends AsyncTask<RecordingDelete, Integer, DeleteFromWasabi.Result> {

        private DeleteListener listener;

        DeleteFromWasabi(DeleteListener listener) {
            setListener(listener);
        }

        void setListener(DeleteListener listener) {
            this.listener = listener;
        }

        @Override
        protected DeleteFromWasabi.Result doInBackground(RecordingDelete... deletes) {
            DeleteFromWasabi.Result result = null;
            if (!isCancelled() && deletes != null) {
                deletes[0].deleteRecording();
                result = new DeleteFromWasabi.Result("Success");

            }
            return result;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(DeleteFromWasabi.Result result) {
            if (result != null && listener != null) {
                if (result.exception != null) {
                    listener.onFail();
                } else if (result.resultValue != null) {
                    listener.onSuccess();
                }
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(DeleteFromWasabi.Result result) {
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

    public interface DeleteListener {
        void onSuccess();

        void onFail();
    }

}
