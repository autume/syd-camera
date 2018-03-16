package com.oden.syd_camera.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.oden.syd_camera.utils.DisplayUtils;
import com.oden.syd_camera.utils.LogUtils;

/**
 * Created by syd on 2017/5/19.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "SydCamera";
    private SurfaceHolder mHolder;
    private Context context;

    public CameraPreview(Context context) {
        super(context);
        this.context = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
    }

    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.i("surfaceCreated");
        CameraInterface.getInstance().initCamera(DisplayUtils.getScreenRate(context)); //默认全屏的比例
//        CameraInterface.getInstance().initCamera(-1); //默认全屏的比例
        // The Surface has been created, now tell the camera where to draw the preview.
        startPreview(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Take care of releasing the Camera preview in your activity.
        LogUtils.i("surfaceDestroyed");
        CameraInterface.getInstance().releaseCamera(); // release the camera for other applications
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        LogUtils.i("surfaceChanged");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            if (CameraInterface.getInstance().getCamera() != null)
                CameraInterface.getInstance().getCamera().stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        startPreview(holder);
    }

    private void startPreview(SurfaceHolder holder) {
        LogUtils.i("startPreview");
        try {
            if (CameraInterface.getInstance().getCamera() != null) {
                CameraInterface.getInstance().getCamera().setPreviewDisplay(holder);
                CameraInterface.getInstance().getCamera().startPreview();
                CameraInterface.getInstance().getCamera().cancelAutoFocus();
            }
        } catch (Exception e) {
            LogUtils.i("Error starting camera preview: " + e.getMessage());
        }
    }

}
