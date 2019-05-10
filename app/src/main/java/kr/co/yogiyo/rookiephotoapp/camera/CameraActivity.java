package kr.co.yogiyo.rookiephotoapp.camera;

import android.content.Intent;

import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.otaliastudios.cameraview.AspectRatio;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.SizeSelector;
import com.otaliastudios.cameraview.SizeSelectors;

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

    private CameraView cameraView;
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
        cameraView = findViewById(R.id.camera);
        controlButtonsFrameLayout = findViewById(R.id.frame_control_buttons);
        backButton = findViewById(R.id.btn_back);
        flashButton = findViewById(R.id.btn_flash);
        timerButton = findViewById(R.id.btn_timer);
        captureButton = findViewById(R.id.btn_capture);
        changeCameraButton = findViewById(R.id.btn_change_camera);
        timerMessageTextView = findViewById(R.id.text_timer_message);
        darkScreenFrame = findViewById(R.id.frame_dark_screen);

        backButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        timerButton.setOnClickListener(this);
        captureButton.setOnClickListener(this);
        changeCameraButton.setOnClickListener(this);

        initCameraView();

        updateDelayButton();
    }

    private void initCameraView() {
        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                if (jpeg != null) {
                    CameraUtils.decodeBitmap(jpeg, new CameraUtils.BitmapCallback() {
                        @Override
                        public void onBitmapReady(Bitmap bitmap) {
                            if (bitmap == null) {
                                finish();
                                return;
                            }
                            ResultHolder.setBitmap(bitmap);
                            Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER);

        SizeSelector ratio = SizeSelectors.aspectRatio(AspectRatio.of(3, 4), 0);
        SizeSelector result = SizeSelectors.or(ratio, SizeSelectors.biggest());
        cameraView.setPictureSize(result);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
        if (timerHandler.hasMessages(0)) {
            finishDelayCapture();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    private void captureNow() {
        cameraView.capturePicture();
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
        if (captureDelay == 0) {
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
