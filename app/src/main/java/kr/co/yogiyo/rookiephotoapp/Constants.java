package kr.co.yogiyo.rookiephotoapp;

import android.os.Environment;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
    public static final File YOGIDIARY_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YogiDiary");

    // Settings
    public static final String NO_DATA = "no_data";
    public static final String FAILED_TO_CONNECT = "Failed to connect";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String PREFERENCE_KEY = "key";
    public static final String BACKUP_DIALOG_KEY = "backup_dialog";
    public static final String RESTORE_DIALOG_KEY = "restore_dialog";
    public static final String SIGN_DIALOG_KEY = "sign_dialog";
    public static final String PREFERENCE_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";
    public static final String COMPRESSED_FOLDER_NAME = "compressed";
    public static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
}