package com.clientpower.localocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.clientpower.localocr.databinding.ActivityCameraBinding;
import com.clientpower.localocr.tools.ToolBitmap;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class CameraActivity extends AppCompatActivity {

    ActivityCameraBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startCamera();

        binding.btnCapture.setOnClickListener((v) -> {
            try {
                takePhoto();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 创建 Preview 对象
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .build();

                // 设置 SurfaceProvider
                preview.setSurfaceProvider(binding.cameraPreviewView.getSurfaceProvider());

                // 选择后置摄像头
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 先解绑之前的 use cases
                cameraProvider.unbindAll();

                // 绑定新的 preview use case
                cameraProvider.bindToLifecycle(
                        this, // lifecycle owner
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e("OCR", "Camera initialization failed", e);
            } catch (Exception e) {
                Log.e("OCR", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private ImageCapture imageCapture;

    /**
     * 裁剪框内区域
     * */
    private Bitmap cropIdCardFromBitmap(Bitmap bitmap) {
        View frameView = binding.idCardFrame;
        View previewView = binding.cameraPreviewView;

        int[] location = new int[2];
        frameView.getLocationInWindow(location);
        int frameLeft = location[0];
        int frameTop = location[1];
        int frameWidth = frameView.getWidth();
        int frameHeight = frameView.getHeight();

        float scaleX = (float) bitmap.getWidth() / (float) previewView.getWidth();
        float scaleY = (float) bitmap.getHeight() / (float) previewView.getHeight();

        int cropLeft = (int) (frameLeft * scaleX);
        int cropTop = (int) (frameTop * scaleY);
        int cropWidth = (int) (frameWidth * scaleX);
        int cropHeight = (int) (frameHeight * scaleY);

        // 防止越界
        cropLeft = Math.max(0, cropLeft);
        cropTop = Math.max(0, cropTop);
        cropWidth = Math.min(cropWidth, bitmap.getWidth() - cropLeft);
        cropHeight = Math.min(cropHeight, bitmap.getHeight() - cropTop);

        return Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight);
    }

    /**
     * 拍照
     * */
    private void takePhoto() throws IOException {
        // 获取 ImageCapture 的引用
        if (imageCapture == null) {
            return;
        }

        // 创建带时间戳的文件名
        File photoFile = File.createTempFile("ocr",".jpg");

        // 创建输出选项对象
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // 拍照
        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        int r =  image.getImageInfo().getRotationDegrees();
                        Bitmap bm = ToolBitmap.rotateBitmap(image.toBitmap(),r);
                        image.close();
                        Bitmap cardBm = cropIdCardFromBitmap(bm);
                        String path = ToolBitmap.saveBitmapToCache(CameraActivity.this,cardBm);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("path", path);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                    }
                }
        );
//        imageCapture.takePicture(
//                outputOptions,
//                ContextCompat.getMainExecutor(this),
//                new ImageCapture.OnImageSavedCallback() {
//                    @Override
//                    public void onError(@NonNull ImageCaptureException exc) {
//                        Log.e("OCR", "Photo capture failed: " + exc.getMessage(), exc);
//                    }
//
//                    @Override
//                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
//                        Uri savedUri = Uri.fromFile(photoFile);
//                        String msg = "Photo capture succeeded: " + savedUri;
//                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
//                        Log.d("OCR", msg);
//
//                        // 获取区域
//                        Bitmap bm = ToolBitmap.uriToBitmap(CameraActivity.this,savedUri);
//                        Bitmap rbm = ToolBitmap.rotateBitmap(bm);
//                        Bitmap cbm = cropIdCardFromBitmap(bm);
//
//                        Intent resultIntent = new Intent();
//                        resultIntent.putExtra("bitmap", cbm);
//                        setResult(RESULT_OK, resultIntent);
//                        finish();
//
//                    }
//                }
//        );
    }


}