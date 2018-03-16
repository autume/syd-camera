package com.oden.syd_camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oden.syd_camera.camera.CameraParaUtil;
import com.oden.syd_camera.crop.CropView;
import com.oden.syd_camera.crop.CropParaUtil;
import com.oden.syd_camera.utils.BitmapUtils;
import com.oden.syd_camera.utils.FileUtils;
import com.oden.syd_camera.utils.LogUtils;

import java.io.File;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.oden.syd_camera.crop.CropParaUtil.CROP_FILE_PATH;

/**
 * Created by syd on 2017/6/17.
 */

public class SydCropActivity extends Activity {
    private LinearLayout ll_view;
    private CropView cropView;
    private Button btn_ok;
    private ImageView img_back;
    private TextView tv_title;
    private TextView tv_cancel;

    private int cropQuality;
    private String cropSrcPicPath;
    private String cropTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syd_crop);

        Intent intent = getIntent();
        cropQuality = intent.getIntExtra(CropParaUtil.cropQuality, CropParaUtil.defaultPicQuality);
        cropSrcPicPath = intent.getStringExtra(CropParaUtil.cropSrcPicPath);
        cropTitle = intent.getStringExtra(CropParaUtil.cropTitle);
        LogUtils.d("cropQuality: " + cropQuality + ",cropSrcPicPath: "  + cropSrcPicPath + ", cropTitle: " + cropTitle);

        initView();
        initEvent();
    }

    private void initView() {
        ll_view = (LinearLayout) findViewById(R.id.ll_view);
        cropView = (CropView) findViewById(R.id.cropView);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        img_back = (ImageView) findViewById(R.id.img_back);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_title = (TextView) findViewById(R.id.tv_title);


        if (cropSrcPicPath != null) {
            cropView.setImageBitmap(BitmapFactory.decodeFile(cropSrcPicPath));
        } else if (CameraParaUtil.pictureBitmap != null){
            cropView.setImageBitmap(CameraParaUtil.pictureBitmap);
        }else{
            LogUtils.e("裁剪图片路径不能为空!");
        }

        tv_title.setText(cropTitle);
    }

    private void initEvent() {
        ll_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        //返回
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCrop();
            }
        });

        //取消
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCrop();
            }
        });

        //确定
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedBitmap = cropView.clip();

                if (cropSrcPicPath == null) {
                    CropParaUtil.croppedBitmap = croppedBitmap;
                    Intent intent = new Intent();
                    setResult(CropParaUtil.REQUEST_CODE_FROM_CUTTING_FAIL, intent);
                    finish();
                    return;
                }

                File file = FileUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE, CROP_FILE_PATH);
                if (file == null || croppedBitmap == null) {
                    Intent intent = new Intent();
                    setResult(CropParaUtil.REQUEST_CODE_FROM_CUTTING_FAIL, intent);
                    finish();
                    LogUtils.e("截图失败!");
                    return;
                }
                BitmapUtils.saveBitmapToSd(croppedBitmap, file.getPath(), cropQuality);

                //更新本地相册
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
                sendBroadcast(localIntent);

                LogUtils.d("截图成功并保存： " + file.getPath());
                Intent intent = new Intent();
                intent.putExtra(CropParaUtil.cropDestPicPath, file.getPath());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                cancelCrop();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void cancelCrop() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

}