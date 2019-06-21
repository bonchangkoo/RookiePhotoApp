package kr.co.yogiyo.rookiephotoapp;

import android.os.Environment;

import java.io.File;

public class Constants {

    // REQUEST
    public static final int REQUEST_PICK_GALLERY = 101;
    public static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    public static final int REQUEST_STORAGE_READ_AND_WRITE_ACCESS_PERMISSION = 103;
    public static final int REQUEST_DIARY_PICK_GALLERY = 104;
    public static final int REQUEST_DIARY_CAPTURE_PHOTO = 105;

    // RESULT
    public static final int RESULT_EDIT_PHOTO = 201;
    public static final int RESULT_CAPTURED_PHOTO = 202;

    // PATH
    public static final File FOONCARE_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FooNCaRe");
}
