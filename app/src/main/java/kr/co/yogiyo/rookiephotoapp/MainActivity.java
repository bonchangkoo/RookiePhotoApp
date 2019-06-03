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
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabaseCallback;
import kr.co.yogiyo.rookiephotoapp.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DiaryDatabaseCallback {

    // 편집 화면으로 가기 위한 임시 버튼
    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        setupCheckPermission();
    }

    private void initView() {
        testButton = findViewById(R.id.btn_test);
        testButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            startActivity(new Intent(this, SettingsActivity.class));
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

    @Override
    public void onDiaryAdded() {
        // Do nothing
    }

    @Override
    public void onDiaryByIdFinded(Diary diary) {
        // Do nothing
    }

    @Override
    public void onDiaryUpdated() {
        // Do nothing
    }

    @Override
    public void onDiaryDeleted() {
        // Do nothing
    }

    @Override
    public void onDiariesFinded(List<Diary> diaries) {
        for (Diary diary : diaries) {
//            diaryBackupRestore.executePostDiary(diary);
        }
    }
}