package kr.co.yogiyo.rookiephotoapp.camera

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.otaliastudios.cameraview.*
import kotlinx.android.synthetic.main.activity_camera_v2.*
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.camera.capture.ResultHolder
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityCameraV2Binding

class CameraV2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraV2Binding

    private val viewModel: CameraV2ViewModel by lazy {
        CameraV2ViewModel(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_v2)
        binding.viewmodel = viewModel

        initView()

        initCameraView()

        viewModel.initViewModel()
    }

    override fun onResume() {
        super.onResume()
        camera.start()
    }

    override fun onPause() {
        super.onPause()
        camera.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.destroy()
    }

    private fun initView() {
        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_flash.setOnClickListener {
            when (viewModel.getNextFlashType()) {
                0 -> {
                    camera.flash = Flash.OFF
                    viewModel.updateFlashButton(Flash.OFF.name)
                }
                1 -> {
                    camera.flash = Flash.ON
                    viewModel.updateFlashButton(Flash.ON.name)
                }
                2 -> {
                    camera.flash = Flash.AUTO
                    viewModel.updateFlashButton(Flash.AUTO.name)
                }
                3 -> {
                    camera.flash = Flash.TORCH
                    viewModel.updateFlashButton(Flash.TORCH.name)
                }
            }
        }

        btn_timer.setOnClickListener {
            val captureDelayLabel = when (val captureDelay = viewModel.getNextCaptureDelay()) {
                        0 -> getString(R.string.text_timer)
                        else -> getString(R.string.text_timer_button_text_format).format(captureDelay)
                    }
            viewModel.updateDelayButton(captureDelayLabel)
        }

        btn_change_camera.setOnClickListener {
            when {
                camera.facing == Facing.FRONT -> {
                    camera.facing = Facing.BACK
                    viewModel.updateFacingButton(Facing.BACK.name)
                }
                else -> {
                    camera.facing = Facing.FRONT
                    viewModel.updateFacingButton(Facing.FRONT.name)
                }
            }
        }
    }

    private fun initCameraView() {
        camera.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                if (jpeg != null) {
                    CameraUtils.decodeBitmap(jpeg, CameraUtils.BitmapCallback { bitmap ->
                        if (bitmap == null) {
                            finish()
                            return@BitmapCallback
                        }
                        ResultHolder.dispose()
                        ResultHolder.setBitmap(bitmap)
                        val intent = Intent(this@CameraV2Activity, PreviewActivity::class.java)
                        startActivity(intent)
                    })
                }
            }
        })
        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER)

        val ratio = SizeSelectors.aspectRatio(AspectRatio.of(3, 4), 0f)
        val result = SizeSelectors.or(ratio, SizeSelectors.biggest())
        camera.setPictureSize(result)
    }

    private fun CameraV2ViewModel.initViewModel() {
        captureNow = {
            camera.capturePicture()
            startBlinkAnimation()
        }
    }

    private fun startBlinkAnimation() {
        frame_dark_screen.visibility = View.VISIBLE

        val blinkAnimation = AnimationUtils.loadAnimation(this@CameraV2Activity, R.anim.blink)
        blinkAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // Do nothing
            }

            override fun onAnimationEnd(animation: Animation) {
                frame_dark_screen.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {
                // Do nothing
            }
        })
        frame_dark_screen.startAnimation(blinkAnimation)
    }
}
