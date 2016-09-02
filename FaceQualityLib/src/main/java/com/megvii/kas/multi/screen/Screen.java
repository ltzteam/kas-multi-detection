package com.megvii.kas.multi.screen;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Screen {
    public void initialize(Context context) {
        if (drawWidth == 0 || drawHeight == 0 || mWidth == 0 || mHeight == 0
                || density == 0) {
            Resources res = context.getResources();
            DisplayMetrics metrics = res.getDisplayMetrics();

            density = metrics.density;
            mNotificationBarHeight = (int) (35 * density);
            mWidth = metrics.widthPixels;// - (int)(50 * density)
            mHeight = metrics.heightPixels/* - mNotificationBarHeight*/;// -
            // (int)(50
            // *
            // density)
            mScreenWidth = metrics.widthPixels;
            mScreenHeight = metrics.heightPixels;

            densityDpi = metrics.densityDpi;

            drawPaddingLeft = density * PADDING_L;
            drawPaddingRight = density * PADDING_R;
            drawPaddingTop = density * PADDING_T;
            drawPaddingBottom = density * PADDING_B;

            drawWidth = mWidth - drawPaddingLeft - drawPaddingRight;
            // TODO 如果非全屏，需减去标题栏的高度
            drawHeight = mHeight - drawPaddingTop - drawPaddingBottom;
        }
    }

    protected Screen() {
    }

    public static Screen instance(Context context) {
        if (null == _instance) {
            _instance = new Screen();
            _instance.initialize(context);
        }
        return _instance;
    }

    public int mNotificationBarHeight;

    public int mScreenWidth;
    public int mScreenHeight;
    public int mWidth;
    public int mHeight;
    public float densityDpi;
    public float density;

    public float drawWidth;
    public float drawHeight;

    private static final int PADDING_L = 30;
    private static final int PADDING_R = 30;
    private static final int PADDING_T = 50;
    private static final int PADDING_B = 40;

    public float drawPaddingLeft;
    public float drawPaddingRight;
    public float drawPaddingTop;
    public float drawPaddingBottom;

    static Screen _instance;
}