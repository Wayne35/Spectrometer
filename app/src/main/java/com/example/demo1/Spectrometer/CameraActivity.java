package com.example.demo1.Spectrometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.camera2.impl.Camera2CaptureRequestBuilder;
//import androidx.camera.core.CameraCaptureResult;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.biometrics.BiometricManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.example.demo1.R;

public class CameraActivity extends AppCompatActivity {

    TextureView textureView;
    TextureView.SurfaceTextureListener surfaceTextureListener;
    CameraManager cameraManager;
    CameraCharacteristics mCameraCharacteristics; // ????????????
    CameraDevice.StateCallback cam_stateCallback;
    CameraDevice opened_camera;
    Surface texture_surface;
    CameraCaptureSession.StateCallback cam_capture_session_stateCallback;
    CameraCaptureSession.CaptureCallback still_capture_callback;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest.Builder requestBuilder;
    CaptureRequest.Builder requestBuilder_image_reader;
    ImageReader imageReader;
    Surface imageReaderSurface;
    Bitmap bitmap;
    CaptureRequest request;
    Handler mBackgroundHandler;
    Button takePhoto_btn,savePhoto_btn,plus_btn,minus_btn,zoomIn_btn,zoomOut_btn;
    ImageView takePhoto_imageView;
    TextView ISO;
    private int iso = 50;
    private float zoom = 0.6f;
    private int mCameraId = CameraCharacteristics.LENS_FACING_FRONT;
    //??????????????????????????????
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        textureView = findViewById(R.id.texture_view_camera2);
        takePhoto_btn = findViewById(R.id.btn_camera2_takePhoto);
        savePhoto_btn = findViewById(R.id.btn_camera2_savePhoto);
        takePhoto_imageView = findViewById(R.id.image_view_preview_image);
        plus_btn = findViewById(R.id.btn_plus);
        minus_btn = findViewById(R.id.btn_minus);
        zoomIn_btn = findViewById(R.id.btn_zoomIn);
        zoomOut_btn = findViewById(R.id.btn_zoomOut);
        ISO = findViewById(R.id.iso);
        takePhoto_imageView.setDrawingCacheEnabled(true);
        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                texture_surface=new Surface(textureView.getSurfaceTexture());
                openCamera();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        };
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        //B1. ????????????????????????ImageReader
        imageReader = ImageReader.newInstance(1000  ,1000, ImageFormat.JPEG,1);
        //B2. ?????????????????????ImageReader??????????????????????????????
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                //B2.1 ??????????????????ImageReader?????????????????????????????????Bitmap
                Image image= reader.acquireLatestImage();
                ByteBuffer buffer= image.getPlanes()[0].getBuffer();
                int length= buffer.remaining();
                byte[] bytes= new byte[length];
                buffer.get(bytes);
                image.close();
                bitmap = BitmapFactory.decodeByteArray(bytes,0,length);
                //B2.2 ????????????
                takePhoto_imageView.setImageBitmap(bitmap);
            }
        },null);
        //B3 ???????????????ImageReader???Surface
        imageReaderSurface = imageReader.getSurface();

        //B4. ??????????????????
        takePhoto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //B4.1 ??????request????????? ????????????(??????????????????????????????????????? opened_camera???????????????????????????
                try {
                    requestBuilder_image_reader = opened_camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                requestBuilder_image_reader.set(CaptureRequest.JPEG_ORIENTATION,90);
                requestBuilder_image_reader.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                //B4.2 ??????request????????? ???????????????
                requestBuilder_image_reader.addTarget(imageReaderSurface );
                try {
                    //B4.3 ????????????
                    cameraCaptureSession.capture(requestBuilder_image_reader.build(),null,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        savePhoto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToSystemGallery(bitmap);
            }
        });
        plus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((iso+5)<=100){
                    iso = iso + 5;
                    ISO.setText(""+iso);
                    requestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,50+(iso*30));
                    request = requestBuilder.build();
                    startPreview();
                }else{
                   // Toast.makeText(CameraActivity.this,"???????????????0~100??????",Toast.LENGTH_SHORT).show();
                }
            }
        });
        minus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((iso-5)>=0){
                    iso = iso - 5;
                    ISO.setText(""+iso);
                    requestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,50+(iso*30));
                    request = requestBuilder.build();
                    startPreview();
                }else{
                    //Toast.makeText(CameraActivity.this,"???????????????0~100??????",Toast.LENGTH_SHORT).show();
                }
            }
        });
        zoomIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zoom <= 0.9f){
                zoom += 0.1f;
                applyZoom(zoom);}
            }
        });
        zoomOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zoom >= 0.1f){
                zoom -= 0.1f;
                applyZoom(zoom);}
            }
        });
    }
    public void saveToSystemGallery(Bitmap bmp) {
        // ??????????????????
        File fileDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(fileDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ????????????????????????????????????
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // ????????????????????????
//        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        Uri uri = Uri.fromFile(file);
//        intent.setData(uri);
//        sendBroadcast(intent);
//        MediaScannerConnection.scanFile(CameraActivity.this,new String[]{file.getAbsolutePath()},null,null);
        //????????????????????????????????????
        Toast.makeText(this, "?????????????????????" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }

    private void openCamera() {
        cameraManager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);  // ?????????
        cam_stateCallback=new CameraDevice.StateCallback() {
            @Override
    public void onOpened(@NonNull CameraDevice camera) {
                opened_camera=camera;
                try {
                    mCameraCharacteristics = cameraManager.getCameraCharacteristics(Integer.toString(mCameraId));
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                try {
                    requestBuilder = opened_camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    requestBuilder.set(CaptureRequest.CONTROL_MODE,0);
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,0);
                    //requestBuilder.set(CaptureRequest.CONTROL_AWB_MODE,3);
                    requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,0);
                    requestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,50+(iso*30));
                    requestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,(long)200000);
                    applyZoom(zoom);
                    requestBuilder.addTarget(texture_surface);
                    request = requestBuilder.build();
//                    Range range1 = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
//                    Range range2 = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
//                    Log.d("????????????????????????",""+range1);
//                    Log.d("???????????????????????????",""+range2);
                    ISO.setText(""+iso);
                    createCameraPreviewSession();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    Toast.makeText(CameraActivity.this, "try??????", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Toast.makeText(CameraActivity.this, "????????????", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Toast.makeText(CameraActivity.this, "????????????", Toast.LENGTH_SHORT).show();
            }
        };
        checkPermission();
        try {
            cameraManager.openCamera(cameraManager.getCameraIdList()[0],cam_stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() throws CameraAccessException {
        opened_camera.createCaptureSession( Arrays.asList(texture_surface,imageReaderSurface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                //Toast.makeText(CameraActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                cameraCaptureSession = session;
                try {
                    session.setRepeatingRequest(request,null,null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Toast.makeText(CameraActivity.this, "????????????", Toast.LENGTH_SHORT).show();
            }
        },null);
    }

    private void checkPermission() {
        // ???????????????????????????
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){

            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // ?????? textureView??????????????????????????????
        if(textureView.isAvailable()){
            openCamera();
        }else{
            // ??????????????????????????????????????????
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }
    @Override
    protected void onPause() {
        // ???????????????session??????
        if(cameraCaptureSession!=null){
            cameraCaptureSession.close();
        }
        // ???????????????
        if(null!=opened_camera){
            opened_camera.close();
        }
        // ????????????ImageReader
        if(null!=imageReader){
            imageReader.close();
        }
        // ???????????????View?????????
        super.onPause();
    }
    public void startPreview() {
        if (cameraCaptureSession == null || requestBuilder == null) {
            return;
        }
        try {
            // ?????????????????????????????????????????????
            cameraCaptureSession.setRepeatingRequest(request, null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void applyZoom(float zoom) {
        float mZoomValue = zoom;
        if(mCameraCharacteristics != null){
            float maxZoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            // converting 0.0f-1.0f zoom scale to the actual camera digital zoom scale
            // (which will be for example, 1.0-10.0)
            float calculatedZoom = (mZoomValue * (maxZoom - 1.0f)) + 1.0f;
            Rect newRect = getZoomRect(calculatedZoom, maxZoom);
            requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, newRect);
            request = requestBuilder.build();
            startPreview();
        }
    }

    private Rect getZoomRect(float zoomLevel, float maxDigitalZoom) {

        Rect activeRect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        int minW = (int) (activeRect.width() / maxDigitalZoom);
        int minH = (int) (activeRect.height() / maxDigitalZoom);
        int difW = activeRect.width() - minW;
        int difH = activeRect.height() - minH;

        // When zoom is 1, we want to return new Rect(0, 0, width, height).
        // When zoom is maxZoom, we want to return a centered rect with minW and minH
        int cropW = (int) (difW * (zoomLevel - 1) / (maxDigitalZoom - 1) / 2F);
        int cropH = (int) (difH * (zoomLevel - 1) / (maxDigitalZoom - 1) / 2F);
        return new Rect(cropW, cropH, activeRect.width() - cropW,
                 activeRect.height() - cropH);
    }
}