package kr.co.yogiyo.rookiephotoapp.camera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;

import java.util.Arrays;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity;
import kr.co.yogiyo.rookiephotoapp.camera.capture.ResultHolder;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final List<Integer> DELAY_DURATIONS = Arrays.asList(0, 2, 5, 10);
    private static final int DEFAULT_DELAY = 0;

    private Handler timerHandler;

    private int captureTimer;
    private int currentCaptureID;
    private int captureDelay;
    private int flashType;

    private CameraKitView cameraKitView;
    private FrameLayout controlButtonsFrameLayout;
    private Button backButton;
    private Button flashButton;
    private Button timerButton;
    private Button captureButton;
    private Button changeCameraButton;
    private TextView timerMessageTextView;
    private FrameLayout darkScreenFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initView();

        initialize();
    }

    private void initView() {
        cameraKitView = findViewById(R.id.camera);
        controlButtonsFrameLayout = findViewById(R.id.frame_control_buttons);
        backButton = findViewById(R.id.btn_back);
        flashButton = findViewById(R.id.btn_flash);
        timerButton = findViewById(R.id.btn_timer);
        captureButton = findViewById(R.id.btn_capture);
        changeCameraButton = findViewById(R.id.btn_change_camera);
        timerMessageTextView = findViewById(R.id.text_timer_message);
        darkScreenFrame = findViewById(R.id.frame_dark_screen);

        cameraKitView.setOnClickListener(this);
        backButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        timerButton.setOnClickListener(this);
        captureButton.setOnClickListener(this);
        changeCameraButton.setOnClickListener(this);

        updateDelayButton();
    }

    private void initialize() {
        timerHandler = new Handler();
        captureTimer = 0;
        currentCaptureID = 0;
        captureDelay = DEFAULT_DELAY;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_flash:
                setFlashNext();
                break;
            case R.id.btn_timer:
                setCaptureDelayNext();
                break;
            case R.id.btn_capture:
                capture();
                break;
            case R.id.btn_change_camera:
                changeFacing();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (timerHandler.hasMessages(0)) {
            finishDelayCapture();
        } else {
            super.onBackPressed();
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
        if (timerHandler.hasMessages(0)) {
            finishDelayCapture();
        }
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

    private void captureNow() {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                ResultHolder.dispose();
                ResultHolder.setImage(bytes);
                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                startActivity(intent);
            }
        });
        startBlinkAnimation();
    }

    private void capture() {
        if (captureDelay == DEFAULT_DELAY) {
            captureNow();
        } else {
            captureAfterDelay(captureDelay);
        }
    }

    private void setCaptureDelayNext() {
        int index = DELAY_DURATIONS.indexOf(captureDelay);
        if (index < 0) {
            captureDelay = DEFAULT_DELAY;
        } else {
            captureDelay = DELAY_DURATIONS.get((index + 1) % DELAY_DURATIONS.size());
        }
        updateDelayButton();
    }

    private void updateDelayButton() {
        if (captureDelay==0) {
            timerButton.setText(getString(R.string.text_timer));
        } else {
            String timerButtonTextFormat = getString(R.string.text_timer_button_text_format);
            timerButton.setText(String.format(timerButtonTextFormat, captureDelay));
        }
    }

    private void captureAfterDelay(int delay) {
        timerMessageTextView.setVisibility(View.VISIBLE);
        captureTimer = delay;
        updateTimerMessage();
        currentCaptureID++;
        timerHandler.postDelayed(makeDecrementTimerFunction(currentCaptureID), 1000);

        setControlButtonsVisibility(false);
    }

    private Runnable makeDecrementTimerFunction(final int captureID) {
        return new Runnable() {
            public void run() {
                decrementTimer(captureID);
            }
        };
    }

    private void decrementTimer(final int captureID) {
        if (captureID != currentCaptureID) {
            return;
        }
        --captureTimer;
        if (captureTimer == 0) {
            captureNow();
            finishDelayCapture();
        } else if (captureTimer > 0) {
            updateTimerMessage();
            timerHandler.postDelayed(makeDecrementTimerFunction(captureID), 1000);
        }
    }

    private void updateTimerMessage() {
        String timerMessageFormat = getString(R.string.text_timer_message_format);
        timerMessageTextView.setText(String.format(timerMessageFormat, captureTimer));
    }

    private void setControlButtonsVisibility(boolean allowSave) {
        controlButtonsFrameLayout.setVisibility(allowSave ? View.VISIBLE : View.GONE);
    }

    private void finishDelayCapture() {
        timerHandler.removeCallbacksAndMessages(null);
        timerMessageTextView.setVisibility(View.GONE);
        setControlButtonsVisibility(true);
    }

    private void setFlashNext() {
        flashType = (flashType + 1) % Flash.values().length;
        switch (flashType) {
            case 0:
                cameraView.setFlash(Flash.OFF);
                updateFlashButton("");
                break;
            case 1:
                cameraView.setFlash(Flash.ON);
                updateFlashButton(Flash.ON.name());
                break;
            case 2:
                cameraView.setFlash(Flash.AUTO);
                updateFlashButton(Flash.AUTO.name());
                break;
            case 3:
                cameraView.setFlash(Flash.TORCH);
                updateFlashButton(Flash.TORCH.name());
                break;
            default:
                break;
        }
    }

    private void updateFlashButton(String flashTypeString) {
        String flashButtonTextFormat = getString(R.string.text_flash_button_text_format);
        flashButton.setText(String.format(flashButtonTextFormat, flashTypeString));
    }

    private void changeFacing() {
        if (cameraView.getFacing() == Facing.FRONT) {
            cameraView.setFacing(Facing.BACK);
            updateFacingButton(Facing.BACK.name());
        } else {
            cameraView.setFacing(Facing.FRONT);
            updateFacingButton(Facing.FRONT.name());
        }
    }

    private void updateFacingButton(String facingTypeString) {
        String facingButtonTextFormat = getString(R.string.text_facing_button_text_format);
        changeCameraButton.setText(String.format(facingButtonTextFormat, facingTypeString));
    }

    private void startBlinkAnimation() {
        darkScreenFrame.setVisibility(View.VISIBLE);

        Animation blinkAnimation = AnimationUtils.loadAnimation(CameraActivity.this, R.anim.blink);
        blinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                darkScreenFrame.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        darkScreenFrame.startAnimation(blinkAnimation);
    }
}
