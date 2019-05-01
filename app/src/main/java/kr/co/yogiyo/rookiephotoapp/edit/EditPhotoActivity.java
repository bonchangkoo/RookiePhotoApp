package kr.co.yogiyo.rookiephotoapp.edit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;

public class EditPhotoActivity extends BaseActivity {

    public static final int EDIT_SELECTED_PHOTO = 0;
    public static final int EDIT_CAPTURED_PHOTO = 1;

    private int requestMode = 1;

    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        Intent intent = getIntent();

        int intentNumber = intent.getIntExtra("IntentNumber", EDIT_SELECTED_PHOTO);

        if (intentNumber == EDIT_SELECTED_PHOTO) { // 0 이 갤러리 선택
            // 1) 갤러리에서 사진 선택
            pickFromGallery();
        } else if (intentNumber == EDIT_CAPTURED_PHOTO) {
            // 2) 찍은 사진을 편집
            Uri uri = intent.getParcelableExtra("Preview");
            startCrop(uri);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == requestMode) {
                final Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    startCrop(selectedUri);
                } else {
                    Toast.makeText(EditPhotoActivity.this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                // 편집 완료 후 이동할 화면
                //handleCropResult(data);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            //handleCropError(data);
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
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("image/*")
                    .addCategory(Intent.CATEGORY_OPENABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), requestMode);
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

