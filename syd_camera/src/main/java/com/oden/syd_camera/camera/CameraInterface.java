package com.oden.syd_camera.camera;

import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import com.oden.syd_camera.utils.FileUtils;
import com.oden.syd_camera.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static com.oden.syd_camera.camera.CameraParaUtil.CAMERA_FILE_PATH;
import static com.oden.syd_camera.camera.CameraParaUtil.VIDEO_FILE_PATH;

/**
 * Created by syd on 2017/5/19.
 */

public class CameraInterface {
    private static final String TAG = "SydCamera";
    private static CameraInterface mCameraInterface;
    private Camera mCamera = null;
    private CameraListener cameraListener = null;
    private int minPreViewWidth = CameraParaUtil.defaultPreviewWidth; //相机预览界面最小宽度配置，高度根据屏幕比例自动配置
    private int minPicWidth = CameraParaUtil.defaultPicWidth;  //照片最小宽度配置，高度根据屏幕比例自动配置
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK; //前置或后置摄像头

    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private boolean isScaleView = true;
    private VideoRecordListener videoRecordListener = null;

    public interface CameraListener {
        void onTakePictureSuccess(File pictureFile);

        void onTakePictureFail(byte[] data);
    }

    public interface VideoRecordListener {
        void onStartRecorder();

        void onStopRecorder();
    }

    public static synchronized CameraInterface getInstance() {
        if (mCameraInterface == null) {
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    public void initCamera(float previewRate) {
        if (mCamera == null) {
            mCamera = getCameraInstance();
        }
        if (mCamera == null)
            return;
        float viewRate;
        if (isScaleView) {
            viewRate = previewRate;
        } else {
            viewRate = -1;
        }

        Camera.Parameters params = mCamera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        params.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
        if (focusModes.contains(FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
            Log.i(TAG, "params.setFocusMode : " + FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
            Log.i(TAG, "params.setFocusMode : " + FOCUS_MODE_CONTINUOUS_VIDEO);
        }

//        if (focusModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//            params.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO);
//        }
//        params.set("rotation", 90);//照片旋转90度

        com.oden.syd_camera.camera.CameraParaUtil.getInstance().printSupportPictureSize(params);
        com.oden.syd_camera.camera.CameraParaUtil.getInstance().printSupportPreviewSize(params);
        //设置PreviewSize和PictureSize
        Camera.Size pictureSize = com.oden.syd_camera.camera.CameraParaUtil.getInstance().getPropPictureSize(params.getSupportedPictureSizes(), viewRate, minPicWidth);
        Camera.Size previewSize = com.oden.syd_camera.camera.CameraParaUtil.getInstance().getPropPreviewSize(params.getSupportedPreviewSizes(), previewRate, minPreViewWidth);
        params.setPictureSize(pictureSize.width, pictureSize.height);
        params.setPreviewSize(previewSize.width, previewSize.height);
        // set Camera parameters
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);//预览旋转90度
    }

    public void switchCamera() {
        if (getCamera() != null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            Camera.getCameraInfo(mCameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            releaseCamera();
        }
    }

    public void focusOnTouch(int x, int y, FrameLayout preview) {
        Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
        int left = rect.left * 2000 / preview.getWidth() - 1000;
        int top = rect.top * 2000 / preview.getHeight() - 1000;
        int right = rect.right * 2000 / preview.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / preview.getHeight() - 1000;
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        focusOnRect(new Rect(left, top, right, bottom));
    }

    private void focusOnRect(Rect rect) {
        if (getCamera() != null) {
            Camera.Parameters parameters = getCamera().getParameters(); // 先获取当前相机的参数配置对象
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            Log.i(TAG, "parameters.getMaxNumFocusAreas() : " + parameters.getMaxNumFocusAreas());
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(rect, 1000));
                parameters.setFocusAreas(focusAreas);
            }

            getCamera().cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
            getCamera().setParameters(parameters);
            getCamera().autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    Log.i(TAG, "autoFocusCallback success:" + success);
                }
            });
        }
    }

    public void releaseCamera() {
        if (isRecording) {
            stopRecorder();
        }
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    public void startOrStopRecorder(Surface surface) {
        if (isRecording) {
            stopRecorder();
        } else {
            // initialize video camera
            if (prepareVideoRecorder(surface)) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();
                // inform the user that recording has started
                isRecording = true;
                if (videoRecordListener != null) {
                    videoRecordListener.onStartRecorder();
                }
//                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, getOutputMediaFileUri(MEDIA_TYPE_VIDEO));
//                MainActivity.this.sendBroadcast(localIntent);
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    }

    public void startRecorder(Surface surface) {
        LogUtils.i("startRecorder");
        if (isRecording)
            return;
        startOrStopRecorder(surface);
    }

    public void stopRecorder() {
        LogUtils.i("stopRecorder");
        if (!isRecording)
            return;
        // stop recording and release camera
        try {
            //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
            //报错为：RuntimeException:stop failed
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.stop();
        } catch (IllegalStateException e) {
            LogUtils.e(Log.getStackTraceString(e));
        }catch (RuntimeException e) {
            LogUtils.e(Log.getStackTraceString(e));
        }catch (Exception e) {
            LogUtils.e(Log.getStackTraceString(e));
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        isRecording = false;
        if (videoRecordListener != null) {
            videoRecordListener.onStopRecorder();
        }
    }

    private boolean prepareVideoRecorder(Surface surface) {
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_CIF));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(FileUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO, VIDEO_FILE_PATH).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(surface);

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.setOrientationHint(90);
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void takePicture() {
        if (mCamera != null)
            mCamera.takePicture(null, null, mPicture);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = FileUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE, CAMERA_FILE_PATH);

            if (pictureFile == null) {
                Log.e(TAG, "Error creating media file, check storage permissions");
                onTakePictureFail(data);
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
//                Log.d(TAG, "拍照，存储中: " + Arrays.toString(data));
                if (cameraListener != null) {
                    cameraListener.onTakePictureSuccess(pictureFile);
                }
                mCamera.startPreview(); //再次进入preview
                mCamera.cancelAutoFocus();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.getMessage());
                onTakePictureFail(data);
            } catch (IOException e) {
                Log.e(TAG, "Error accessing file: " + e.getMessage());
                onTakePictureFail(data);
            }
        }
    };

    private void onTakePictureFail(byte[] data) {
        if (cameraListener != null) {
            cameraListener.onTakePictureFail(data);
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(mCameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(TAG, "getCameraInstance: " + e);
            // Camera is not available (in use or does not exist)
        }
        Log.d(TAG, "getCameraInstance: " + c);
        return c; // returns null if camera is unavailable
    }

    public Camera getCamera() {
        if (mCamera == null) {
            mCamera = getCameraInstance();
        }
        return mCamera;
    }

    public void setCameraListener(CameraListener cameraListener) {
        this.cameraListener = cameraListener;
    }

    public void setVideoRecordListener(VideoRecordListener videoRecordListener) {
        this.videoRecordListener = videoRecordListener;
    }

    public void setMinPreViewWidth(int minPreViewWidth) {
        this.minPreViewWidth = minPreViewWidth;
    }

    public void setMinPicWidth(int minPicWidth) {
        this.minPicWidth = minPicWidth;
    }

    public void setScaleView(boolean scaleView) {
        isScaleView = scaleView;
    }

    public int getmCameraId() {
        return mCameraId;
    }
}
