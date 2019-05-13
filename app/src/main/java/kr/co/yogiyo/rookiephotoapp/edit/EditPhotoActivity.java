package kr.co.yogiyo.rookiephotoapp.edit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;

public class EditPhotoActivity extends BaseActivity {

    private static final String TAG = EditPhotoActivity.class.getSimpleName();

    public static final int EDIT_SELECTED_PHOTO = 0;
    public static final int EDIT_CAPTURED_PHOTO = 1;

    private static final int REQUEST_PICK_GALLERY = 123;
    private static final int REQUEST_STORAGE_READ_AND_WRITE_ACCESS_PERMISSION = 103;


    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        doSeparateIntent();
    }

    private void doSeparateIntent() {

        Intent intent = getIntent();
        // 갤러리에서 가져온 사진과 찍은 사진을 구별하기 위한 Intent
        int photoCategoryNumber = intent.getIntExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO);

        if (photoCategoryNumber == EDIT_SELECTED_PHOTO) { // 갤러리에서 사진 선택
            pickFromGallery();
        } else if (photoCategoryNumber == EDIT_CAPTURED_PHOTO) {  // 찍은 사진을 편집
            Uri capturedPhotoUri = intent.getParcelableExtra(getString(R.string.capture_photo_uri));

            if (capturedPhotoUri != null) {
                startCrop(capturedPhotoUri);
            } else {
                showToast(R.string.dont_load_captured_photo);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == REQUEST_PICK_GALLERY && data != null) {
                    final Uri selectedUri = data.getData();
                    if (selectedUri != null) {
                        startCrop(selectedUri);
                    } else {
                        showToast(R.string.toast_cannot_retrieve_selected_image);
                    }
                } else if (requestCode == UCrop.REQUEST_CROP) {
                    // 편집 완료 후 이동할 화면
                    if (data != null) {
                        handleCropResult(data);
                        finish();
                    }else{
                        showToast(R.string.toast_unexpected_error);
                    }

                }
                break;
            case RESULT_CANCELED:
                finish();
                break;
            case UCrop.RESULT_ERROR:
                if (data != null) {
                    handleCropError(data);
                }
                break;
        }
    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            EditResultActivity.startWithUri(EditPhotoActivity.this, resultUri);
        } else {
            showToast(R.string.toast_cannot_retrieve_cropped_image);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            showToast(cropError.getMessage());
        } else {
            showToast(R.string.toast_unexpected_error);
        }
    }

    // 갤러리에서 이미지 선택 : EDIT_SELECTED_PHOTO
    private void pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED))) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_READ_AND_WRITE_ACCESS_PERMISSION);
        } else { // 권한 허용 후
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("image/*")
                    .addCategory(Intent.CATEGORY_OPENABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
            // 갤러리 이동
            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_PICK_GALLERY);
        }
    }

    // 편집 화면 이동 : EDIT_CAPTURED_PHOTO
    private void startCrop(Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME + ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));

        // Crop Gestures는 SCALE만 가능하게 옵션 설정
        UCrop.Options options = new UCrop.Options();
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.SCALE);
        uCrop.withOptions(options);

        uCrop.start(EditPhotoActivity.this);
    }

}

