package com.function.karaoke.hardware.utils;

import android.Manifest;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
    private final Context context;
    //    private AudioRecorder audioRecorder;
    private AppCompatActivity activity;
    private TextureView mTextureView;
    private CameraDevice mCamera;
    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
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
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CameraManager mCameraManager;
    private String cameraId;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

//    private boolean cameraClosed = false;

    public File getVideo() {
        return mVideoFile;
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private Size mPreviewSize;
    private Size mVideoSize;
    private File mVideoFolder;
    private File mVideoFile;
    String fileName;
    private int mTotalRotation;
    private MediaRecorder mMediaRecorder;

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
        setupMediaRecorder();
    }

    private void createVideoFolder() {
//        File movieFile = activity.getCacheDir();
//        File movieFile = context.getFilesDir();
        mVideoFolder = new File(context.getFilesDir(), DIRECTORY_NAME);
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private void createVideoFileName() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMM_HHmmss").format(new Date());
        String prepend = "VIDEO" + timeStamp + "_";
        File videoFile = new File(mVideoFolder, prepend + ".mp4");
        fileName = videoFile.getAbsolutePath();
        mVideoFile = videoFile;
    }

    public void prepareMediaRecorder() {
        try {
            createVideoFileName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setMediaRecorder();
    }

    private void setupMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        if (mCamera != null) {
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);

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
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mMediaRecorder.setAudioChannels(1);
            mMediaRecorder.setAudioEncodingBitRate(12200);
            mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
            mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
            mMediaRecorder.setAudioSamplingRate(8000);
//        }
        mMediaRecorder.setOrientationHint(mTotalRotation);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//            audioRecorder.startRecorder();
    }

    private void setMediaRecorder() {
        try {
            setupMediaRecorder();
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
            e.printStackTrace();
        }

    }

    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                audioRecorder.stopRecording();
//                audioRecorder.deleteFile();
//            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pauseRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.pause();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//                audioRecorder.pauseRecorder();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resumeRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.resume();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//                audioRecorder.resumeRecording();
        }
    }

    public void start() {
        PrintWriter writer;
        try {
            writer = new PrintWriter(mVideoFile);
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (mVideoFile.length() < 1000) {
            mMediaRecorder.start();

        } else
            throw new RuntimeException("there is a problem with the video file   " + mVideoFile.length());

    }

}
