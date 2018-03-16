package com.oden.syd_camera.crop;

import android.graphics.Bitmap;

/**
 * Created by syd on 2017/6/16.
 */

public class CropParaUtil {
    public static final int REQUEST_CODE_FROM_CUTTING = 1601;
    public static final int REQUEST_CODE_FROM_CUTTING_FAIL = 1602;

    //裁剪后的图片，文件存储失败的情况下存入croppedBitmap
    public static Bitmap croppedBitmap;

    public static final String CROP_FILE_PATH = "sydCrop";
    public static String cropQuality = "cropQuality";
    public static String cropTitle = "cropTitle";
    public static String cropDestPicPath = "cropDestPicPath";
    public static String cropSrcPicPath = "cropSrcPicPath";


    //待增加
    public static String cropShowGuideLine = "cropShowGuideLine";

    public static int defaultPicQuality = 80; //图片质量，0~100

}
