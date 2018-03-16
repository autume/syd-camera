package com.oden.syd_camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.oden.syd_camera.camera.CameraInterface;
import com.oden.syd_camera.camera.CameraParaUtil;
import com.oden.syd_camera.camera.CameraPreview;
import com.oden.syd_camera.entity.FileInfo;
import com.oden.syd_camera.utils.BitmapUtils;
import com.oden.syd_camera.utils.DialogUtil;
import com.oden.syd_camera.utils.FileUtils;
import com.oden.syd_camera.utils.LogUtils;
import com.oden.syd_camera.utils.SDCardUtils;
import com.oden.syd_camera.utils.XPermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.oden.syd_camera.camera.CameraParaUtil.VIDEO_FILE_PATH;

/**
 * Created by syd on 2017/8/22.
 */

public class SydVideoActivity extends Activity implements CameraInterface.CameraListener, CameraInterface.VideoRecordListener {
    private final String TAG = "SydCamera";
    private final int PERMISSION_REQUEST_CODE_CAMERA = 0x02;
    private final int PERMISSION_REQUEST_CODE_STORAGE = 0x03;
    private static Timer timer = null;
    private static TimerTask task = null;
    private FrameLayout preview;
    private CameraPreview mSurfaceView;
    private ImageView img_video_ctrl;
    private int cameraOrientation = 0;
    private int picQuality;
    private int picWidth;
    private int picDuration;
    private int previewWidth;
    private int pictureSize;
    private int videoDuration;
    private long timerCount = 0;
    private boolean isEnableAutoPicture;
    private boolean isEnableRecord = true;
    private boolean isActivityShow = false;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        setContentView(R.layout.activity_syd_video);

        Intent intent = getIntent();
        picQuality = intent.getIntExtra(CameraParaUtil.picQuality, CameraParaUtil.defaultPicQuality);
        picWidth = intent.getIntExtra(CameraParaUtil.picWidth, CameraParaUtil.defaultPicWidth);
//        picDuration = intent.getIntExtra(CameraParaUtil.picDuration, CameraParaUtil.defaultPicDuration);
        picDuration = intent.getIntExtra(CameraParaUtil.picDuration, CameraParaUtil.defaultPicDuration);
        previewWidth = intent.getIntExtra(CameraParaUtil.previewWidth, CameraParaUtil.defaultPreviewWidth);
        pictureSize = intent.getIntExtra(CameraParaUtil.pictureSize, 0);
        videoDuration = intent.getIntExtra(CameraParaUtil.videoDuration, CameraParaUtil.defaultVideoDuration);
//        videoDuration = intent.getIntExtra(CameraParaUtil.videoDuration, CameraParaUtil.defaultVideoDuration);

        LogUtils.i("picQuality: " + picQuality + ",picDuration: " + picDuration + ",picWidth: " + picWidth + ",previewWidth: " + previewWidth + ",pictureSize: " + pictureSize + ",videoDuration: " + videoDuration);

