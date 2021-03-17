package com.function.karaoke.interaction.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A basic Camera preview class
 */
public class CameraPreview {

    private static final String DIRECTORY_NAME = "camera2videoImageNew";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private final Context context;
    //    private AudioRecorder audioRecorder;
    private final AppCompatActivity activity;
    String fileName;
    private TextureView mTextureView;
    private CameraDevice mCamera;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private String cameraId;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    //    private boolean cameraClosed = false;
    private Size mPreviewSize;
    private final CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
//            if (!cameraClosed) {

            mCamera = cameraDevice;
            startPreview();
//            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCamera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCamera = null;
        }
    };
    private Size mVideoSize;
    private File mVideoFolder;
    private File mVideoFile;
    private int mTotalRotation;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private CameraErrorListener cameraErrorListener;

    public CameraPreview(AppCompatActivity activity, Context context) {
//        if (hasCamera)
//            mTextureView = textureView;
        this.activity = activity;
        this.context = context;
        createVideoFolder();
        mMediaRecorder = new MediaRecorder();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//            audioRecorder = new AudioRecorder(context);

    }

    private static int sensitiveDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getHeight() < option.getWidth() * height / width && option.getWidth() < width && option.getHeight() < height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    public File getVideo() {
        return mVideoFile;
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.close();
            stopBackgroundThread();
//            cameraClosed = true;
            mCamera = null;
        }
    }

    public void setTextureView(TextureView textureView) {
        this.mTextureView = textureView;
    }

    public void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = activity.getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensitiveDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;

                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                cameraId = id;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImageNew");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void connectCamera() {
//        cameraClosed = false;
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, mCameraStateCallback, mBackgroundHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCamera.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(activity.getApplicationContext(), "Unable to open camera", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void initiateRecorder() {
        //            createVideoFolder();
//            createVideoFileName();
        setupMediaRecorder(false);
    }

    private void createVideoFolder() {
//        File movieFile = activity.getCacheDir();
//        File movieFile = context.getFilesDir();
        mVideoFolder = new File(context.getCacheDir(), DIRECTORY_NAME);
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private void createVideoFileName() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMM_HHmmss").format(new Date());
        String prepend = "VIDEO" + timeStamp + "_";
        File videoFile = new File(mVideoFolder, prepend + ".mp4");
        fileName = videoFile.getAbsolutePath();
        mVideoFile = videoFile;
    }

    public void prepareMediaRecorder() {
        createVideoFileName();
        setMediaRecorder();
    }

    private void setupMediaRecorder(boolean retry) {
        try {
            mMediaRecorder = new MediaRecorder();
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            if (mCamera != null) {
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            }

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            if (!retry && (Util.SDK_INT < 29 || (AcousticEchoCanceler.isAvailable()
                    && AutomaticGainControl.isAvailable() && NoiseSuppressor.isAvailable())))
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            else {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            }
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(fileName);
//        mMediaRecorder.setProfile(profile);
            if (mCamera != null) {
                mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
                mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
                mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            }
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB); // balanced quality and speed encoder
            mMediaRecorder.setAudioEncodingBitRate(256000); // maximum encoding bitrate

            mMediaRecorder.setAudioChannels(1);
            mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
            mMediaRecorder.setOrientationHint(mTotalRotation);


            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecording = true;
        } catch (Exception e) {
            if (retry) {
                cameraErrorListener.recorderError();
            } else
                setupMediaRecorder(true);
        }

    }


    private void setMediaRecorder() {
        try {
            setupMediaRecorder(false);
            if (mCamera != null) {
                SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                Surface previewSurface = new Surface(surfaceTexture);
                Surface recordSurface = mMediaRecorder.getSurface();
                mCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                mCaptureRequestBuilder.addTarget(previewSurface);
                mCaptureRequestBuilder.addTarget(recordSurface);

                mCamera.createCaptureSession(Arrays.asList(previewSurface, recordSurface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        try {
                            cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    }
                }, null);
            }
        } catch (Exception e) {
            cameraErrorListener.cameraError();
        }

    }

    public boolean stopRecording() {
        if (mMediaRecorder != null) {
            if (isRecording)
                mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            isRecording = false;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                audioRecorder.stopRecording();
//                audioRecorder.deleteFile();
//            }
        }
        return isRecording;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean pauseRecording() {
        if (mMediaRecorder != null) {
            if (isRecording) {
                mMediaRecorder.pause();
                return true;
            } else
                return false;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//                audioRecorder.pauseRecorder();

        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resumeRecording() {
        if (mMediaRecorder != null) {
            if (isRecording)
                mMediaRecorder.resume();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//                audioRecorder.resumeRecording();
        }
    }

//    public void start() {
//        PrintWriter writer;
//        try {
//            writer = new PrintWriter(mVideoFile);
//            writer.print("");
//            writer.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        if (mVideoFile.length() < 1000) {
//            mMediaRecorder.start();
//
//        } else
//            throw new RuntimeException("there is a problem with the video file   " + mVideoFile.length());
//    }

    public long getTimeCreated() {
        return 0;
    }

    public void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void subscribeErrorListener(CameraErrorListener cameraErrorListener) {
        this.cameraErrorListener = cameraErrorListener;
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    public interface CameraErrorListener {
        void cameraError();

        void recorderError();
    }

}
