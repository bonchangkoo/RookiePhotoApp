package kr.co.yogiyo.rookiephotoapp;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;
import kr.co.yogiyo.rookiephotoapp.diary.DiaryDetailActivity;
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 편집 화면으로 가기 위한 임시 버튼
    private Button editPhotoButton;
    // 다이어리로 가기 위한 임시 버튼
    private Button diaryButton;

    private Button test1Button;
    private Button test2Button;
    private Button test3Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        setupCheckPermission();
    }

    private void initView() {
        editPhotoButton = findViewById(R.id.btn_edit_photo);
        diaryButton = findViewById(R.id.btn_diary);

        editPhotoButton.setOnClickListener(this);
        diaryButton.setOnClickListener(this);

        // 테스트용
        test1Button = findViewById(R.id.btn_test1);
        test2Button = findViewById(R.id.btn_test2);
        test3Button = findViewById(R.id.btn_test3);

        test1Button.setOnClickListener(this);
        test2Button.setOnClickListener(this);
        test3Button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_photo:
                Intent doStartEditPhotoActivityIntent = new Intent(this, EditPhotoActivity.class);
                doStartEditPhotoActivityIntent.putExtra(getString(R.string.edit_photo_category_number), EditPhotoActivity.EDIT_SELECTED_PHOTO);
                startActivity(doStartEditPhotoActivityIntent);
                break;
            case R.id.btn_diary:
                Intent doStartDiaryEditActivityIntent = new Intent(this, DiaryEditActivity.class);
                doStartDiaryEditActivityIntent.putExtra("DIARY_IDX", "NEW");
                startActivity(doStartDiaryEditActivityIntent);
                break;
            case R.id.btn_test1:
                Intent doStartTest1Intent = new Intent(this, DiaryDetailActivity.class);
                doStartTest1Intent.putExtra("DIARY_IDX", "1");
                startActivity(doStartTest1Intent);
                break;
            case R.id.btn_test2:
                Intent doStartTest2Intent = new Intent(this, DiaryDetailActivity.class);
                doStartTest2Intent.putExtra("DIARY_IDX", "2");
                startActivity(doStartTest2Intent);
                break;
            case R.id.btn_test3:
                Intent doStartTest3Intent = new Intent(this, DiaryDetailActivity.class);
                doStartTest3Intent.putExtra("DIARY_IDX", "3");
                startActivity(doStartTest3Intent);
                break;
        }
    }

    // Permission check
    private void setupCheckPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
            }
        };

        // 기기 버전별 요청 권한 추가
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // 권한 확인 및 요청 라이브러리
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("단말기의 [설정] > [권한]에서 접근 권한을 ON으로 설정해주세요.\n\n* 카메라\n* 저장공간")
                .setGotoSettingButtonText("설정")
                .setDeniedCloseButtonText("취소")
                .setPermissions(permissions.toArray(new String[0]))
                .check();
    }
}


