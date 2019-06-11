package kr.co.yogiyo.rookiephotoapp.camera

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.otaliastudios.cameraview.*
import kotlinx.android.synthetic.main.activity_camera.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityCameraBinding
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryActivity
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryFragment

class CameraActivity : BaseActivity() {

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
        viewModel.initButtonVisibility()
        initGoGalleryButton()

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

    override fun onBackPressed() {
        when {
            viewModel.timerCancel() -> return
            btn_show_more.hasFocus() -> btn_show_more.clearFocus()
            btn_capture_size.hasFocus() -> btn_capture_size.clearFocus()
            else -> super.onBackPressed()
        }
    }

    private fun initView() {
        if (GlobalApplication.globalApplicationContext.fromDiary) {
            btn_go_diary.visibility = View.INVISIBLE
            btn_go_gallery.visibility = View.INVISIBLE
        }

        btn_go_diary.setOnClickListener {
            val intent = Intent(this, DiariesActivity::class.java)
            startActivity(intent)
        }

        btn_flash.setOnClickListener {

            when (viewModel.getNextFlashType()) {
                0 -> {
                    camera.flash = Flash.OFF
                    viewModel.updateFlashButton(getString(R.string.text_flash), R.drawable.baseline_flash_off_white_24)
                }
                1 -> {
                    camera.flash = Flash.ON
                    viewModel.updateFlashButton(getString(R.string.text_flash_ON), R.drawable.baseline_flash_on_white_24)
                }
                2 -> {
                    camera.flash = Flash.AUTO
                    viewModel.updateFlashButton(getString(R.string.text_flash_AUTO), R.drawable.baseline_flash_auto_white_24)
                }
                3 -> {
                    camera.flash = Flash.TORCH
                    viewModel.updateFlashButton(getString(R.string.text_flash_TORCH), R.drawable.baseline_flash_on_white_24)
                }
            }
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

        btn_go_gallery.run {
            setOnClickListener {
                val intent = Intent(this@CameraActivity, GalleryActivity::class.java)
                startActivity(intent)
            }
        }

        btn_one_to_one.setOnClickListener {
            viewModel.updateViewByCaptureSize(false)
            setCaptureSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1, 1)
        }

        btn_three_to_four.setOnClickListener {
            viewModel.updateViewByCaptureSize(false)
            setCaptureSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3, 4)
        }

        btn_nine_to_sixteen.setOnClickListener {
            viewModel.updateViewByCaptureSize(true)
            setCaptureSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 9, 16)
        }

        btn_grid.setOnClickListener {
            camera.run {
                grid = when (viewModel.getNextGridType()) {
                    0 -> {
                        viewModel.updateGridButton(context.getString(R.string.text_grid),
                                if (viewModel.isCaptureSizeFull()) {
                                    R.drawable.baseline_grid_off_white_36
                                } else {
                                    R.drawable.baseline_grid_off_black_36
                                })
                        Grid.OFF
                    }
                    1 -> {
                        viewModel.updateGridButton(context.getString(R.string.text_grid_three_by_three),
                                if (viewModel.isCaptureSizeFull()) {
                                    R.drawable.baseline_grid_on_white_36
                                } else {
                                    R.drawable.baseline_grid_on_black_36
                                })
                        Grid.DRAW_3X3
                    }
                    2 -> {
                        viewModel.updateGridButton(context.getString(R.string.text_grid_four_by_four),
                                if (viewModel.isCaptureSizeFull()) {
                                    R.drawable.baseline_grid_on_white_36
                                } else {
                                    R.drawable.baseline_grid_on_black_36
                                })
                        Grid.DRAW_4X4
                    }
                    else -> {
                        viewModel.updateGridButton(context.getString(R.string.text_grid_phi),
                                if (viewModel.isCaptureSizeFull()) {
                                    R.drawable.baseline_grid_on_white_36
                                } else {
                                    R.drawable.baseline_grid_on_black_36
                                })
                        Grid.DRAW_PHI
                    }
                }
            }
        }

        btn_show_more.run {
            setOnClickListener {
                viewModel.updateShowMoreLayout(if (relative_more_control_buttons.visibility == View.VISIBLE) {
                    clearFocus()
                    View.GONE
                } else {
                    requestFocus()
                    View.VISIBLE
                })
            }

            setOnFocusChangeListener { _, hasFocus ->
                viewModel.updateShowMoreLayout(if (!hasFocus) View.GONE else View.VISIBLE)
            }
        }

        btn_capture_size.run {
            setOnClickListener {
                viewModel.updateShowCaptureSizeLayout(if (relative_capture_size_buttons.visibility == View.VISIBLE) {
                    clearFocus()
                    View.GONE
                } else {
                    requestFocus()
                    View.VISIBLE
                })
            }

            setOnFocusChangeListener { _, hasFocus -> viewModel.updateShowCaptureSizeLayout(if (!hasFocus) View.GONE else View.VISIBLE) }
        }

        relative_root.run {
            requestFocus()

            setOnClickListener {
                when {
                    btn_show_more.hasFocus() -> btn_show_more.clearFocus()
                    btn_capture_size.hasFocus() -> btn_capture_size.clearFocus()
                }
            }
        }
    }

    private fun initCameraView() {
        camera.run {
            addCameraListener(object : CameraListener() {
                override fun onPictureTaken(jpeg: ByteArray?) {
                    if (jpeg != null) {
                        startBlinkAnimation()
                        CameraUtils.decodeBitmap(jpeg, CameraUtils.BitmapCallback { bitmap ->
                            if (bitmap == null) {
                                finish()
                                return@BitmapCallback
                            }
                            val intent = Intent(this@CameraActivity, PreviewActivity::class.java)
                            PreviewActivity.capturedImageBitmap = bitmap
                            if (GlobalApplication.globalApplicationContext.fromDiary) {
                                startActivityForResult(intent, Constants.REQUEST_DIARY_CAPTURE_PHOTO)
                            } else {
                                startActivity(intent)
                            }
                        })
                    }
                }

                override fun onFocusStart(point: PointF?) {
                    when {
                        btn_show_more.hasFocus() -> btn_show_more.clearFocus()
                        btn_capture_size.hasFocus() -> btn_capture_size.clearFocus()
                        else -> super.onFocusStart(point)
                    }
                }
            })
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
        captureNow = { camera.capturePicture() }
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

    private fun initGoGalleryButton() {
        GalleryFragment.loadImages(this@CameraActivity, "YogiDiary").run {
            Glide.with(this@CameraActivity)
                    .load(if (isEmpty()) null else this[0].pathOfImage)
                    .error(if (viewModel.isCaptureSizeFull()) {
                        R.drawable.baseline_collections_white_24
                    } else {
                        R.drawable.baseline_collections_black_24
                    })
                    .into(btn_go_gallery)
        }
    }

    private fun setCaptureSize(width: Int, height: Int, x: Int, y: Int) {
        camera.run {
            stop()
            layoutParams.width = width
            layoutParams.height = height
            val ratio = SizeSelectors.aspectRatio(AspectRatio.of(x, y), 0f)
            val result = SizeSelectors.or(ratio, SizeSelectors.biggest())
            setPictureSize(result)
            start()
        }
    }
}