package com.rash1k.camera2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //    region
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "MainActivity";
    private TextureView txvOut;
    private Button btnTakePicture;
    private String cameraID;
    private Size cameraSize;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;

    HandlerThread mHandlerThread;
    Handler mHandler = new Handler();

    CaptureRequest.Builder mCaptureRequestBuilder;
//  regionend

    TextureView.SurfaceTextureListener textListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
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

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            MainActivity.this.cameraDevice = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {

        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {

        }
    };

    private void createCameraPreview() {
        SurfaceTexture surfaceTexture = txvOut.getSurfaceTexture();
        if (surfaceTexture == null) {
            return;
        } else {
            surfaceTexture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
            Surface surface = new Surface(surfaceTexture);

            try {
                mCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                mCaptureRequestBuilder.addTarget(surface);
                cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                        if (cameraDevice == null) {
                            return;
                        }
                        MainActivity.this.cameraCaptureSession = cameraCaptureSession;
                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                    }
                }, null);

                cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private void openCamera() {
        CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            cameraID = cm.getCameraIdList()[0];
            Log.d(TAG, "openCamera: " + cameraID);
            CameraCharacteristics cc = cm.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map != null) {
                cameraSize = map.getOutputSizes(SurfaceTexture.class)[0];

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);

                }
                cm.openCamera(cameraID, stateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePicture = (Button) findViewById(R.id.btnTakePicture);
        txvOut = (TextureView) findViewById(R.id.txvOut);

        if (txvOut != null) {
            txvOut.setSurfaceTextureListener(textListener);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandlerThread = new HandlerThread("Camera");
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            }
        }
    }
}
