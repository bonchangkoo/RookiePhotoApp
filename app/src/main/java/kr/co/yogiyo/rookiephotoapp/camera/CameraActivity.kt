package kr.co.yogiyo.rookiephotoapp.camera

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.otaliastudios.cameraview.*
import kotlinx.android.synthetic.main.activity_camera.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityCameraBinding
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryActivity

class CameraActivity : BaseActivity() {

    private lateinit var binding: ActivityCameraBinding

    private val viewModel: CameraViewModel by lazy {
        CameraViewModel(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.viewModel = viewModel

        if (globalApp.fromDiary == null) {
            globalApp.fromDiary = false
        }

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

        if (globalApp.fromDiary) {
            btn_go_diary.visibility = View.INVISIBLE
            btn_go_diary.visibility = View.INVISIBLE
        }

        btn_go_diary.setOnClickListener {
            val intent = Intent(this, DiariesActivity::class.java)
            startActivity(intent)
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

        btn_go_gallery.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initCameraView() {
        camera.run {
            addCameraListener(object : CameraListener() {
                override fun onPictureTaken(jpeg: ByteArray?) {
                    if (jpeg != null) {
                        CameraUtils.decodeBitmap(jpeg, CameraUtils.BitmapCallback { bitmap ->
                            if (bitmap == null) {
                                finish()
                                return@BitmapCallback
                            }
                            val intent = Intent(this@CameraActivity, PreviewActivity::class.java)
                            PreviewActivity.capturedImageBitmap = bitmap
                            if (globalApp.fromDiary) {
                                startActivityForResult(intent, Constants.REQUEST_DIARY_CAPTURE_PHOTO)
                            } else {
                                startActivity(intent)
                            }
                        })
                    }
                }

            })

            mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER)

            val ratio = SizeSelectors.aspectRatio(AspectRatio.of(3, 4), 0f)
            val result = SizeSelectors.or(ratio, SizeSelectors.biggest())
            setPictureSize(result)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val intent = Intent(this@CameraActivity, DiaryEditActivity::class.java).apply {
                        this.data = it.data
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                Constants.RESULT_CAPTURED_PHOTO -> {
                    val intent = Intent(this@CameraActivity, DiaryEditActivity::class.java)
                    setResult(Constants.RESULT_CAPTURED_PHOTO, intent)
                    finish()
                }
            }
        }
    }

    private fun CameraViewModel.initViewModel() {
        captureNow = {
            camera.capturePicture()
            startBlinkAnimation()
        }
    }

    private fun startBlinkAnimation() {
        val blinkAnimation = AnimationUtils.loadAnimation(this@CameraActivity, R.anim.blink)
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

        frame_dark_screen.run {
            visibility = View.VISIBLE
            startAnimation(blinkAnimation)
        }
    }
}
