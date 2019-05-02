package kr.co.yogiyo.rookiephotoapp.camera.capture;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

public class ResultHolder {

    private static byte[] image;
    private static Bitmap bitmap;

    public static void setImage(@Nullable byte[] image) {
        ResultHolder.image = image;
    }

    @Nullable
    public static byte[] getImage() {
        return image;
    }

    public static void setBitmap(@Nullable Bitmap bitmap) {
        ResultHolder.bitmap = bitmap;
    }

    @Nullable
    public static Bitmap getBitmap() {
        return bitmap;
    }

    public static void dispose() {
        setImage(null);
        setBitmap(null);
    }

}
