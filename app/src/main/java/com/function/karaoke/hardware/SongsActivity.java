package com.function.karaoke.hardware;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.function.karaoke.core.model.Song;
import com.function.karaoke.core.model.SongsDB;
import com.function.karaoke.hardware.ui.login.LoginActivity;

import java.util.Locale;

public class SongsActivity
        extends AppCompatActivity
        implements SongsListFragment.OnListFragmentInteractionListener {

    private static final int VIDEO_REQUEST = 1;
    private static final int CAMERA_CODE = 2;
    private static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 102;
    private SongsDB mSongs;
    public String language;
    Locale myLocale;
//    private CameraPreview cameraPreview;
    //    private SurfaceView surfaceView;
//    private SurfaceHolder surfaceHolder;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            mTextureView = findViewById(R.id.camera_place);
//            cameraPreview = new CameraPreview(mTextureView, SongsActivity.this);
//            cameraPreview.setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
//            cameraPreview.connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
//    private CameraDevice mCamera;
//    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(@NonNull CameraDevice cameraDevice) {
//            mCamera = cameraDevice;
//            startPreview();
//        }
//
//        @Override
//        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            cameraDevice.close();
//            mCamera = null;
//        }
//
//        @Override
//        public void onError(@NonNull CameraDevice cameraDevice, int i) {
//            cameraDevice.close();
//            mCamera = null;
//        }
//    };
//    private CaptureRequest.Builder mCaptureRequestBuilder;
//    private CameraManager mCameraManager;
//    private String cameraId;
//    private HandlerThread mBackgroundHandlerThread;
//    private Handler mBackgroundHandler;
//    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
//
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 0);
//        ORIENTATIONS.append(Surface.ROTATION_90, 90);
//        ORIENTATIONS.append(Surface.ROTATION_180, 180);
//        ORIENTATIONS.append(Surface.ROTATION_270, 270);
//    }
//
//    private static class CompareSizeByArea implements Comparator<Size> {
//
//        @Override
//        public int compare(Size lhs, Size rhs) {
//            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
//        }
//    }
//
//    private Size mPerviewSIze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongs = new SongsDB(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        mSongs.addSongs();
        showPromo();
        language = getResources().getConfiguration().locale.getDisplayLanguage();

//        setContentView(R.layout.activity_songs);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startBackgroundThread();
//        if (mTextureView.isAvailable()) {
//            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
//            connectCamera();
//        } else {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        }
    }

    private void showPromo() {
        setContentView(R.layout.promo);
        //todo set for when the app loads from the server
        setTimer();
    }

    private void setTimer() {
        new CountDownTimer(5000, 1) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                setContentView(R.layout.activity_songs);
                mTextureView = (TextureView) findViewById(R.id.camera_place);
//                cameraPreview = new CameraPreview(mTextureView, SongsActivity.this);
                String languageToDisplay = language.equals("English") ? "En" : "עב";
                ((TextView) findViewById(R.id.language)).setText(languageToDisplay);
            }
        }.start();
    }

    @Override
    public void onListFragmentInteraction(Song item) {
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, item.fullPath.toString());
        startActivity(intent);
    }

    @Override
    public SongsDB getSongs() {
        return mSongs;
    }

    public void openLogInActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void changeLanguage(View view) {
        if (language.equals("Hebrew")) {
            setLocale("en");
        } else {
            setLocale("iw");
        }
    }

//    public void openCamera(View view) {
//        if (checkCameraHardware(this)) {
//
//            //todo remove. this is for storing on external data
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, EXTERNAL_STORAGE_WRITE_PERMISSION);
//
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
//                } else {
//                    cameraPreview.startBackgroundThread();
//                    if (mTextureView.isAvailable()) {
//                        cameraPreview.setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
//                        cameraPreview.connectCamera();
//                    } else {
//                        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//                    }
//                }
//            }
//        }
//    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void setLocale(String lang) {

        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, SongsActivity.class);
        startActivity(refresh);
    }
//
//
//    @Override
//    public void addCallback(Callback callback) {
//
//    }
//
//    @Override
//    public void removeCallback(Callback callback) {
//
//    }
//
//    @Override
//    public boolean isCreating() {
//        return false;
//    }
//
//    @Override
//    public void setType(int i) {
//
//    }
//
//    @Override
//    public void setFixedSize(int i, int i1) {
//
//    }
//
//    @Override
//    public void setSizeFromLayout() {
//
//    }
//
//    @Override
//    public void setFormat(int i) {
//
//    }
//
//    @Override
//    public void setKeepScreenOn(boolean b) {
//
//    }
//
//    @Override
//    public Canvas lockCanvas() {
//        return null;
//    }
//
//    @Override
//    public Canvas lockCanvas(Rect rect) {
//        return null;
//    }
//
//    @Override
//    public void unlockCanvasAndPost(Canvas canvas) {
//
//    }
//
//    @Override
//    public Rect getSurfaceFrame() {
//        return null;
//    }
//
//    @Override
//    public Surface getSurface() {
//        return null;
//    }

//    private void closeCamera() {
//        if (mCamera != null) {
//            mCamera.close();
//            mCamera = null;
//        }
//    }

    @Override
    protected void onPause() {
//        cameraPreview.closeCamera();
//        cameraPreview.stopBackgroundThread();
        super.onPause();
    }

//    private void setupCamera(int width, int height) {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            for (String id : cameraManager.getCameraIdList()) {
//                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
//                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
//                    continue;
//                }
//                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
//                int totalRotation = sensitiveDeviceRotation(cameraCharacteristics, deviceOrientation);
//                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
//                int rotatedWidth = width;
//                int rotatedHeight = height;
//                if (swapRotation) {
//                    rotatedWidth = height;
//                    rotatedHeight = width;
//                }
//                mPerviewSIze = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
//                cameraId = id;
//                return;
//
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//
//    }

//    private void startBackgroundThread() {
//        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
//        mBackgroundHandlerThread.start();
//        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
//    }
//
//    private void stopBackgroundThread() {
//        mBackgroundHandlerThread.quitSafely();
//        try {
//            mBackgroundHandlerThread.join();
//            mBackgroundHandlerThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }

//    private static int sensitiveDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
//        int sensorOrientatoin = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
//        return (sensorOrientatoin + deviceOrientation + 360) % 360;
//    }
//
//    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
//        List<Size> bigEnough = new ArrayList<>();
//        for (Size option : choices) {
//            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width && option.getHeight() >= height) {
//                bigEnough.add(option);
//            }
//        }
//        if (bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new CompareSizeByArea());
//        } else {
//            return choices[0];
//        }
//    }

//    private void connectCamera() {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
//                    Toast.makeText(this, "Video enhances experience", Toast.LENGTH_SHORT).show();
//                }
//
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
//            } else {
//                cameraManager.openCamera(cameraId, mCameraStateCallback, mBackgroundHandler);
//
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_CODE:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
//                    cameraPreview.connectCamera();
                break;
            case EXTERNAL_STORAGE_WRITE_PERMISSION:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                }
        }
    }

//    private void startPreview() {
//        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//        surfaceTexture.setDefaultBufferSize(mPerviewSIze.getWidth(), mPerviewSIze.getHeight());
//        Surface previewSurface = new Surface(surfaceTexture);
//
//        try {
//            mCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mCaptureRequestBuilder.addTarget(previewSurface);
//
//            mCamera.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    try {
//
//                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(getApplicationContext(), "Unable to open camera", Toast.LENGTH_SHORT).show();
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
}
