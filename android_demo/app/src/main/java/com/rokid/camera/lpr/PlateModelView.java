package com.rokid.camera.lpr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import static com.rokid.camera.lpr.PlateModelView.RectConfig.LARGE;
import static com.rokid.camera.lpr.PlateModelView.RectConfig.SMALL;

public class PlateModelView extends View {
    private final static String TAG = "LPR_" + PlateModelView.class.getSimpleName();
    private Context context;
    private Paint paint;
    private int whiteColor, greenColor, redColor;
    private static final int GAP = 60;
    private Rect baseRect;
    private LPRModel lprModel;
    private boolean isDrawing = false;
    private int previewWidth = -1;
    private int previewHeight = -1;

    public PlateModelView(Context context) {
        this(context,null);
    }

    public PlateModelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlateModelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        whiteColor = Color.WHITE;
        redColor = Color.RED;
        greenColor = Color.GREEN;

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        paint.setColor(whiteColor);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    enum RectConfig{
        LARGE(40,15),SMALL(40,15);
        private int line_width;
        private int radius;

        public int getLine_width() {
            return line_width;
        }

        public void setLine_width(int line_width) {
            this.line_width = line_width;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        RectConfig(int line_width, int radius) {
            this.line_width = line_width;
            this.radius = radius;
        }
    }

    public boolean setResultLP(LPRModel model)
    {
        if (!isDrawing) {
            lprModel = model;
            postInvalidate();
            return true;
        }
        return false;
    }

    public void setPreviewSize(int w, int h)
    {
        previewWidth = w;
        previewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");
        if (previewWidth < 0 || previewHeight < 0)
        {
            return;
        }
        if (isDrawing) {
            return;
        }
        isDrawing = true;
        paint.setColor(whiteColor);
        if (lprModel != null && lprModel.size() > 0) {
            for (int i = 0; i < lprModel.size(); i++) {
                paint.setStrokeWidth(10);
                Rect rect = lprModel.getLP(i).getRect();
                Log.d(TAG, "rect = [" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom + "]");
                Log.d(TAG, "name = " + lprModel.getLP(i).getName());
                drawBaseRect(canvas, rect, paint, SMALL);
                drawResult(canvas, rect, lprModel.getLP(i).getName(), greenColor);
            }
            lprModel = null;
        } else {
            canvas.save();
            canvas.restore();
        }
        isDrawing = false;
    }

    private void drawBaseRect(Canvas canvas, Rect rect,Paint paint,RectConfig rectConfig) {

        Log.i("drawRountRect","[width]:"+rect.width());
        canvas.save();

        canvas.translate((rect.left+rect.right)/2f, (rect.top+rect.bottom) / 2f);

        drawRect(canvas, 0,rect.width(),rect.height(),paint,rectConfig);
        drawRect2(canvas, 0,rect.width(),rect.height(),paint,rectConfig);
        drawRect(canvas, 180,rect.width(),rect.height(),paint,rectConfig);
        drawRect2(canvas, 180,rect.width(),rect.height(),paint,rectConfig);

        canvas.restore();
    }

    private void drawResult(Canvas canvas, Rect rect,String name,int color) {
        if (name == null || name.length() <= 0)
        {
            return;
        }
        canvas.save();
        if(rect.left < GAP){
            canvas.translate((rect.left + rect.right) / 2f+rect.width(), (rect.top + rect.bottom) / 2f);
        }else if(previewWidth - rect.right < GAP){
            canvas.translate(rect.left, (rect.top + rect.bottom) / 2f);
        }else if(previewHeight - rect.bottom < GAP){
            canvas.translate((rect.left + rect.right) / 2f, rect.top-GAP);
        }else{
            canvas.translate((rect.left + rect.right) / 2f, rect.bottom+GAP);
        }
        Paint paint = new Paint();
        paint.setColor(Color.rgb(243,170,60));
        paint.setTextSize(50);
        paint.setColor(color);
        canvas.drawText(name,-rect.width()/2,0,paint);
        canvas.restore();
    }

    private void drawRect(Canvas canvas, int angle, int width, int height, Paint paint,RectConfig rectConfig) {
        int LINE_WIDTH = rectConfig.line_width;
        int RADIUS = rectConfig.radius;
        canvas.save();
        canvas.rotate(angle);

        Path path = new Path();
        path.moveTo(-width / 2, height / 2 - LINE_WIDTH);
        path.lineTo(-width / 2, height / 2 - RADIUS);

        RectF rectF = new RectF(-width / 2f, height / 2f - 2 * RADIUS,
                -width / 2f + 2 * RADIUS, height / 2f);

        path.addArc(rectF, 90, 90f);

        path.moveTo(-width / 2 + RADIUS, height / 2);
        path.lineTo(-width / 2 + LINE_WIDTH, height / 2);
        canvas.drawPath(path, paint);

        canvas.restore();

    }

    private void drawRect2(Canvas canvas, int angle, int width, int height, Paint paint,RectConfig rectConfig) {
        int LINE_WIDTH = rectConfig.line_width;
        int RADIUS = rectConfig.radius;
        canvas.save();
        canvas.rotate(angle);

        Path path = new Path();
        path.moveTo(width / 2, height / 2 - LINE_WIDTH);
        path.lineTo(width / 2, height / 2 - RADIUS);

        RectF rectF = new RectF(width / 2f - 2 * RADIUS, height / 2f - 2 * RADIUS,
                width / 2f , height / 2f);

        path.addArc(rectF, 90, -90f);

        path.moveTo(width / 2 - RADIUS, height / 2);
        path.lineTo(width / 2 - LINE_WIDTH, height / 2);
        canvas.drawPath(path, paint);

        canvas.restore();

    }


}
