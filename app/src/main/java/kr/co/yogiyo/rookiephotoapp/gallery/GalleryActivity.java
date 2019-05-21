package kr.co.yogiyo.rookiephotoapp.gallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;

/**
 * GalleryActivity
 * : Toolbar의 폴더명을 보여주는 Spinner에서 폴더명을 선택하면
 * 폴더명 안의 사진들을 Fragment의 RecyclerView - GridLayout으로 보여주도록 요청하는 Activity
 *
 * GalleryActivity의 역할
 * - toolbar 설정
 * - Fragment에서 사진 선택시 toolbar의 옵션 메뉴 버튼 edit/done 활성화 기능 제공
 * - 사진 파일을 가진 폴더 검색해서 spinner 설정
 * - spinner에서 폴더 선택시 새 Fragment에 폴더명 목록을 전달하고 Fragment를 시작함
 */
public class GalleryActivity extends BaseActivity {

    // 편집 액티비티에 갤러리에서 선택한 사진과 같이 전달할 request code (test)
    private static final int EDIT_SELECTED_GALLERY_PHOTO = 11111;
    // 저장소 접근 권한 요청 code
    private static final int REQUEST_STORAGE_READ_AND_WRITE_ACCESS_PERMISSION = 103;

    // toolbar : 화면 상단에서 기능 버튼 제공
    // gallerySpinner : toolbar 안에서 폴더 선택 제공
    private Toolbar toolbar;
    private Spinner gallerySpinner;

    // Toolbar에 있는 기능 버튼이며 사진 선택 여부에 따라 menu icon 활성화/비활성화 설정을 위해 선언
    // 메서드 참고 : setControlButton(...)
    private MenuItem editMenuItem;
    private MenuItem doneMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initView();

        setupActionBar();

        setupSpinner();

