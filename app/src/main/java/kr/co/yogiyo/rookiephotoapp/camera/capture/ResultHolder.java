package kr.co.yogiyo.rookiephotoapp.camera.capture;

import android.util.Size;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ResultHolder {

    private static byte[] image;


    public static void setImage(@Nullable byte[] image) {
        ResultHolder.image = image;
    }

    @Nullable
    public static byte[] getImage() {
        return image;
    }

    public static void dispose() {
        setImage(null);
    }

}
