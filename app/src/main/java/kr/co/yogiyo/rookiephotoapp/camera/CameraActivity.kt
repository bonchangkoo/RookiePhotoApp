package kr.co.yogiyo.rookiephotoapp.camera

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.otaliastudios.cameraview.*
import kotlinx.android.synthetic.main.activity_camera.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityCameraBinding
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryActivity

class CameraActivity : BaseActivity(), View.OnClickListener {

    companion object {
        val TAG = CameraActivity::class.java.simpleName as String
    }

    private lateinit var binding: ActivityCameraBinding

    private val viewModel: CameraViewModel by lazy {
        CameraViewModel(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.viewModel = viewModel

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

    override fun onClick(v: View?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        when (v?.id) {
            R.id.btn_go_diary -> {
                val intent = Intent(this, DiariesActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_flash -> {
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
            R.id.btn_timer -> {
                val captureDelayLabel = when (val captureDelay = viewModel.getNextCaptureDelay()) {
                    0 -> getString(R.string.text_timer)
                    else -> getString(R.string.text_timer_button_text_format).format(captureDelay)
                }
                viewModel.updateDelayButton(captureDelayLabel)
            }
            R.id.btn_change_camera -> {
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
            R.id.btn_go_gallery -> {
                val intent = Intent(this, GalleryActivity::class.java)
                intent.putExtra(STARTING_POINT, TAG)
                startActivity(intent)
            }
        }
    }

    private fun initView() {
        btn_go_diary.setOnClickListener(this)
        btn_flash.setOnClickListener(this)
        btn_timer.setOnClickListener(this)
        btn_change_camera.setOnClickListener(this)
        btn_go_gallery.setOnClickListener(this)
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
                            PreviewActivity.capturedImageBitmap = bitmap
                            val intent = Intent(this@CameraActivity, PreviewActivity::class.java)
                            startActivity(intent)
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
