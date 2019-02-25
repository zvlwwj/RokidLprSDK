package com.rokid.camera.cameralibrary;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.rokid.camerakit.cameralibrary.view.DefultCameraView;
import com.rokid.citrus.citruslprsdk.CitrusLPRSDK;
import com.rokid.jni.ImageStruct;
import com.rokid.jni.libyuv.YuvUtil;

public class MainActivity extends AppCompatActivity {

    private final static String TAG="LPR";

    private DefultCameraView default_camera;
    private final int defaultvale=100;
    private boolean needframe = true;
    private YuvUtil yuvproc = new YuvUtil();
    private ImageStruct imgyuv = new ImageStruct();
    private ImageStruct imgbgr = null;
    private Handler handle;
    private CitrusLPRSDK lpr = new CitrusLPRSDK();
    private long lprHandler;

    void doPR(byte[] data)
    {
        byte[] b = data.clone();
        long startTime = System.currentTimeMillis(); //程序开始记录时间
        int[] rects = lpr.detect(b, default_camera.getPreviewSizeWidth(), default_camera.getPreviewSizeHeight(), 1, lprHandler);
        long totalTime   = System.currentTimeMillis() - startTime; //程序结束记录时间
        Log.d(TAG, "------------detect------------ " + rects + " time:" + totalTime + "(ms)");
        startTime = System.currentTimeMillis();
        String names = lpr.recogAll(b, default_camera.getPreviewSizeWidth(), default_camera.getPreviewSizeHeight(), 1, rects, lprHandler);
        totalTime   = System.currentTimeMillis() - startTime;
        Log.d(TAG, "------------recog------------ " + names + " time:" + totalTime + "(ms)");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lpr.updateModel(getApplicationContext());
        lprHandler = lpr.init(getApplicationContext());

        handle = new Handler();
        default_camera=findViewById(R.id.defult_camera);

        default_camera.addPreviewCallBack(new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(needframe) {
                    doPR(data);
                    needframe = false;
                }
            }
        });

        initOnClick();
    }

    private void initOnClick(){
        default_camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                Log.d(TAG,"defult_camera touch :" + x + " " + y);
                int w = default_camera.getMeasuredWidth();
                int h = default_camera.getMeasuredHeight();
                int previewW=default_camera.getPreviewSizeWidth();
                int previewH=default_camera.getPreviewSizeHeight();
                Rect rect = new Rect((int) ((x - defaultvale < 0 ? 0 : x - defaultvale) / w * previewW),
                        (int) ((y - defaultvale < 0 ? 0 : y - defaultvale) / h * previewH),
                        (int) ((x + defaultvale > w ? w : x + defaultvale) / w * previewW),
                        (int) ((y + defaultvale > h ? h : y + defaultvale) / h * previewH));
                default_camera.setAreaFouce(rect,
                        new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                Log.d(TAG, "fouce return:" + success);
                                if (success) {
                                    needframe = true;
                                }
                            }
                        });

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        default_camera.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        default_camera.onPause();
    }

}
