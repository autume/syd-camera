package com.oden.camera_test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.oden.syd_camera.SydCameraActivity;
import com.oden.syd_camera.SydCropActivity;
import com.oden.syd_camera.SydVideoActivity;
import com.oden.syd_camera.camera.CameraParaUtil;
import com.oden.syd_camera.crop.CropParaUtil;
import com.oden.syd_camera.utils.BitmapUtils;
import com.oden.syd_camera.utils.WaterMaskUtils;

import java.io.File;


/**
 * Created by syd
 */
public class CameraTestActivity extends AppCompatActivity {
    private final String TAG = "SydCamera";
    private Button btn_take_photo;
    private Button btn_crop;
    private Button btn_video;
    private ImageView img_photo;
    private boolean isNeedCrop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        initView();
        initEvent();
    }

    private void replacePhotoWithWaterMask(Context context, String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Log.d(TAG,"bitmap: " + bitmap + ",filePath: " + filePath);
        if (bitmap != null){
            Bitmap waterBitmap = WaterMaskUtils.drawTextToRightBottom(context, bitmap, "waterMaskTest323", 16, Color.RED, 0, 0);
            BitmapUtils.saveBitmapToSd(waterBitmap, filePath, 80);
            Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath)));
            sendBroadcast(localIntent);
        }
    }

    private void initView() {
        btn_take_photo = (Button) findViewById(R.id.btn_take_photo);
        btn_crop = (Button) findViewById(R.id.btn_crop);
        btn_video = (Button) findViewById(R.id.btn_video);
        img_photo = (ImageView) findViewById(R.id.img_photo);
    }

    private void initEvent() {
        //录像
        btn_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVideo();
            }
        });

        //拍照
        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTakePhoto();
            }
        });

        //拍照并裁剪
        btn_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNeedCrop = true;
                startTakePhoto();
            }
        });
    }

    private void startVideo() {
        Intent intent = new Intent(CameraTestActivity.this, SydVideoActivity.class);
        intent.putExtra(CameraParaUtil.picQuality, 70); //视频质量0~100
        intent.putExtra(CameraParaUtil.picWidth, 1536);  //视频最小宽度配置，高度根据屏幕比例自动配置
        intent.putExtra(CameraParaUtil.previewWidth, 1280);  //相机预览界面最小宽度配置，高度根据屏幕比例自动配置
        startActivityForResult(intent, CameraParaUtil.REQUEST_CODE_FROM_VIDEO);
    }

    private void startTakePhoto() {
        Intent intent = new Intent(CameraTestActivity.this, SydCameraActivity.class);
        intent.putExtra(CameraParaUtil.picQuality, 70); //图片质量0~100
//        intent.putExtra(CameraParaUtil.pictureSize, 300); //图片大小 KB
        intent.putExtra(CameraParaUtil.picWidth, 1536);  //照片最小宽度配置，高度根据屏幕比例自动配置
        intent.putExtra(CameraParaUtil.previewWidth, 1280);  //相机预览界面最小宽度配置，高度根据屏幕比例自动配置
        startActivityForResult(intent, CameraParaUtil.REQUEST_CODE_FROM_CAMERA);
    }

    private void startCrop(String path) {
        Intent intent = new Intent(CameraTestActivity.this, SydCropActivity.class);
        intent.putExtra(CropParaUtil.cropQuality, 70); //图片质量0~100
        intent.putExtra(CropParaUtil.cropTitle, "图片裁剪");
        intent.putExtra(CropParaUtil.cropSrcPicPath, path);
        startActivityForResult(intent, CropParaUtil.REQUEST_CODE_FROM_CUTTING);
    }

    private void startCropForBitmap() {
        Intent intent = new Intent(CameraTestActivity.this, SydCropActivity.class);
        intent.putExtra(CropParaUtil.cropQuality, 70); //图片质量0~100
        intent.putExtra(CropParaUtil.cropTitle, "图片裁剪");
        startActivityForResult(intent, CropParaUtil.REQUEST_CODE_FROM_CUTTING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i(TAG, "onActivityResult resultCode:" + resultCode + ",requestCode: " + requestCode);

        onResultExceptionHandle(resultCode);

        if (resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "操作取消!");
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "操作失败!");
            return;
        }

        switch (requestCode) {
            case CameraParaUtil.REQUEST_CODE_FROM_CAMERA:
                String picturePath;
                picturePath = data.getStringExtra(CameraParaUtil.picturePath);
                if (isNeedCrop) {
                    startCrop(picturePath);
                    isNeedCrop = false;
                } else {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
                    img_photo.setImageBitmap(bitmap);
                    Log.d(TAG, "onActivityResult bitmap.getByteCount(): " + bitmap.getByteCount());
                }
                Log.d(TAG, "onActivityResult picturePath: " + picturePath);
                break;
            case CropParaUtil.REQUEST_CODE_FROM_CUTTING:
                String cropDestPicPath;
                cropDestPicPath = data.getStringExtra(CropParaUtil.cropDestPicPath);
                img_photo.setImageBitmap(BitmapFactory.decodeFile(cropDestPicPath));
                Log.d(TAG, "onActivityResult cropDestPicPath: " + cropDestPicPath);
                break;
            default:
                break;
        }
    }

    /**
     * 兼容三星A8出现无权限存储文件的处理，
     * 将手机重启后可正常存储文件..
     *
     * @param resultCode
     */
    private void onResultExceptionHandle(int resultCode) {
        Log.d(TAG, "onResultExceptionHandle");

        //拍照失败处理，针对三星A8无法存储文件处理，拍照失败后检查CameraParaUtil.pictureBitmap是否有数据
        if (resultCode == CameraParaUtil.REQUEST_CODE_FROM_CAMERA_FAIL && CameraParaUtil.pictureBitmap != null) {
            //使用pictureBitmap进行裁剪
            startCropForBitmap();
        }

        //裁剪失败处理，针对三星A8无法存储文件处理，失败后检查CropParaUtil.croppedBitmap是否有数据
        if (resultCode == CropParaUtil.REQUEST_CODE_FROM_CUTTING_FAIL && CropParaUtil.croppedBitmap != null) {
            img_photo.setImageBitmap(CropParaUtil.croppedBitmap);
        }
    }


}