        mHandler = new Handler();
        initView();
        initEvent();
        checkCameraPermission();
        checkSdPermission();
    }

    /**
     * view初始化
     */
    private void initView() {
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        img_video_ctrl = (ImageView) findViewById(R.id.img_video_ctrl);
    }

    /**
     * 点击事件初始化
     */
    private void initEvent() {
        //录像
        img_video_ctrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_video_ctrl.setEnabled(false);
                CameraInterface.getInstance().startOrStopRecorder(mSurfaceView.getHolder().getSurface());
            }
        });
    }

    /**
     * camera初始化
     */
    private void initCameraView() {
        CameraInterface.getInstance().setMinPreViewWidth(previewWidth);
        CameraInterface.getInstance().setMinPicWidth(picWidth);
        CameraInterface.getInstance().setCameraListener(this);
        CameraInterface.getInstance().setVideoRecordListener(this);
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

    /**
     * 开始自动录像
     */
    private void startRecord() {
        recycleSdSpace();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isActivityShow && isEnableRecord) {
                    CameraInterface.getInstance().startRecorder(mSurfaceView.getHolder().getSurface());
                    startTimer();
                    Toast.makeText(SydVideoActivity.this, "开始录像!", Toast.LENGTH_SHORT).show();
                }
            }
        }, 2000);
    }

    /**
     * 停止录像
     */
    private void stopRecord() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CameraInterface.getInstance().stopRecorder();
            }
        });
    }

    /**
     * 开始自动拍照
     */
    private void startAutoPicture() {
        if (picDuration > 0)
            isEnableAutoPicture = true;

        //简单测试，用定时器做更好
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isEnableAutoPicture) {
                    try {
                        Thread.sleep(picDuration);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isEnableAutoPicture) {
                        LogUtils.i("startAutoPicture");
                        CameraInterface.getInstance().takePicture();
                    }
                }
            }
        }).start();
    }

    /**
     * 停止自动拍照
     */
    private void stopAutoPicture() {
        isEnableAutoPicture = false;
    }

    /**
     * 检测SD卡空间，空间不足则删除视频文件
     */
    private void recycleSdSpace() {
        long availableSize = SDCardUtils.getSDAvailableSize(this);
        LogUtils.i("sd availableSize: " + availableSize + "M");

        if (availableSize < 1024)
        {
            int successCount = 0;
            LogUtils.e("剩余空间少于1G，开始删除文件!");
            String path = FileUtils.getMediaStorageDir(VIDEO_FILE_PATH);
            if (path != null) {
                ArrayList<FileInfo>  fileInfoArrayList = FileUtils.ListFilesByTime(path, FileUtils.TYPE_VIDEO);
                LogUtils.i("GetFiles: " + fileInfoArrayList);
                //删除最早的三个文件
                if (fileInfoArrayList.size() > 3) {
                    for (int i=0; i<3; i++) {
                        File file = new File(fileInfoArrayList.get(i).getPath());
                        if (file.exists()){
                            boolean result = file.delete();
                            LogUtils.i("recycleSdSpace: " + result + ",file: " + file.getName());
                            successCount++;
                        }
                    }
                }
                if (successCount < 2){
                    isEnableRecord = false;
                    LogUtils.i("空间不足，无法开始录像!");
                }else {
                    isEnableRecord = true;
                }
            }
        }
    }

    /**
     * 启动定时器
     */
    private void startTimer() {
        timerCount = 0;
        if (timer == null) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    LogUtils.i("timerCount: " + timerCount);
                    timerCount++;
                    if (timerCount >= videoDuration) {
                        timerCount = 0;
                        stopRecord();
                        startRecord();
                        stopTimer();
                    }
                }
            };
            timer.schedule(task, 0, 1000);  //0s后执行timer，之后每隔1s执行一次
        }
    }

    /**
     * 关闭定时器
     */
    private void stopTimer() {
        if (timer != null && task != null) {
            task.cancel();
            timer.cancel();

            task = null;
            timer = null;
        }
    }

    /**
     * 拍照成功回调
     *
     * @param pictureFile
     */
    @Override
    public void onTakePictureSuccess(File pictureFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapUtils.rotateBitmap(BitmapFactory.decodeFile(pictureFile.getPath(), options), CameraInterface.getInstance().getmCameraId(), cameraOrientation);
        if (pictureSize > 0) {
            bitmap = BitmapUtils.bitmapCompress(bitmap, 120);
        }
        Log.d(TAG, "onTakePictureSuccess picQuality: " + picQuality + ", bitmap.getByteCount():" + bitmap.getByteCount());
        BitmapUtils.saveBitmapToSd(bitmap, pictureFile.getPath(), picQuality);

        //更新本地相册
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile));
        sendBroadcast(localIntent);
        LogUtils.i("拍照成功 pictureFile:" + pictureFile.getPath());
        Toast.makeText(SydVideoActivity.this, "拍照成功 pictureFile:" + pictureFile.getPath(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 拍照失败回调
     *
     * @param data
     */
    @Override
    public void onTakePictureFail(byte[] data) {
        LogUtils.e("拍照失败，请检查权限设置!");
    }

    /**
     * 开始录像回调
     */
    @Override
    public void onStartRecorder() {
        LogUtils.i("onStartRecorder");
        img_video_ctrl.setImageResource(R.drawable.icon_video_stop);
        mHandler.postDelayed(setRecorderButtonEnable, 1000);
        startAutoPicture();
    }

    /**
     * 停止录像回调
     */
    @Override
    public void onStopRecorder() {
        LogUtils.i("onStopRecorder");
        img_video_ctrl.setImageResource(R.drawable.icon_video_record);
        mHandler.postDelayed(setRecorderButtonEnable, 1000);
        stopAutoPicture();
    }

    /**
     * 延时使能按钮 防止多次点击
     */
    Runnable setRecorderButtonEnable = new Runnable() {
        @Override
        public void run() {
            img_video_ctrl.setEnabled(true);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 获取摄像头权限
     */
    private void checkCameraPermission() {
        XPermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE_CAMERA, new String[]{Manifest.permission.CAMERA},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        LogUtils.i("checkCameraPermission onPermissionGranted");
                        if (CameraInterface.getInstance().getCamera() == null) {
                            LogUtils.e("checkCameraPermission getCamera() == null");
                            DialogUtil.showPermissionDeniedDialog(SydVideoActivity.this, "相机");
                        } else {
                            initCameraView();
                        }
                    }

                    @Override
                    public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                        Toast.makeText(SydVideoActivity.this, "获取相机权限失败", Toast.LENGTH_SHORT).show();
                        if (alwaysDenied) { // 拒绝后不再询问 -> 提示跳转到设置
                            DialogUtil.showPermissionDeniedDialog(SydVideoActivity.this, "相机");
                        } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                            DialogUtil.showPermissionRemindDiaog(SydVideoActivity.this, "相机", deniedPermissions, PERMISSION_REQUEST_CODE_CAMERA);
                        }
                    }
                });
    }

    /**
     * 获取SD卡权限
     */
    private void checkSdPermission() {
        XPermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE_STORAGE, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        LogUtils.i("checkSdPermission onPermissionGranted");
                    }

                    @Override
                    public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                        LogUtils.i("checkSdPermission onPermissionDenied");
                        if (alwaysDenied) { // 拒绝后不再询问 -> 提示跳转到设置
                            DialogUtil.showPermissionDeniedDialog(SydVideoActivity.this, "文件存储");
                        } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                            DialogUtil.showPermissionRemindDiaog(SydVideoActivity.this, "文件存储", deniedPermissions, PERMISSION_REQUEST_CODE_CAMERA);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityShow = true;
        startRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityShow = false;
//        CameraInterface.getInstance().stopRecorder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        CameraInterface.getInstance().stopRecorder();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}
