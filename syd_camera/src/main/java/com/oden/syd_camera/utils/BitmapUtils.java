package com.oden.syd_camera.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by syd on 2017/6/14.
 */

public class BitmapUtils {
    private static final String TAG = "SydCamera";

    /**
     * 根据相机旋转旋转角度将图片进行旋转
     * @param bm
     * @param cameraId
     * @param cameraOrientation
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bm, int cameraId, int cameraOrientation) {
        Matrix matrixs = new Matrix();
        int degree;

        if (cameraOrientation > 325 || cameraOrientation <= 45) {
            Log.i(TAG, "Surface.ROTATION_0:" + cameraOrientation);
            if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                degree = 90;
            }else{
                degree = -90;
            }
        } else if (cameraOrientation > 45 && cameraOrientation <= 135) {
            Log.i(TAG, " Surface.ROTATION_270:" + cameraOrientation);
            degree = 180;
        } else if (cameraOrientation > 135 && cameraOrientation < 225) {
            Log.i(TAG, "Surface.ROTATION_180:" + cameraOrientation);
            degree = 270;
        } else {
            Log.i(TAG, "Surface.ROTATION_90:" + cameraOrientation);
            degree = 0;
        }

        matrixs.setRotate(degree);
        Bitmap returnBm = null;

        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrixs, true);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "图片旋转操作失败!");
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 将图片进行旋转
     * @param bm
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bm, int orientation) {
        Matrix matrixs = new Matrix();
        matrixs.setRotate(orientation);
        Bitmap returnBm = null;

        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrixs, true);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "图片旋转操作失败!");
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 将bitmap存成文件
     * @param bitmap
     * @param path
     * @param quality
     */
    public static boolean saveBitmapToSd(Bitmap bitmap, String path, int quality) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
            Log.i(TAG, "图片已经保存!");
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "图片保存失败!");
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "图片保存失败!");
            return false;
        }
    }

    /**
     * byte[]转换成Bitmap
     * @param b
     * @return
     */
    public static Bitmap Bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    /**
     * 压缩到指定大小下
     * @param size 图片允许最大空间   单位：KB
     */
    public static Bitmap bitmapCompress(Bitmap bitmap, double size) {
        //将bitmap放至数组中，意在获取bitmap的大小
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        //将字节换成KB
        double mid = b.length/1024;
        //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
        if (mid > size) {
            //获取bitmap大小 是允许最大大小的多少倍
            double i = mid / size;
            //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
            bitmap = bitmapCompress(bitmap, bitmap.getWidth() / Math.sqrt(i),
                    bitmap.getHeight() / Math.sqrt(i));
        }
        return bitmap;
    }

    /**
     * 将图片压缩到制定宽高
     * @param bgimage
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap bitmapCompress(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @param size,单位KB，压缩到size之下
     * @return
     */
    public static Bitmap compressToSizeByQuality(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > size) {  //循环判断如果压缩后图片是否大于size kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
            Log.w(TAG, "options: " + options);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 获取压缩到指定大小之下所需的quality参数
     * @param image
     * @param size
     * @return
     */
    public static int getQualityBySize(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > size) {  //循环判断如果压缩后图片是否大于size kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 5;//每次都减少5
            Log.w(TAG, "options: " + options);
        }
        return options;
    }

}
