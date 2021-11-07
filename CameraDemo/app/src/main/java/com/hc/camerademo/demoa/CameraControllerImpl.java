package com.hc.camerademo.demoa;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import com.hc.camerademo.LogUtil;
import com.hc.camerademo.demoa.CameraController;

public class CameraControllerImpl extends CameraController {
    private static final String TAG = "CameraControllerImpl";
    Context context;
    private byte[] preViewBytes = null;

    private static CameraControllerImpl cameraController;

    public static CameraControllerImpl getInstance(Context context, TextureView textureView) {
        if (cameraController == null) {
            cameraController = new CameraControllerImpl(context,textureView);
        }
        return cameraController;
    }
    public CameraControllerImpl(Context context, TextureView textureView) {
        super(context);
        this.context = context;
        if(textureView != null){
            setListener(textureView);
            setSurfaceTexture(textureView.getSurfaceTexture());
        }
    }



    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LogUtil.d(TAG,"onSurfaceTextureAvailable" +surface);
        //设置surface已可用，重新attach到相机
        setSurfaceAvailable(true);
        setSurfaceTexture(surface);
        cameraStart();
    }



    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        LogUtil.d(TAG,"onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LogUtil.d(TAG,"onSurfaceTextureDestroyed" +surface);
        setSurfaceAvailable(false);
        cameraStop();
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onError(int error, Camera camera) {
        if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
            // Looks like this is recoverable.
            LogUtil.e(TAG,"Recoverable error inside the onError callback.CAMERA_ERROR_SERVER_DIED");
            cameraStop();
            cameraStart();
            return;
        }
        LogUtil.e(TAG,"Error inside the onError callback:" + error);
    }

}
