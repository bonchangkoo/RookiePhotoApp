package kr.co.yogiyo.rookiephotoapp.camera.capture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import kr.co.yogiyo.rookiephotoapp.R;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

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

        byte[] jpeg = ResultHolder.getImage();

        if (jpeg != null) {
            previewImageView.setVisibility(View.VISIBLE);

            capturedImageBitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

            if (capturedImageBitmap == null) {
                finish();
                return;
            }

            previewImageView.setImageBitmap(capturedImageBitmap);
        }
    }

    private void initView() {
        backButton = findViewById(R.id.btn_back_capture);
        editPhotoButton = findViewById(R.id.btn_edit);
        backButton.setOnClickListener(this);
        editPhotoButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back_capture:
                onBackPressed();
                break;
            case R.id.btn_edit:
                // 편집화면으로 이동
                break;
        }
    }

}