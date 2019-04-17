package kr.co.yogiyo.rookiephotoapp.camera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;

import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity;
import kr.co.yogiyo.rookiephotoapp.camera.capture.ResultHolder;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static final float STANDARD_SENSOR_LOACTION = 5.0f;

    private SensorManager verticalRecognitionSensorManager;
    private Sensor verticalRecognitionSensor;

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

        verticalRecognitionSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        verticalRecognitionSensor = verticalRecognitionSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


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
                onBackPressed();
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
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                        ResultHolder.dispose();
                        ResultHolder.setImage(bytes);
                        Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                        startActivity(intent);
                    }
                });
                break;
            case R.id.btn_change_camera:
                if (cameraKitView.getFacing() == CameraKit.FACING_FRONT) {
                    cameraKitView.setFacing(CameraKit.FACING_BACK);
                } else {
                    cameraKitView.setFacing(CameraKit.FACING_FRONT);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
        verticalRecognitionSensorManager.registerListener(this, verticalRecognitionSensor, SensorManager.SENSOR_DELAY_UI);
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


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];

                if ((x > STANDARD_SENSOR_LOACTION && y < STANDARD_SENSOR_LOACTION) || (x < -STANDARD_SENSOR_LOACTION && y > -STANDARD_SENSOR_LOACTION)) {
                    wariningTextView.setVisibility(View.INVISIBLE);
                } else if ((x > -STANDARD_SENSOR_LOACTION && y > STANDARD_SENSOR_LOACTION) || (x < STANDARD_SENSOR_LOACTION && y < -STANDARD_SENSOR_LOACTION)) {
                    wariningTextView.setVisibility(View.VISIBLE);
                }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
