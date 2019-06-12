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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.Constants;
import kr.co.yogiyo.rookiephotoapp.GlobalApplication;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;

public class GalleryActivity extends BaseActivity implements View.OnClickListener {

    private static final int EDIT_SELECTED_GALLERY_PHOTO = 11111;

    private ImageButton editButton;
    private ImageButton doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initView();

        setupCheckPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_DIARY_PICK_GALLERY) {
                Intent intent = new Intent(GalleryActivity.this, DiaryEditActivity.class);
                intent.setData(data.getData());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        Fragment nowFragment = getSupportFragmentManager().findFragmentById(R.id.frame_gallery);
        switch (v.getId()) {
            case R.id.btn_close:
                onBackPressed();
                break;
            case R.id.btn_edit:
                if (nowFragment instanceof GalleryFragment) {
                    Uri uriForEdit = ((GalleryFragment) nowFragment).getSelectedImageUri();
                    if (uriForEdit != null) {
                        setControlButtonEnabled(false);
                        Intent doStartEditPhotoActivityIntent = new Intent(GalleryActivity.this, EditPhotoActivity.class);
                        doStartEditPhotoActivityIntent.putExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO);
                        doStartEditPhotoActivityIntent.setData(uriForEdit);
                        if (GlobalApplication.getGlobalApplicationContext().isFromDiary()) {
                            startActivityForResult(doStartEditPhotoActivityIntent, Constants.REQUEST_DIARY_PICK_GALLERY);
                        } else {
                            startActivity(doStartEditPhotoActivityIntent);
                            finish();
                        }

                    } else {
                        showToast(R.string.toast_cannot_retrieve_selected_image);
                    }
                }
                break;
            case R.id.btn_done:
                if (nowFragment instanceof GalleryFragment) {
                    Uri originalUri = ((GalleryFragment) nowFragment).getSelectedImageUri();
                    if (originalUri != null) {
                        Intent originalUriIntent = new Intent(GalleryActivity.this, DiaryEditActivity.class);
                        originalUriIntent.setData(originalUri);
                        setResult(Activity.RESULT_OK, originalUriIntent);
                        finish();
                    } else {
                        showToast(R.string.toast_cannot_retrieve_selected_image);
                    }
                }
                break;
        }
    }

    public List<String> getFolderNames(Activity activity) {
        Map<String, Integer> mapOfAllImageFolders = new HashMap<>();
        List<String> listOfAllImageFolders = new ArrayList<>();
        int countOfAllImages = 0;
        String folderName;

        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return listOfAllImageFolders;
        }

        int columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            countOfAllImages++;
            folderName = cursor.getString(columnIndexFolderName);
            if (mapOfAllImageFolders.containsKey(folderName)) {
                mapOfAllImageFolders.put(folderName, mapOfAllImageFolders.get(folderName) + 1);
            } else {
                mapOfAllImageFolders.put(folderName, 1);
            }
        }

        cursor.close();

        if (mapOfAllImageFolders.containsKey("YogiDiary")) {
            listOfAllImageFolders.add("");
        }

        for (String key : mapOfAllImageFolders.keySet()) {

            if (key.equals("YogiDiary")) {
                listOfAllImageFolders.set(0, String.format(getString(R.string.spinner_folder_name_count),
                        key, mapOfAllImageFolders.get(key)));
                continue;
            }
            listOfAllImageFolders.add(String.format(getString(R.string.spinner_folder_name_count),
                    key, mapOfAllImageFolders.get(key))
            );
        }

        listOfAllImageFolders.add(String.format(getString(R.string.spinner_folder_name_count),
                getString(R.string.spinner_folder_all), countOfAllImages));

        return listOfAllImageFolders;
    }

    public void setControlButtonEnabled(boolean selectedImage) {
        if (selectedImage) {
            editButton.setEnabled(true);
            editButton.setAlpha(255);
            doneButton.setEnabled(true);
            doneButton.setAlpha(255);
        } else {
            editButton.setEnabled(false);
            editButton.setAlpha(128);
            doneButton.setEnabled(false);
            doneButton.setAlpha(128);
        }
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageButton closeButton = findViewById(R.id.btn_close);
        Spinner gallerySpinner = findViewById(R.id.spinner_gallery);
        editButton = findViewById(R.id.btn_edit);
        doneButton = findViewById(R.id.btn_done);

        if (!GlobalApplication.getGlobalApplicationContext().isFromDiary()) {
            doneButton.setVisibility(View.GONE);
        }

        closeButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        List<String> folderNames = getFolderNames(this);

        // TODO : Spinner 목록 색 변경 필요
        // TODO : Spinner 에 보여지는 텍스트 2가지로 구분하는게 좋을 것 같음 -> 폴더명... (이미지 수)
        ArrayAdapter<String> gallerySpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, folderNames);
        gallerySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gallerySpinner.setAdapter(gallerySpinnerAdapter);
        gallerySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setControlButtonEnabled(false);

                String item = parent.getItemAtPosition(position).toString().substring(0, parent.getItemAtPosition(position).toString().lastIndexOf(" "));

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_gallery, GalleryFragment.newInstance(item))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupCheckPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // Do nothing
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
            }
        };

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("단말기의 [설정] > [권한]에서 접근 권한을 ON으로 설정해주세요.\n\n* 카메라\n* 저장공간")
                .setGotoSettingButtonText("설정")
                .setDeniedCloseButtonText("취소")
                .setPermissions(permissions.toArray(new String[0]))
                .check();
    }
}