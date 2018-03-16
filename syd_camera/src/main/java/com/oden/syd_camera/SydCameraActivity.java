package com.oden.syd_camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.oden.syd_camera.camera.CameraInterface;
import com.oden.syd_camera.camera.CameraParaUtil;
import com.oden.syd_camera.camera.CameraPreview;
import com.oden.syd_camera.utils.BitmapUtils;
import com.oden.syd_camera.utils.DialogUtil;
import com.oden.syd_camera.utils.XPermissionUtils;

import java.io.File;

/**
 * Created by syd on 2017/6/14.
 */
public class SydCameraActivity extends Activity implements CameraInterface.CameraListener {
    private final String TAG = "SydCamera";
    private final int PERMISSION_REQUEST_CODE_CAMERA = 0x02;
    private final int PERMISSION_REQUEST_CODE_STORAGE = 0x03;
    private FrameLayout preview;
    private CameraPreview mSurfaceView;
    private ImageView img_take_picture;
    private ImageView img_switch_camera;
    private ImageView img_exit;
    private OrientationEventListener mOrientationListener;
    private int cameraOrientation = 0;
    private int picQuality;
    private int picWidth;
    private int previewWidth;
    private int pictureSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        setContentView(R.layout.activity_syd_camera);

        Intent intent = getIntent();
        picQuality = intent.getIntExtra(CameraParaUtil.picQuality, CameraParaUtil.defaultPicQuality);
        picWidth = intent.getIntExtra(CameraParaUtil.picWidth, CameraParaUtil.defaultPicWidth);
        previewWidth = intent.getIntExtra(CameraParaUtil.previewWidth, CameraParaUtil.defaultPreviewWidth);
        pictureSize = intent.getIntExtra(CameraParaUtil.pictureSize, 0);

        Log.i(TAG, "picQuality: " + picQuality + ",picWidth: " + picWidth + ",previewWidth: " + previewWidth + ",pictureSize: " + pictureSize);

        initView();
        initEvent();
        checkCameraPermission();
        checkSdPermission();
    }

    private void initView() {
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        img_take_picture = (ImageView) findViewById(R.id.img_take_picture);
        img_switch_camera = (ImageView) findViewById(R.id.img_switch_camera);
        img_exit = (ImageView) findViewById(R.id.img_exit);
//        initCameraView();
    }

    private void initCameraView() {
        CameraInterface.getInstance().setMinPreViewWidth(previewWidth);
        CameraInterface.getInstance().setMinPicWidth(picWidth);
        CameraInterface.getInstance().setCameraListener(this);
        mSurfaceView = new CameraPreview(this);
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                CameraInterface.getInstance().focusOnTouch((int) event.getX(), (int) event.getY(), preview);
                return false;
            }
        });
        preview.addView(mSurfaceView);
    }

    private void initEvent() {
        //拍照
        img_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraInterface.getInstance().takePicture();
            }
        });

        //切换摄像头
        img_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraInterface.getInstance().switchCamera();
                preview.removeAllViews();
                preview.addView(mSurfaceView);
            }
        });

        //退出拍照
        img_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTakePhoto();
            }
        });

        //监听手机旋转角度
        mOrientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                cameraOrientation = orientation;
            }
        };
    }


    private void checkCameraPermission() {
        XPermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE_CAMERA, new String[]{Manifest.permission.CAMERA},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Log.i(TAG, "checkCameraPermission onPermissionGranted");
                        if (CameraInterface.getInstance().getCamera() == null) {
                            Log.e(TAG, "checkCameraPermission getCamera() == null");
                            DialogUtil.showPermissionDeniedDialog(SydCameraActivity.this, "相机");
                        } else {
                            initCameraView();
                        }
                    }

                    @Override
                    public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                        Toast.makeText(SydCameraActivity.this, "获取相机权限失败", Toast.LENGTH_SHORT).show();
                        if (alwaysDenied) { // 拒绝后不再询问 -> 提示跳转到设置
                            DialogUtil.showPermissionDeniedDialog(SydCameraActivity.this, "相机");
                        } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                            DialogUtil.showPermissionRemindDiaog(SydCameraActivity.this, "相机", deniedPermissions, PERMISSION_REQUEST_CODE_CAMERA);
                        }
                    }
                });
    }

    private void checkSdPermission() {
        XPermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE_STORAGE, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Log.i(TAG, "checkSdPermission onPermissionGranted");
                    }

                    @Override
                    public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                        Log.i(TAG, "checkSdPermission onPermissionDenied");
                        if (alwaysDenied) { // 拒绝后不再询问 -> 提示跳转到设置
                            DialogUtil.showPermissionDeniedDialog(SydCameraActivity.this, "文件存储");
                        } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                            DialogUtil.showPermissionRemindDiaog(SydCameraActivity.this, "文件存储", deniedPermissions, PERMISSION_REQUEST_CODE_CAMERA);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrientationListener.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOrientationListener != null)
            mOrientationListener.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                cancelTakePhoto();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void cancelTakePhoto() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onTakePictureSuccess(File pictureFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapUtils.rotateBitmap(BitmapFactory.decodeFile(pictureFile.getPath(), options), CameraInterface.getInstance().getmCameraId(), cameraOrientation);
//        Bitmap bitmap = BitmapUtils.rotateBitmap(BitmapFactory.decodeFile(pictureFile.getPath(), options), 45);
//        Bitmap waterBitmap = WaterMaskUtils.drawTextToRightBottom(this, bitmap, "waterMaskTest123", 16, Color.RED, 0, 0);
//        Bitmap smallBitmap = BitmapUtils.bitmapCompress(bitmap, 120);
        if (pictureSize > 0) {
            bitmap = BitmapUtils.bitmapCompress(bitmap, 120);
        }
//        bitmap = BitmapUtils.compressToSizeByQuality(bitmap, 120);
        Log.i(TAG, "onTakePictureSuccess bitmap.getWidth: " + bitmap.getWidth() + ", bitmap.getHeight():" + bitmap.getHeight());
        Log.i(TAG, "onTakePictureSuccess picQuality: " + picQuality + ", bitmap.getByteCount():" + bitmap.getByteCount());
//        Log.d(TAG, "onTakePictureSuccess picQuality: " + picQuality + ", smallBitmap.getByteCount():" + smallBitmap.getByteCount());
        BitmapUtils.saveBitmapToSd(bitmap, pictureFile.getPath(), picQuality);

        //更新本地相册
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile));
        sendBroadcast(localIntent);
        Log.i(TAG, "拍照成功 pictureFile:" + pictureFile.getPath());

        Intent intent = new Intent();
        intent.putExtra(CameraParaUtil.picturePath, pictureFile.getPath());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onTakePictureFail(byte[] data) {
        Log.e(TAG, "拍照失败，请检查权限设置!"); //三星A8出现无法创建文件夹的提示，重启恢复正常
        CameraParaUtil.pictureBitmap = BitmapUtils.rotateBitmap(BitmapUtils.Bytes2Bitmap(data), CameraInterface.getInstance().getmCameraId(), cameraOrientation);
        Intent intent = new Intent();
        setResult(CameraParaUtil.REQUEST_CODE_FROM_CAMERA_FAIL, intent);
        finish();
//        Toast.makeText(SydCameraActivity.this, "拍照失败，请检查权限设置!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
