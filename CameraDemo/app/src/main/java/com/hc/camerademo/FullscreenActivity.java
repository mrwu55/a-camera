package com.hc.camerademo;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.hc.camerademo.demoa.CameraControllerImpl;

import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    FrameLayout frameLayout;
    CameraControllerImpl cameraController;
    TextureView textureView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        LogUtil.e("XXXXXX","onCreate thread size:" + stacks.size());
        setContentView(R.layout.activity_fullscreen);
        frameLayout = findViewById(R.id.frame);
        textureView = new TextureView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(1080,1440);
        frameLayout.addView(textureView,layoutParams);
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        LogUtil.e("XXXXXX","screenWidth:"+screenWidth+"screenHeight:"+(screenWidth/3*4));
        cameraController = new CameraControllerImpl(this,textureView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeOrientation(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }
    private void changeOrientation (boolean isLandScape) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureView.getLayoutParams();
        int width = 1080;
        int height = 1440;
        if (isLandScape) {
            width = 450;
            height = 600;
            cameraController.setCameraDegrees(0);
        } else {
            cameraController.setCameraDegrees(90);
        }
        layoutParams.width = width;
        layoutParams.height = height;
    }

    @Override
    protected void onResume() {
        if (cameraController != null) {
            cameraController.cameraStart();
        }
        super.onResume();
        LogUtil.e("XXXXXX","onResume");

    }

    @Override
    protected void onStop() {
        if (cameraController != null) {
            cameraController.cameraStop();
        }
        super.onStop();
        LogUtil.e("XXXXXX","onStop");
    }

    @Override
    protected void onDestroy() {
        if (cameraController != null) {
            cameraController.destroy();
        }
        super.onDestroy();
        LogUtil.e("XXXXXX","onDestroy");
    }

    public void onChangeCameraClick(View view) {
        if (cameraController != null) {
            cameraController.changeCamera();
        }
    }

    public void onOpenLightClick(View view) {
        if (cameraController != null) {
            cameraController.openOrCloseLight();
        }
    }
}
