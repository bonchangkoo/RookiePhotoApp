package kr.co.yogiyo.rookiephotoapp.edit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Locale;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity;

public class EditResultActivity extends BaseActivity {

    private static final String TAG = EditResultActivity.class.getSimpleName();

    private static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    private Uri getEditPhotoUri;

    // 사진 편집 화면으로 오는 `시작 액티비티`를 구별하기 위한 스트링
    private String startingPointActivity;

    public static void startWithUri(@NonNull Context context, @NonNull Uri uri) {
        Intent intent = new Intent(context, EditResultActivity.class);
        intent.setData(uri);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_result);

        startingPointActivity = getIntent().getStringExtra("startingPointActivity");
        setView();
        setSettingAndResultActionBar();
    }

    // UCropView setting
    private void setView() {
        getEditPhotoUri = getIntent().getData();
        if (getEditPhotoUri != null) {
            try {
                ImageView editedPhotoImageView = findViewById(R.id.iv_edited_photo);
                editedPhotoImageView.setImageURI(getEditPhotoUri);

            } catch (Exception e) {
                Log.e(TAG, "setImageUri", e);
                showToast(e.getMessage());
            }
        }
    }

    private void setSettingAndResultActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (startingPointActivity!= null && !startingPointActivity.equals(DiaryEditActivity.class.getSimpleName())) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.baseline_note_add_white_36); // 왼쪽에 아이콘 배치(홈 아이콘 대체)
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_result, menu);
        MenuItem downloadItem = menu.findItem(R.id.menu_download);
        if (startingPointActivity!=null && startingPointActivity.equals(DiaryEditActivity.class.getSimpleName())) {
            downloadItem.setIcon(R.mipmap.diary_save);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_download) {
            if (startingPointActivity != null) {
                if (startingPointActivity.equals(DiaryEditActivity.class.getSimpleName())) { // 시작 액티비티가 DiaryEditActivity
                    Intent intent = new Intent(EditResultActivity.this, EditPhotoActivity.class);
                    intent.setData(getEditPhotoUri);
                    setResult(RESULT_EDIT_PHOTO, intent);
                    finish();
                }
            } else {
                saveCroppedImage(); // 이미지 저장
            }
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 임시로 back 기능으로 대체
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCroppedImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(EditResultActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditResultActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
        } else {
            if (getEditPhotoUri != null && "file".equals(getEditPhotoUri.getScheme())) {
                try {
                    copyFileToDownloads(getEditPhotoUri);
                } catch (Exception e) {
                    showToast(e.getMessage());
                    Log.e(TAG, getEditPhotoUri.toString(), e);
                }
            } else {
                showToast(R.string.toast_unexpected_error);
            }
        }
    }

    private void copyFileToDownloads(Uri croppedFileUri) throws Exception {

        File yogiDiaryStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YogiDiary");

        if (!yogiDiaryStorageDir.exists()) {
            if (yogiDiaryStorageDir.mkdirs()) {
                Log.d(TAG, getString(R.string.text_mkdir_success));
            } else {
                Log.d(TAG, getString(R.string.text_mkdir_fail));
            }
        }

        String downloadsDirectoryPath = yogiDiaryStorageDir.getPath() + "/";
        String filename = String.format(Locale.getDefault(),
                "%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment());

        File saveFile = new File(downloadsDirectoryPath, filename);

        FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(saveFile);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)));

        showToast(R.string.notification_image_saved);
        finish();
    }
}
