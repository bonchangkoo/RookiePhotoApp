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
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;

public class EditPhotoActivity extends BaseActivity {

    private static final String TAG = "EditPhotoActivity";

    public static final int EDIT_SELECTED_PHOTO = 0;
    public static final int EDIT_CAPTURED_PHOTO = 1;

    private static final int REQUEST_PICK_GALLERY = 123;

    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

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
                Toast.makeText(this, getString(R.string.dont_load_captured_photo), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_GALLERY) {
                final Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    startCrop(selectedUri);
                } else {
                    Toast.makeText(this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                // 편집 완료 후 이동할 화면
                // handleCropResult(data);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(EditPhotoActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(EditPhotoActivity.this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

    // 갤러리에서 이미지 선택 : EDIT_SELECTED_PHOTO
    private void pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
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

        // Crop Gestures는 SCALE만 가능하게
        UCrop.Options options = new UCrop.Options();
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.SCALE);
        uCrop.withOptions(options);

        uCrop.start(EditPhotoActivity.this);
    }
}

