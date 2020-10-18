package com.function.karaoke.hardware.tasks;

import android.os.AsyncTask;

import androidx.fragment.app.Fragment;

import com.function.karaoke.hardware.activities.Model.DatabaseSong;

public class NetworkTasks extends Fragment {
    public static final String TAG = "NetworkFragment";

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static ParseWordsTask parseWords(DatabaseSong dbSong, ParseListener parseListener) {
        ParseWordsTask parseWordsTask = new ParseWordsTask(parseListener);
        parseWordsTask.execute(dbSong);
        return parseWordsTask;
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
                    result = new ParseWordsTask.Result("Success");
                } catch (Exception e) {
                    result = new ParseWordsTask.Result(e);
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

    public interface ParseListener {
        void onSuccess();

        void onFail();
    }
}
