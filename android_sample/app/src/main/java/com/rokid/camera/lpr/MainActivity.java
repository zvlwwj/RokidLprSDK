package com.rokid.camera.lpr;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.rokid.camerakit.cameralibrary.view.DefaultCameraView;
import com.rokid.citrus.citruslprsdk.CitrusLPRSDK;

public class MainActivity extends Activity {

    private final static String TAG = "LPR_" + MainActivity.class.getSimpleName();

    private DefaultCameraView cameraView;
    private boolean needframe = true;
    private int frameCount = -1;
    private CitrusLPRSDK lpr = new CitrusLPRSDK();
    private long lprHandler;
    private PlateModelView plateModelView;
    private boolean needReset = false;
    private int pWidth, pHeight;
    private int sWidth, sHeight;

    private void doPR(byte[] data)
    {
        byte[] b = data.clone();
        long startTime = System.currentTimeMillis();
        int[] rects = lpr.detect(b, pWidth, pHeight, 1, lprHandler);
        if (rects.length > 0)
        {
            LPRModel lprModel = new LPRModel();
            long detectTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, "======== detect : " + " time:" + detectTime + "(ms)");
            String rectStr = "";
            for (int i = 0; i < rects.length; i++)
            {
                rectStr = rectStr + rects[i] + (((i + 1) % 4) == 0 ? " / " : ",");
            }
            Log.d(TAG,"rects = " + rectStr);

            for (int i = 0; i + 3 < rects.length; i = i + 4)
            {
                // 针对预览画面(pWidth，pHeight)的坐标
                int singleRect[] = {rects[i], rects[i + 1], rects[i + 2], rects[i + 3]};
                startTime = System.currentTimeMillis();
                String names = lpr.recogAll(b, pWidth, pHeight, 0, singleRect, lprHandler);
                if (names.length() > 0) {
                    long recogTime = System.currentTimeMillis() - startTime;
                    Log.d(TAG, "======== recog : name = " + names + "; time:" + recogTime + "(ms)");
                    // 将(pWidth, pHeight)上的坐标转换到屏幕(sWidth, sHeight)的坐标
                    int left = rects[i] * sWidth / pWidth;
                    int top = rects[i + 1] * sHeight / pHeight;
                    int right = left + rects[i + 2] * sWidth / pWidth;
                    int bottom = top + rects[i + 3] * sHeight / pHeight;
                    Rect rect = new Rect(left, top, right, bottom);
                    String name = names.replace(",", "");
                    LPRModel.LP lp = lprModel.new LP(rect, name);
                    lprModel.addLP(lp);
                }
            }
            if (lprModel.size() > 0 && plateModelView.setResultLP(lprModel)) {
                needReset = true;
            }
        }
        else if (needReset) {
            if (plateModelView.setResultLP(null)) {
                needReset = false;
            }
        }
    }

    private void init() {

        plateModelView = findViewById(R.id.plateModelView);

        cameraView = new DefaultCameraView(MainActivity.this);
        cameraView.setPreviewSize(1280, 720);
        ConstraintLayout.LayoutParams params =
                new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((ConstraintLayout)findViewById(R.id.view_layout)).addView(cameraView, params);

        lpr.updateModel(getApplicationContext());
        lprHandler = lpr.init(getApplicationContext());

        pWidth = cameraView.getPreviewSizeWidth();
        pHeight = cameraView.getPreviewSizeHeight();

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        sWidth = dm.widthPixels;
        sHeight = dm.heightPixels;
        plateModelView.setPreviewSize(sWidth, sHeight);

        Log.d(TAG, "preview [ width = " + pWidth + ", height = " + pHeight + " ]");
        Log.d(TAG, "screen  [ width = " + sWidth + ", height = " + sHeight + " ]");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraView.addPreviewCallBack(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                frameCount = (frameCount + 1) % 10;
                if(needframe && frameCount == 0) {
                    doPR(data);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "requestCode = " + requestCode);
        switch (requestCode){
            case 1: {
                switch (permissions[0]) {
                    case Manifest.permission.CAMERA: {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            init();
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        if (!(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA))) {
            Log.d(TAG, "no permission");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 0x01);
        }
        else {
            init();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (cameraView != null) {
            cameraView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (cameraView != null) {
            cameraView.onPause();
        }
    }

}
