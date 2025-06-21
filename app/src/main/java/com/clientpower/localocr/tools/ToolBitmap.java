package com.clientpower.localocr.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ToolBitmap {

    /**
     * uri 文件地址 转成 bitmap 位图
     * */
    public static Bitmap uriToBitmap(Context ctx, Uri uri) {
        try {
            InputStream inputStream = ctx.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 旋转成为拍摄角度
     * */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        return Bitmap.createBitmap(
            bitmap,
            0, 0,
            bitmap.getWidth(),
            bitmap.getHeight(),
            matrix,
            true
        );
    }


    /**
     * bitmap 生成地址
     * */
    public static String saveBitmapToCache(Context context, Bitmap bitmap) {
        File file = new File(context.getCacheDir(), "temp_bitmap.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
