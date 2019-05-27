package kr.co.yogiyo.rookiephotoapp;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;

public class BaseActivity extends AppCompatActivity {

    protected static final int EDIT_SELECTED_PHOTO = 0;
    protected static final int EDIT_CAPTURED_PHOTO = 1;

    protected static final String STARTING_POINT = "startingPoint";

    protected static final File YOGIDIARY_PATH =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YogiDiary");

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected void destroy() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    protected void showToast(String toastMessage) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    protected void showToast(int stringId) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show();
    }
}
