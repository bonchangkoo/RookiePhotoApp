package kr.co.yogiyo.rookiephotoapp.camera.capture;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

public class ResultHolder {

    private static Bitmap bitmap;

    public static void setBitmap(@Nullable Bitmap bitmap) {
        ResultHolder.bitmap = bitmap;
    }

    @Nullable
    public static Bitmap getBitmap() {
        return bitmap;
    }

    public static void dispose() {
        setBitmap(null);
    }

}
