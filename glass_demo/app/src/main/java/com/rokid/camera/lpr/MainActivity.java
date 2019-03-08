package com.rokid.camera.lpr;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.rokid.camerakit.cameralibrary.view.GLCameraView;
import com.rokid.citrus.citruslprsdk.CitrusLPRSDK;

public class MainActivity extends Activity {

    private final static String TAG = "LPR_" + MainActivity.class.getSimpleName();

    private GLCameraView cameraView;
    private boolean needframe = true;
    private int frameCount = -1;
    private CitrusLPRSDK lpr = new CitrusLPRSDK();
    private long lprHandler;
    private PlateModelView plateModelView;
    private boolean needReset = false;
    private int sWidth = Constants.SCREEN_WIDTH;
    private int sHeight = Constants.SCREEN_HEIGHT;
    private int pWidth, pHeight;

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
                String names = lpr.recogAll(b, cameraView.getPreviewSizeWidth(), cameraView.getPreviewSizeHeight(), 1, singleRect, lprHandler);
                if (names.length() > 0) {
                    long recogTime = System.currentTimeMillis() - startTime;
                    Log.d(TAG, "======== recog : name = " + names + "; time:" + recogTime + "(ms)");
                    // 将(pWidth, pHeight)上的坐标转换到屏幕(sWidth, sHeight)的坐标
                    int left = rects[i] * sWidth / pWidth;
                    int top = rects[i + 1] * sHeight / pHeight;
                    int right = left + rects[i + 2] * sWidth / pWidth;
                    int bottom = top + rects[i + 3] * sHeight / pHeight;
                    Rect rect = new Rect(left, top, right, bottom);
                    Rect actualRect = Tools.getScaleRect(rect, sWidth, sHeight);
                    String name = names.replace(",", "");
                    if (actualRect != null) {
                        LPRModel.LP lp = lprModel.new LP(actualRect, name);
                        lprModel.addLP(lp);
                    }
                }
            }
            if (plateModelView.setResultLP(lprModel)) {
                needReset = true;
            }
        }
        else if (needReset) {
            if (plateModelView.setResultLP(null)) {
                needReset = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lpr.updateModel(getApplicationContext());
        lprHandler = lpr.init(getApplicationContext());

        cameraView =  findViewById(R.id.camera_view);
        plateModelView = findViewById(R.id.plateModelView);

        pWidth = cameraView.getPreviewSizeWidth();
        pHeight = cameraView.getPreviewSizeHeight();

        Log.d(TAG, "preview [ width = " + pWidth + ", height = " + pHeight + " ]");

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
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

}
