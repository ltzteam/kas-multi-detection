package com.megvii.kas.multi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.megvii.bean.FaceInfo;
import com.megvii.kas.multi.camera.hardware.CameraModule;
import com.megvii.kas.multi.screen.Screen;

import java.util.List;

/**
 * Created by tanjun on 16/8/26.
 */
public class MultiDetectView extends View {
    private Paint paint;
    private Paint paintText;

    private List<FaceInfo> lstFaceRect;

    public MultiDetectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(0xff00b4ff);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        paintText = new Paint();
        paintText.setColor(0xffffffff);
        paintText.setTextSize(Screen.instance(context).density * 11);
        paintText.setTypeface(Typeface.DEFAULT);
    }

    public void updateFaceRect(List<FaceInfo> list) {
        lstFaceRect = list;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != lstFaceRect) {
            for (FaceInfo face : lstFaceRect ) {
                RectF position = face.position;
                canvas.drawRect(
                        getWidth() * (CameraModule.FACING.CAMERA_BACK == CameraModule.facing ? position.left : (1 - position.right)),
                        getHeight() * position.top,
                        getWidth() * (CameraModule.FACING.CAMERA_BACK == CameraModule.facing ? position.right : (1 - position.left)),
                        getHeight() * position.bottom,
                        paint
                );

                canvas.drawText(String.format(" ID: %02d  Q: %2.1f", face.faceID, face.faceQuality),
                        getWidth() * (CameraModule.FACING.CAMERA_BACK == CameraModule.facing ? position.left : (1 - position.right)),
                        getHeight() * position.top - 5,
                        paintText
                );
            }
        }
        super.onDraw(canvas);
    }
}
