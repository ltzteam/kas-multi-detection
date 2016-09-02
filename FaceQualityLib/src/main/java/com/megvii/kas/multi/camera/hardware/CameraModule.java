package com.megvii.kas.multi.camera.hardware;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.widget.RelativeLayout;

import com.megvii.kas.multi.screen.Screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by tanjun on 16/8/25.
 */
public class CameraModule {
    protected int cameraWidth;
    protected int cameraHeight;

    protected Camera camera;
    protected int cameraId;//front:1 back:0
    public static FACING facing = FACING.CAMERA_BACK;

    public static enum FACING {
        CAMERA_BACK,
        CAMERA_FRONT
    }


    public CameraModule() {
    }

    /**
     * Initialize Camera & Parameters.
     */
    public Camera open(Activity activity) {
        try {
            cameraId = FACING.CAMERA_BACK == facing ? 0 : 1;
            camera = Camera.open(cameraId);
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            Camera.Parameters params = camera.getParameters();
            Camera.Size bestPreviewSize = calcPreviewSize(camera.getParameters(), 1920, 1080);
            cameraWidth = bestPreviewSize.width;
            cameraHeight = bestPreviewSize.height;
            Log.w("FIL_MESSAGE", String.format("camera: [W: %d, H:%d].", cameraWidth, cameraHeight));
            params.setPreviewSize(cameraWidth, cameraHeight);
            params.setFocusMode(FACING.CAMERA_BACK == facing ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : Camera.Parameters.FLASH_MODE_OFF);
            camera.setDisplayOrientation(getCameraAngle(activity));
            camera.setParameters(params);

            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            Log.w("FIL_MESSAGE", String.format("preview: [W: %d, H:%d]", previewSize.width, previewSize.height));
            return camera;
        } catch (Exception e) {
            Log.e("FIL_MESSAGE", String.format("initialize camera & parameters failed. [%s].",e.getMessage()));
            if (null != camera) {
                closeCamera();
            }
            return null;
        }
    }

    /**
     * Calculate nearest preview size
     * @param params
     * @param width expect preview's width
     * @param height expect preview's width
     * @return current real size
     */
    private Camera.Size calcPreviewSize(Camera.Parameters params, final int width, final int height) {
        List<Camera.Size> allSupportedSize = params.getSupportedPreviewSizes();
        ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
        for (Camera.Size tmpSize : allSupportedSize) {
            if (tmpSize.width > tmpSize.height) {
                widthLargerSize.add(tmpSize);
            }
        }
        Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int off_one = Math.abs(lhs.width * lhs.height - width * height);
                int off_two = Math.abs(rhs.width * rhs.height - width * height);
                return off_one - off_two;
            }
        });
        return widthLargerSize.get(0);
    }

    public void setPreviewCallback(Camera.PreviewCallback cb) {
        if (camera != null) {
            camera.setPreviewCallback(cb);
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        try {
            if (camera != null) {
                camera.setPreviewTexture(surfaceTexture);
                camera.startPreview();
            }
        } catch (IOException e) {
            Log.e("FIL_MESSAGE", String.format("error: Camera StartPreview. [%s]", e.getMessage()));
        }
    }

    /**
     * Get camera's rotation angle
     */
    public int getCameraAngle(Activity activity) {
        int rotateAngle = 90;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        if (Camera.CameraInfo.CAMERA_FACING_FRONT == info.facing) {
            rotateAngle = (info.orientation + degrees) % 360;
            rotateAngle = (360 - rotateAngle) % 360; // compensate the mirror
        } else { // back-facing
            rotateAngle = (info.orientation - degrees + 360) % 360;
        }
        return rotateAngle;
    }

    public void closeCamera() {
        if (null != camera) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    /**
     * Pass through screen's ratio to calculated preview's ratio (get a suitable of ratio for preview)
     */
    public RelativeLayout.LayoutParams getLayoutParam(Context context) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        float scale = Math.min(
                Screen.instance(context).mWidth  * 1.0f / previewSize.height,
                Screen.instance(context).mHeight * 1.0f / previewSize.width
        );
        int layout_width = (int) (scale * previewSize.height);
        int layout_height = (int) (scale * previewSize.width);

        RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
                layout_width, layout_height);
        layout_params.addRule(RelativeLayout.CENTER_VERTICAL);
        layout_params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        return layout_params;
    }
}
