package kr.co.yogiyo.rookiephotoapp.camera.capture;

import org.jetbrains.annotations.Nullable;

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
