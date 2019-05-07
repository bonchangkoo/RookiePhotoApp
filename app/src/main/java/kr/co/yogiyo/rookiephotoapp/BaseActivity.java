package kr.co.yogiyo.rookiephotoapp;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {


    protected void showToast(String toastMessage) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    protected void showToast(int stringId) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show();

    }

}
