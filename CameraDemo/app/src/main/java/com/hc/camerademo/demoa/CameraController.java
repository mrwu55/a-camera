package com.hc.camerademo.demoa;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.TextureView;

import androidx.annotation.WorkerThread;

import com.hc.camerademo.LogUtil;

public abstract class CameraController implements
        Thread.UncaughtExceptionHandler,
        TextureView.SurfaceTextureListener, Camera.ErrorCallback{

    private static final String TAG = "CameraController";
    /**
     * Camera is about to be stopped.
     */
    private static final int STATE_STOPPING = -1;
    /**
     * Camera is stopped.
     */
    private static final int STATE_STOPPED = 0;
    /**
     * Camera is about to cameraStart.
     */
    private static final int STATE_STARTING = 1;
    /**
     *  Camera is available and we can set parameters.
     */
    private static final int STATE_STARTED = 2;

    protected Handler mHandler;
    Handler mCrashHandler;
    protected int mState = STATE_STOPPED;
    private Object syncObj = new Object();
    private CameraManager cameraManager;
    private boolean isSurfaceAvailable = false;
    private SurfaceTexture surfaceTexture;
    private HandlerThread mHandlerThread ;
    private static final int FRONT = 1;//前置摄像头标记
    private static final int BACK = 0;//后置摄像头标记
    private int currentCameraType = 0;//当前打开的摄像头标记


    public CameraController(Context context) {
        mCrashHandler = new Handler(Looper.getMainLooper());
        mHandlerThread = new HandlerThread("CameraThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandlerThread.setUncaughtExceptionHandler(this);
        cameraManager = CameraManager.getInstance(context);
    }

    public void setCameraDegrees (int degrees) {
        if (cameraManager != null) {
            cameraManager.setCameraOrientation(degrees);
        }
    }
    private class NoOpExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {

        }
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable throwable) {
        LogUtil.e(TAG, "Interrupting thread with state:" +  cameraState() + "due to CameraException:"+ throwable.getMessage());
        mCrashHandler.post(new Runnable() {
            @Override
            public void run() {
                RuntimeException exception;
                if (throwable instanceof RuntimeException) {
                    exception = (RuntimeException) throwable;
                } else {
                    exception = new RuntimeException(throwable);
                }
                throw exception;
            }
        });
        destroy();
    }

    private String cameraState() {
        switch (mState) {
            case STATE_STOPPING: return "STATE_STOPPING";
            case STATE_STOPPED: return "STATE_STOPPED";
            case STATE_STARTING: return "STATE_STARTING";
            case STATE_STARTED: return "STATE_STARTED";
            default:
                break;
        }
        return "null";
    }


    public void changeCamera() {
        if ((mHandler !=null) && mHandlerThread.isAlive()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (syncObj){
                        if(mState >= STATE_STARTING){
                            mState = STATE_STOPPING;
                            onStop();
                            mState = STATE_STOPPED;
                        }
                        if(currentCameraType == FRONT) {
                            currentCameraType = BACK;
                        } else {
                            currentCameraType = FRONT;
                        }
                        mState = STATE_STARTING;
                        onStart();
                        mState = STATE_STARTED;

                    }

                }});
        }
    }


    /**
     *  Starts the preview asynchronously.
     */
    final public void cameraStart() {
        LogUtil.d(TAG, "CameraController.cameraStart() posting runnable. State:" + cameraState() + "mHandlerThread.isAlive():" + mHandlerThread.isAlive());
        if ((mHandler !=null) && mHandlerThread.isAlive()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (syncObj){
                        LogUtil.e("XXXXXX","cameraStart Thread id:" + Thread.currentThread().getId());
                        LogUtil.d(TAG, "CameraController.cameraStart() executing. State:" + cameraState());
                        if(mState == STATE_STARTED){
                            cameraAttachSurface();
                            return;
                        }
                        if (mState >= STATE_STARTING) {
                            return;
                        }
                        mState = STATE_STARTING;
                        LogUtil.e("XXXXX", "isMainThread:" + isMainThread());
                        LogUtil.d(TAG, "about to call onStart()" + cameraState());
                        onStart();
                        LogUtil.d(TAG, "returned from onStart()." + "Dispatching." + cameraState());
                        mState = STATE_STARTED;
                    }
                }
            });
        }

    }

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * Stops the preview asynchronously.
     */
    public final void cameraStop() {
        LogUtil.d(TAG, "CameraController.cameraStop() posting runnable. State:" + cameraState() + "mHandlerThread.isAlive():" + mHandlerThread.isAlive());
        if ((mHandler !=null) && mHandlerThread.isAlive()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (syncObj){
                        LogUtil.d(TAG, "CameraController.cameraStop() executing. State:" + cameraState());
                        if (mState <= STATE_STOPPED) {
                            return;
                        }
                        mState = STATE_STOPPING;
                        LogUtil.d(TAG, "about to call onStop()");
                        onStop();
                        LogUtil.d(TAG, "returned from onStop()." + "Dispatching.");
                        mState = STATE_STOPPED;
                    }
                }
            });
        }

    }

    public final void openOrCloseLight() {
        LogUtil.d(TAG, "CameraController.openOrCloseLight() posting runnable. State:" + cameraState() + "mHandlerThread.isAlive():" + mHandlerThread.isAlive());
        if ((mHandler !=null) && mHandlerThread.isAlive()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (syncObj){
                        if (mState == STATE_STARTED) {
                            mState = STATE_STOPPING;
                            onStop();
                            mState = STATE_STOPPED;
                        }
                        mState = STATE_STARTING;
                        LogUtil.d(TAG, "about to call onStart()" + cameraState());
                        cameraManager.openOrCloseLight();
                        onStart();
                        LogUtil.d(TAG, "returned from onStart()." + "Dispatching." + cameraState());
                        mState = STATE_STARTED;
                        LogUtil.d(TAG, "CameraController.openOrCloseLight() executing. State:" + cameraState());
//                        cameraManager.openOrCloseLight();
                    }
                }
            });
        }

    }

    public final void destroy() {
        LogUtil.d(TAG, "destroy State was:" + cameraState());
        if (mHandlerThread != null) {
            mHandlerThread.setUncaughtExceptionHandler(new NoOpExceptionHandler());
        }
        if ((mHandler !=null) && mHandlerThread.isAlive()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (syncObj){
                        try {
                            // Don't check, try cameraStop again.
                            LogUtil.d(TAG, "stopImmediately State was:" + cameraState() +isMainThread());
                            if (mState == STATE_STOPPED) {
                                quitHandlerThread();
                                return;
                            }
                            mState = STATE_STOPPING;
                            LogUtil.e(TAG, "stopImmediately call stop");
                            onStop();
                            mState = STATE_STOPPED;
                            LogUtil.d(TAG, "stopImmediately Stopped. State is:" + cameraState());
                        } catch (Exception e) {
                            // Do nothing.
                            LogUtil.d(TAG, "Swallowing exception while stopping." + e);
                            mState = STATE_STOPPED;
                        }
                        quitHandlerThread();

                    }

                }
            });
        }

    }

    private void quitHandlerThread() {
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mCrashHandler != null) {
            mCrashHandler.removeCallbacksAndMessages(null);
            mCrashHandler = null;
        }
    }

    /**
     * Forces a restart.
     */
    protected final void restart() {
        LogUtil.d("Restart:", "posting runnable");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (syncObj){
                    LogUtil.d(TAG, "executing. Needs stopping:" + (mState > STATE_STOPPED) + "," + cameraState());
                    // Don't cameraStop if stopped.
                    if (mState > STATE_STOPPED) {
                        mState = STATE_STOPPING;
                        onStop();
                        mState = STATE_STOPPED;
                        LogUtil.d(TAG, "stopped. Dispatching." + cameraState());
                    }

                    LogUtil.d("Restart: about to cameraStart. State:", cameraState());
                    mState = STATE_STARTING;
                    onStart();
                    mState = STATE_STARTED;
                    LogUtil.d("Restart: returned from cameraStart. Dispatching. State:", cameraState());
                }
            }
        });
    }

    /**
     * Starts the preview.At the end of this method camera must be available, e.g.
     * for setting parameters.
     */
    @WorkerThread
    private void onStart(){
        initCamera(currentCameraType);
        cameraAttachSurface();
    }

    /**
     * Stops the preview.
     */
    @WorkerThread
    private void onStop(){
        releaseCamera();
        LogUtil.e(TAG,"onStop:"+isMainThread());
    }


    /**
     * Returns current state.
     * @return
     */
    final int getState() {
        return mState;
    }


    /**
     * 初始化摄像头 TextureView用
     */
    private void initCamera(int cameraId) {
        if(cameraManager != null){
            cameraManager.initPreviewCamera(this,cameraId);
        }
    }

    /**
     * 释放摄像头
     */
    private void releaseCamera(){
        if(cameraManager != null){
            cameraManager.release();
        }
    }


    private void cameraAttachSurface() {
        if(isSurfaceAvailable && (surfaceTexture != null)){
            cameraManager.cameraAttachSurface(surfaceTexture);
        }
    }
    public void setListener(TextureView textureView) {
        if(textureView != null){
            textureView.setSurfaceTextureListener(this);
        }
    }
    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        if(surfaceTexture != null){
            this.surfaceTexture = surfaceTexture;
        }
    }

    public void setSurfaceAvailable(boolean available){
        isSurfaceAvailable = available;
    }
}
