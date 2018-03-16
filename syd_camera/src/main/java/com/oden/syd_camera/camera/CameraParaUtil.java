package com.oden.syd_camera.camera;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by syd on 2017/5/20.
 三星A8支持的尺寸:
 pictureSizes:width = 4608 height = 3456
 pictureSizes:width = 4608 height = 2592
 pictureSizes:width = 3264 height = 2448
 pictureSizes:width = 3264 height = 1836
 pictureSizes:width = 2560 height = 1920
 pictureSizes:width = 2048 height = 1536
 pictureSizes:width = 2048 height = 1152
 pictureSizes:width = 1920 height = 1080
 pictureSizes:width = 1280 height = 960
 pictureSizes:width = 1280 height = 720
 pictureSizes:width = 640 height = 480
 */

public class CameraParaUtil {
    private static final String TAG = "SydCamera";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CameraParaUtil myCamPara = null;

    public static final String CAMERA_FILE_PATH = "sydPhoto";
    public static final String VIDEO_FILE_PATH = "sydVideo";
    public static final int REQUEST_CODE_FROM_CAMERA = 1501;
    public static final int REQUEST_CODE_FROM_CAMERA_FAIL = 1502;
    public static final int REQUEST_CODE_FROM_VIDEO = 1503;
    public static final int REQUEST_CODE_FROM_VIDEO_FAIL = 1504;

    //拍照后的图片，文件存储失败的情况下存入pictureBitmap，裁剪时取不到本地文件则也作为图片裁剪的来源
    public static Bitmap pictureBitmap;

    public static String picQuality = "picQuality";
    public static String picWidth = "picWidth";
    public static String picDuration = "picDuration";
    public static String previewWidth = "previewWidth";
    public static String picturePath = "picturePath";
    public static String pictureSize = "pictureSize";
    public static String videoDuration = "videoDuration";

    public static int defaultPicQuality = 80; //图片质量，0~100
    public static int defaultPicWidth = 800;  //照片最小宽度配置，高度根据屏幕比例自动配置
    public static int defaultPicDuration = 3600;  //录像时的自动拍照间隔，单位秒
    public static int defaultPreviewWidth = 1280;  //相机预览界面最小宽度配置，高度根据屏幕比例自动配置
    public static int defaultVideoDuration = 1800;  //录像自动分段间隔，单位秒

    public static CameraParaUtil getInstance(){
        if(myCamPara == null){
            myCamPara = new CameraParaUtil();
            return myCamPara;
        }
        else{
            return myCamPara;
        }
    }

    public Camera.Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth){
        Collections.sort(list, sizeComparator);
        Log.i(TAG, "list.size():" + list.size());

        int i = 0;
        for(Camera.Size s:list){
            if((s.width >= minWidth) && equalRate(s, th)){
                Log.i(TAG, "PreviewSize:w = " + s.width + ",h = " + s.height);
                break;
            }
            i++;
        }
        if(i == list.size()){
            i =  list.size() / 2 ; //如果没找到，就选适中的尺寸
            Log.i(TAG, "PreviewSize:w = " + list.get(i).width + ",h = " + list.get(i).height);
        }
        return list.get(i);
    }

    public Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth){
        Collections.sort(list, sizeComparator);
        Log.i(TAG, "list.size():" + list.size());
        Log.i(TAG, "minWidth:" + minWidth);

        int i = 0;
        for(Camera.Size s:list){
            if((s.width >= minWidth)){
                if (th == -1 || equalRate(s, th)) {
                    Log.i(TAG, "select PictureSize : w = " + s.width + ",h = " + s.height);
                    break;
                }
            }
            i++;
        }
        if(i == list.size()){
            i = list.size() / 2 ; //如果没找到，就选适中的尺寸
            Log.i(TAG, "select PictureSize : w = " + list.get(i).width + ",h = " + list.get(i).height);
        }
        return list.get(i);
    }

    public boolean equalRate(Camera.Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.03)
        {
            return true;
        }
        else{
            return false;
        }
    }

    public  class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // TODO Auto-generated method stub
            if(lhs.width == rhs.width){
                return 0;
            }
            else if(lhs.width > rhs.width){
                return 1;
            }
            else{
                return -1;
            }
        }
    }

    /**打印支持的previewSizes
     * @param params
     */
    public  void printSupportPreviewSize(Camera.Parameters params){
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        for(int i=0; i< previewSizes.size(); i++){
            Camera.Size size = previewSizes.get(i);
            Log.i(TAG, "previewSizes:width = "+size.width+" height = "+size.height);
        }
    }

    /**打印支持的pictureSizes
     * @param params
     */
    public  void printSupportPictureSize(Camera.Parameters params){
        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        for(int i=0; i< pictureSizes.size(); i++){
            Camera.Size size = pictureSizes.get(i);
            Log.i(TAG, "printSupportPictureSize pictureSizes:width = "+ size.width +" height = " + size.height);
        }
    }

    /**打印支持的聚焦模式
     * @param params
     */
    public void printSupportFocusMode(Camera.Parameters params){
        List<String> focusModes = params.getSupportedFocusModes();
        for(String mode : focusModes){
            Log.i(TAG, "focusModes--" + mode);
        }
    }

}
