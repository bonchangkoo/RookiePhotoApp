package kr.co.yogiyo.rookiephotoapp.camera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;

import kr.co.yogiyo.rookiephotoapp.R;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraKitView cameraKitView;
    private Button backButton;
    private Button flashButton;
    private Button timerButton;
    private Button captureButton;
    private Button changeCameraButton;
    private TextView wariningTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initView();
    }

    private void initView() {
        cameraKitView = findViewById(R.id.camera);
        backButton = findViewById(R.id.btn_back);
        flashButton = findViewById(R.id.btn_flash);
        timerButton = findViewById(R.id.btn_timer);
        captureButton = findViewById(R.id.btn_capture);
        changeCameraButton = findViewById(R.id.btn_change_camera);
        wariningTextView = findViewById(R.id.tv_warning);

        cameraKitView.setOnClickListener(this);
        backButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        timerButton.setOnClickListener(this);
        captureButton.setOnClickListener(this);
        changeCameraButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                break;
            case R.id.btn_flash:
                if (cameraKitView.getFlash() == CameraKit.FLASH_OFF) {
                    cameraKitView.setFlash(CameraKit.FLASH_ON);
                } else {
                    cameraKitView.setFlash(CameraKit.FLASH_OFF);
                }
                break;
            case R.id.btn_timer:
                break;
            case R.id.btn_capture:
                break;
            case R.id.btn_change_camera:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
