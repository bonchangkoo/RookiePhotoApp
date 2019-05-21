package kr.co.yogiyo.rookiephotoapp;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 갤러리에서 사진 선택할 때 같이 전달할 request code (test)
    public static final int SELECT_GALLERY_PHOTO = 11111;

    // 편집 화면으로 가기 위한 임시 버튼
    private Button editPhotoButton;
    private Button pickFromGalleryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        setupCheckPermission();

        // stetho 초기화 : chrome://inspect/ 에 접속해서 target 기기의 'inspect'를 누르면
        // 기기에 대한 네트워크, db, sharedPreference 등의 정보를 확인할 수 있음
        Stetho.initializeWithDefaults(this);
    }

    // Test code. 선택한 이미지의 uri를 받아 출력함
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_GALLERY_PHOTO) {
                if (data != null) {
                    if (data.getData() != null) {
                        Toast.makeText(this, String.format("선택(하고 편집)한 이미지 uri : %s", data.getData().toString()), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void initView() {
        editPhotoButton = findViewById(R.id.btn_edit_photo);
        editPhotoButton.setOnClickListener(this);
        pickFromGalleryButton = findViewById(R.id.btn_pick_from_gallery);
        pickFromGalleryButton.setOnClickListener(this);
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
            case R.id.btn_pick_from_gallery:
                // 커스텀 갤러리의 사진 선택
                // Test code. 선택한 이미지의 uri를 받아 출력함
                startActivityForResult(new Intent(this, GalleryActivity.class), SELECT_GALLERY_PHOTO);
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


