package kr.co.yogiyo.rookiephotoapp.camera.capture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;

public class PreviewActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    private ImageView previewImageView;
    private Button backButton;
    private Button editPhotoButton;

    private Bitmap capturedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        initImageView();
        initView();

    }

    private void initImageView() {
        previewImageView = findViewById(R.id.preview_image);

        capturedImageBitmap = ResultHolder.getBitmap();

        previewImageView.setVisibility(View.VISIBLE);
        previewImageView.setImageBitmap(capturedImageBitmap);

        showToast(String.format("width: %d, height: %d \nsize : %f MB",
                capturedImageBitmap.getWidth(), capturedImageBitmap.getHeight(), getApproximateFileMegabytes(capturedImageBitmap)));

    }

    private void initView() {
        backButton = findViewById(R.id.btn_back_capture);
        editPhotoButton = findViewById(R.id.btn_edit);
        backButton.setOnClickListener(this);
        editPhotoButton.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (capturedImageBitmap != null && !capturedImageBitmap.isRecycled()) {
            capturedImageBitmap.recycle();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back_capture:
                onBackPressed();
                break;
            case R.id.btn_edit:
                // 편집화면으로 이동
                editCapturedPhoto();
                break;
        }

    }

    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024 / 1024;

    }

    private void editCapturedPhoto() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
        } else {
            Uri uri = getImageUri(PreviewActivity.this, capturedImageBitmap);

            Intent doStartEditPhotoActivityIntent = new Intent(this, EditPhotoActivity.class);
            doStartEditPhotoActivityIntent.putExtra(getString(R.string.edit_photo_category_number), EDIT_CAPTURED_PHOTO);
            doStartEditPhotoActivityIntent.putExtra(getString(R.string.capture_photo_uri), uri);
            startActivity(doStartEditPhotoActivityIntent);
            finish();
        }
    }

    // Bitmap을 Uri로 변환하는 함수
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Image", null);
        return Uri.parse(path);
    }

}
