package kr.co.yogiyo.rookiephotoapp.edit;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.yalantis.ucrop.view.UCropView;

import java.io.File;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;

public class EditResultActivity extends BaseActivity {

    private static final String TAG = EditResultActivity.class.getSimpleName();
    private Uri getEditPhotoUri;

    public static void startWithUri(@NonNull Context context, @NonNull Uri uri) {
        Intent intent = new Intent(context, EditResultActivity.class);
        intent.setData(uri);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_result);

        setUCropView();
        setSettingAndResultActionBar();
    }

    private void setUCropView() {
        getEditPhotoUri = getIntent().getData();
        if (getEditPhotoUri != null) {
            try {
                UCropView uCropView = findViewById(R.id.ucrop);
                uCropView.getCropImageView().setImageUri(getEditPhotoUri, null);
                uCropView.getCropImageView().setScaleEnabled(false);
                uCropView.getCropImageView().setRotateEnabled(false);
                uCropView.getOverlayView().setShowCropFrame(false);
                uCropView.getOverlayView().setShowCropGrid(false);
                uCropView.getOverlayView().setDimmedColor(Color.TRANSPARENT);
            } catch (Exception e) {
                Log.e(TAG, "setImageUri", e);
                showToast(e.getMessage());
            }
        }
    }

    private void setSettingAndResultActionBar() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(new File(getEditPhotoUri.getPath()).getAbsolutePath(), options);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_note_add_white_36); // 왼쪽에 아이콘 배치(홈 아이콘 대체)
            actionBar.setTitle(getString(R.string.format_crop_result_d_d, options.outWidth, options.outHeight));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_download) {
            // 이미지 저장
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