        // 권한 체크 (고의적으로 설정에서 권한을 해제할 때 앱 종료되는 경우 방지)
        setupCheckPermission();
    }

    // Toolbar의 옵션 메뉴에 커스텀 layout 추가 (edit 버튼, done 버튼)
    // 사진 선택 여부에 따라 menu icon 활성화/비활성화 설정을 위해
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery_option, menu);

        editMenuItem = menu.findItem(R.id.menu_edit);
        doneMenuItem = menu.findItem(R.id.menu_done);
        // 처음 시작일 때 옵션 메뉴 버튼 비활성화
        setControlButton(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // fragment의 메서드 호출을 위해서 현재 화면에 보여지고 있는 fragment 반환
        Fragment nowFragment = getSupportFragmentManager().findFragmentById(R.id.frame_gallery);

        switch (item.getItemId()) {
            // home 버튼 위치(toolbar의 왼쪽 끝)의 옵션 아이템 선택했을 때
            case android.R.id.home:
                onBackPressed();
                break;

            // edit 옵션 메뉴 버튼을 눌렀을 때 edit 액티비티에 선택된 이미지 uri 전달
            case R.id.menu_edit:
                Uri uriForEdit = null;
                if (nowFragment instanceof GalleryFragment) {
                    uriForEdit = ((GalleryFragment) nowFragment).getSelectedImageUri();
                }

                // TODO: 현재 EDIT_SELECTED_PHOTO 코드일 경우 gallery를 요청하기 때문에 EDIT_CAPTURED_PHOTO 코드를 저장 (수정 필요)
                // edit 액티비티에 전달
                Intent doStartEditPhotoActivityIntent = new Intent(this, EditPhotoActivity.class);
                doStartEditPhotoActivityIntent.putExtra(getString(R.string.edit_photo_category_number), EDIT_CAPTURED_PHOTO);
                doStartEditPhotoActivityIntent.putExtra(getString(R.string.capture_photo_uri), uriForEdit);
                startActivityForResult(doStartEditPhotoActivityIntent, EDIT_SELECTED_GALLERY_PHOTO);
                break;

            // done 옵션 메뉴 버튼을 눌렀을 때 이전 액티비티에 선택된 이미지 uri 전달
            case R.id.menu_done:
                Uri originalUri = null;
                if (nowFragment instanceof GalleryFragment) {
                    originalUri = ((GalleryFragment) nowFragment).getSelectedImageUri();
                }
                Intent originalUriIntent = new Intent();
                originalUriIntent.setData(originalUri);
                setResult(RESULT_OK, originalUriIntent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_SELECTED_GALLERY_PHOTO) {

            }
        }
    }

    // List<폴더명 (이미지 수)> 반환 메서드
    public List<String> getFolderNames(Activity activity) {
        // Map<폴더명, 이미지 수> 저장
        Map<String, Integer> mapOfAllImageFolders = new HashMap<>();
        // List<폴더명 (이미지 수)> 저장
        List<String> listOfAllImageFolders = new ArrayList<>();
        // 이미지 수 저장
        int countOfAllImages = 0;
        String folderName;

        // External volume(저장소)에 접근하기 위한 Uri 스타일 옵션 변수
        // content://media/external/images/media
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // 어떤 column을 받을지 지정한 옵션 변수
        // BUCKET_DISPLAY_NAME : 이미지 파일을 갖고 있는 폴더명을 반환하는 옵션
        String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        // uri 스타일로 projection 카테고리의 데이터 set을 가져옴
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return listOfAllImageFolders;
        }

        // 해당하는 칼럼 인덱스를 가져옴
        // = 폴더명 카테고리 인덱스를 가져옴
        int columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        // 폴더명과 이미지 수 저장
        while (cursor.moveToNext()) {
            countOfAllImages++;
            // 폴더명 카테고리 인덱스의 값(=폴더명)을 가져옴
            folderName = cursor.getString(columnIndexFolderName);
            // 폴더명 안에 이미지 개수가 몇 개 인지 셈
            if (mapOfAllImageFolders.containsKey(folderName)) {
                mapOfAllImageFolders.put(folderName, mapOfAllImageFolders.get(folderName) + 1);
            } else {
                mapOfAllImageFolders.put(folderName, 1);
            }
        }

        cursor.close();

        // Map에 저장된 폴더명과 이미지 수를 기반으로 "폴더명 (이미지 수)" ArrayList 저장
        listOfAllImageFolders.add(String.format(getString(R.string.spinner_folder_name_count),
                getString(R.string.spinner_folder_all), countOfAllImages)
        );
        for (String key : mapOfAllImageFolders.keySet()) {
            listOfAllImageFolders.add(String.format(getString(R.string.spinner_folder_name_count),
                    key, mapOfAllImageFolders.get(key))
            );
        }

        return listOfAllImageFolders;
    }

    // 갤러리에서 이미지 선택 여부에 따라 편집, 완료 버튼 활성화 / 비활성화
    public void setControlButton(boolean selectedImage) {
        if (selectedImage) {
            editMenuItem.setEnabled(true);
            editMenuItem.getIcon().setAlpha(255);
            doneMenuItem.setEnabled(true);
            doneMenuItem.getIcon().setAlpha(255);
        } else {
            editMenuItem.setEnabled(false);
            editMenuItem.getIcon().setAlpha(128);
            doneMenuItem.setEnabled(false);
            doneMenuItem.getIcon().setAlpha(128);
        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        gallerySpinner = findViewById(R.id.spinner_gallery);
    }

    // toolbar 초기 설정
    private void setupActionBar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_close_white_36);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    // 폴더명 목록을 보여줄 spinner 초기 설정
    private void setupSpinner() {
        List<String> folderNames = getFolderNames(this);

        // Adapter : 데이터를 화면에 보여지는 View에 연결하는 역할 = 화면에 데이터를 보여줍니다.
        // Spinner adapter에
        GallerySpinnerAdapter gallerySpinnerAdapter = new GallerySpinnerAdapter(this, folderNames);
        gallerySpinner.setAdapter(gallerySpinnerAdapter);

        // spinner에서 폴더명 선택했을 때 폴더명에 해당하는 이미지를 보여주도록 구현했습니다.
        gallerySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setControlButton(false);

                // spinner에 보여지는 {폴더명 (개수)} 에서
                // (개수)를 제외한 {폴더명}만 잘라냅니다.
                String item = parent.getItemAtPosition(position).toString().substring(0, parent.getItemAtPosition(position).toString().indexOf(" "));

                // 특정 폴더명 안에 있는 이미지만 가져오기 위해 newInstance의 인자로 {폴더명=item}을 전달합니다.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_gallery, GalleryFragment.newInstance(item))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no action
            }
        });
    }

    // Permission check
    private void setupCheckPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // no action
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