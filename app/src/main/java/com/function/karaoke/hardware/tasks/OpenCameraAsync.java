package com.function.karaoke.hardware.tasks;

import android.os.AsyncTask;
import android.view.TextureView;

import com.function.karaoke.hardware.utils.CameraPreview;

public class OpenCameraAsync {


    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static OpenCamera openCamera(CameraPreview cp, TextureView textureView,
                                        TextureView.SurfaceTextureListener surfaceTextureListener,
                                        OpenCameraListener listener) {
        OpenCamera openCamera = new OpenCamera(listener, textureView, surfaceTextureListener);
        openCamera.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cp);
        return openCamera;
    }


    public interface OpenCameraListener {
        void onSuccess();

        void onFail();

    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private static class OpenCamera extends AsyncTask<CameraPreview, TextureView, OpenCamera.Result> {

        private final TextureView mTextureView;
        private final TextureView.SurfaceTextureListener mSurfaceTextureListener;
        private OpenCameraListener listener;

        OpenCamera(OpenCameraListener listener, TextureView textureView, TextureView.SurfaceTextureListener surfaceTextureListener) {
            this.mTextureView = textureView;
            this.mSurfaceTextureListener = surfaceTextureListener;
            setListener(listener);
        }

        void setListener(OpenCameraListener listener) {
            this.listener = listener;
        }

        @Override
        protected OpenCamera.Result doInBackground(CameraPreview... drivers) {
            OpenCamera.Result result = null;
            if (!isCancelled() && drivers != null) {
                try {
                    drivers[0].startBackgroundThread();
                    if (mTextureView.isAvailable()) {
                        drivers[0].setTextureView(mTextureView);
                        drivers[0].setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                        drivers[0].connectCamera();
                    } else {
                        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                    }
                    result = new OpenCamera.Result("Success");
                } catch (Exception e) {
                    result = new OpenCamera.Result(e);
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
        protected void onPostExecute(OpenCamera.Result result) {
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
        protected void onCancelled(OpenCamera.Result result) {
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
