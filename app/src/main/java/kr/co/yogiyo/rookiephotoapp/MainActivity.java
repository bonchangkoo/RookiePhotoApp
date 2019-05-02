package kr.co.yogiyo.rookiephotoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 편집 화면으로 가기 위한 임시 버튼
    private Button editPhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, CameraActivity.class));

        initView();
    }

    private void initView() {
        editPhotoButton = findViewById(R.id.btn_edit_photo);
        editPhotoButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_photo:
                // 사진 선택
                Intent doStartEditPhotoActivityIntent = new Intent(this, EditPhotoActivity.class);
                doStartEditPhotoActivityIntent.putExtra(getString(R.string.edit_photo_category_number), EditPhotoActivity.EDIT_SELECTED_PHOTO);
                startActivity(doStartEditPhotoActivityIntent);
                break;
        }
    }
}

