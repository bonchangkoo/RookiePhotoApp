package kr.co.yogiyo.rookiephotoapp;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {

    protected static final int EDIT_SELECTED_PHOTO = 0;
    protected static final int EDIT_CAPTURED_PHOTO = 1;

    // 다이어리 추가/편집에서 사진 선택 요청할 때
    protected static final int REQUEST_DIARY_PHOTO_SELECT = 10;

    protected static final int RESULT_EDIT_PHOTO = 199;

    // 다이어리 사진이 저장되는 절대 경로
    protected static final String YOGIDIARY_PATH ="/storage/emulated/0/Pictures/YogiDiary/";

    protected void showToast(String toastMessage) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    protected void showToast(int stringId) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show();
    }
}
