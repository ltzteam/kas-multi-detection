package com.megvii.kas.multi.util;

import android.content.Context;
import android.util.Log;

import com.megvii.demo.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tanjun on 16/8/25.
 */
public class FaceModel {
    public static byte[] read(Context context) {
        byte[] buffer = new byte[1024];
        int count = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            InputStream is = context.getResources().openRawResource(R.raw.model);
            while ((count = is.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
            }
            bos.close();
            is.close();
        } catch (IOException e) {
            Log.e("FIL_MESSAGE", "error: Load Face Model failed." + e.getMessage());
        }
        return bos.toByteArray();
    }
}
