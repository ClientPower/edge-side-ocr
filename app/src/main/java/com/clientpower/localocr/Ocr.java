package com.clientpower.localocr;

import static com.clientpower.localocr.tools.ToolBitmap.uriToBitmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.clientpower.localocr.databinding.ActivityOcrBinding;
import com.clientpower.localocr.tools.PermissionHelper;
import com.henry.henryocr.HOCR;
import com.henry.henryocr.IdCardParser;
import com.henry.henryocr.interfaces.HOCRInterface;

public class Ocr extends AppCompatActivity {
    private PermissionHelper permissionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        permissionManager = new PermissionHelper(Ocr.this);
        permissionManager.requestPermissions(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(Ocr.this, "所有权限已授予", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(String[] pps) {
                Toast.makeText(Ocr.this, "以下权限被拒绝: " + String.join(",",pps), Toast.LENGTH_SHORT).show();
            }
        });

        binding = ActivityOcrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        HOCR.getSingle().config(this);

        // 初始化 Launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri == null) return;
                        binding.orcShowImg.setImageURI(uri);
                        HOCR.getSingle().callResult(uriToBitmap(Ocr.this,uri), _result);
                    }
                }
        );

        binding.orcPaizhao.setOnClickListener((view) -> {
            Intent _binds = new Intent(Ocr.this, CameraActivity.class);
            resultLauncher.launch(_binds);
        });

        binding.orcSelectImg.setOnClickListener((view) -> {
            imagePickerLauncher.launch("image/*");
        });

        binding.orcRun.setOnClickListener((view) -> {


        });


    }

    private ActivityOcrBinding binding;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String path = data.getStringExtra("path");
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        HOCR.getSingle().callResult(bitmap,Ocr.this._result);
                        Log.d("Result", "返回的数据: " + path);
                    }
                }
            }
    );

    private HOCRInterface _result = new HOCRInterface() {
        @Override
        public void success(Bitmap resImg, IdCardParser.IdCardInfo info) {
            binding.orcShowImg.setImageBitmap(resImg);
            binding.orcShowInfo.setText(String.format(
                    "姓名: %s \n性别: %s \n民族: %s \n出生年月: %s \n身份证: %s\n住址: %s",
                    info.name,info.gender,info.nation,info.birth,info.idNumber,info.address
            ));
        }

        @Override
        public void fail(String errMsg) {
        }
    };

}