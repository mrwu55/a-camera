package com.hc.camerademo.demoa;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.hc.camerademo.LogUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CameraManager {
    private static final String TAG = "CameraManager";
    public static CameraManager instance;

    private Camera camera;

    private static final String KEY_QC_SNAPSHOT_PICTURE_FLIP = "snapshot-picture-flip";
    private static final String KEY_QC_PREVIEW_FLIP = "preview-flip";
    private static final String KEY_QC_VIDEO_FLIP = "video-flip";

    // Values for FLIP settings.
    private String FLIP_MODE_OFF = "off";
    private static final String FLIP_MODE_V = "flip-v";
    private static final String FLIP_MODE_H = "flip-h";
    private static final String FLIP_MODE_VH = "flip-vh";

    // 0~parameters.getMaxZoom()
    private static int ZOOM = 10;

    public interface PictureResolution {
        int RESOLUTION_480 = 540;
        int RESOLUTION_600 = 640;
        int RESOLUTION_1080 = 1080;
        int RESOLUTION_1944 = 1944;
    }

    public interface Size {
        int PREVIEW_SIZE_WIDTH = 1600;
        int PREVIEW_SIZE_HEIGHT = 1200;

        /*int PREVIEW_SIZE_WIDTH = 720;
        int PREVIEW_SIZE_HEIGHT = 640;*/

        int PICTURE_SIZE_WIDTH = 3264;
        int PICTURE_SIZE_HEIGHT = 2448;
    }

    public static synchronized CameraManager getInstance(Context context) {
        if (instance == null) {
            instance = new CameraManager(context);
        }
        return instance;
    }

    private CameraManager(Context context) {

    }


    private void setCameraMode(Camera.Parameters parameters, int cameraMode) {
        try {
            Class<?> cls = Class.forName("android.hardware.Camera$Parameters");
            Method setCameraMode = cls.getMethod("setCameraMode", int.class);
            setCameraMode.invoke(parameters, cameraMode);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setZSLMode(Camera.Parameters parameters, String zsl) {
        try {
            Class<?> cls = Class.forName("android.hardware.Camera$Parameters");
            Method setZSLMode = cls.getMethod("setZSLMode", String.class);
            setZSLMode.invoke(parameters, zsl);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void lock() {
        if (camera != null) {
            camera.lock();
        }
    }

    public void unlock() {
        if (camera != null) {
            camera.unlock();
        }
    }

    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }
    public void startPreview() {
        if (camera != null) {
            camera.startPreview();
        }
    }

    public void setCameraOrientation(int degrees) {
       if(camera != null) {
           camera.setDisplayOrientation(degrees);
       }
    }

    private int getCameraIndex(int type) {
        int cameraCount = Camera.getNumberOfCameras();
        //LogUtil.d(TAG, "cameraCount = " + cameraCount);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == type) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void release() {
        if(camera != null) {
            LogUtil.d(TAG, "释放摄像头");
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public synchronized void openOrCloseLight() {

         if (FLIP_MODE_OFF == Camera.Parameters.FLASH_MODE_OFF) {
             FLIP_MODE_OFF = Camera.Parameters.FLASH_MODE_TORCH;
         } else {
             FLIP_MODE_OFF = Camera.Parameters.FLASH_MODE_OFF;
         }

    }

    public synchronized Camera getCamera() {
        return camera;
    }

    public synchronized void  initPreviewCamera(Camera.ErrorCallback errorCallback,int cameraId) {
        if(camera == null) {
            try {
//                int frontIndex = getCameraIndex(Camera.CameraInfo.CAMERA_FACING_BACK);
//                if (frontIndex == -1) {
//                    camera = Camera.open();
//                } else {
//
//                }
                camera = Camera.open(cameraId);
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(FLIP_MODE_OFF);
                for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                    LogUtil.d(TAG, "getSupportedPreviewSizes size.width = " + size.width + "---size.height = " + size.height);
                }
                //parameters.set("zsl", "on");
                if (cameraId == 0) {
                    parameters.setPreviewSize(Size.PREVIEW_SIZE_WIDTH, Size.PREVIEW_SIZE_HEIGHT);
                } else {
                    parameters.setPreviewSize(1280,960);
                }
                LogUtil.d(TAG, "setPreviewSize size.width = " + Size.PREVIEW_SIZE_WIDTH + "---size.height = " + Size.PREVIEW_SIZE_HEIGHT);


                camera.setErrorCallback(errorCallback);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);

            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, "initPictureCamera e = " + e.toString());
            }
        }
    }

    public synchronized void cameraAttachSurface(SurfaceTexture surface){
        if(camera != null){
            try {
                camera.setPreviewTexture(surface);
                camera.startPreview();
                LogUtil.d(TAG, "setPreviewTexture  and startPreview");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void setZoom(int zoom){
        if(getCamera() == null){
            ZOOM = zoom;
            return;
        }
        Camera.Parameters parameters = getCamera().getParameters();
        if(parameters.isZoomSupported()){
            LogUtil.d(TAG,"parameters.setZoom():"+zoom);
            if(zoom <= parameters.getMaxZoom()){
                parameters.setZoom(zoom);
            }else {
                parameters.setZoom(parameters.getMaxZoom());
            }
        }
        getCamera().setParameters(parameters);
    }

    public interface OnInitCompleteListener{
        void onComplete(Camera.Parameters parameters);
    }

    public interface OnPreViewFrameListener{
        void onPreviewFrame();
    }
}
