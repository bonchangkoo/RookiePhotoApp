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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.otaliastudios.cameraview.Flash
import com.otaliastudios.cameraview.Grid
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraUtils
import com.otaliastudios.cameraview.SizeSelectors
import com.otaliastudios.cameraview.AspectRatio
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
import java.util.Calendar

class CameraActivity : BaseActivity() {

    private var backPressedStartTime = 0L

    private val cameraViewModel: CameraViewModel by lazy {
        CameraViewModel(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (DataBindingUtil.setContentView(this, R.layout.activity_camera) as ActivityCameraBinding)
                .viewModel = cameraViewModel

        initView()

        initCameraView()

        cameraViewModel.initViewModel()
    }

    override fun onResume() {
        super.onResume()
        cameraViewModel.run {
            initButtonVisibility()
            updateGalleryButton()
        }

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
            cameraViewModel.timerCancel() -> return
            btn_show_more.hasFocus() -> btn_show_more.clearFocus()
            btn_capture_size.hasFocus() -> btn_capture_size.clearFocus()
            else -> {
                backPressedStartTime = Calendar.getInstance().timeInMillis.also {
                    if (it - backPressedStartTime < 2000 ||
                            GlobalApplication.globalApplicationContext.isFromDiary) {
                        super.onBackPressed()
                    } else {
                        showSnackbar(relative_root, getString(R.string.text_finish_snackbar))
                    }
                }
            }
        }
    }

    private fun initView() {

        btn_go_diary.setOnClickListener {
            val intent = Intent(this, DiariesActivity::class.java)
            startActivity(intent)
        }

        relative_flash.setOnClickListener {

            when (cameraViewModel.getNextFlashType()) {
                0 -> {
                    camera.flash = Flash.OFF
                    cameraViewModel.updateFlashButton(getString(R.string.text_flash), R.drawable.baseline_flash_off_white_24)
                }
                1 -> {
                    camera.flash = Flash.ON
                    cameraViewModel.updateFlashButton(getString(R.string.text_flash_ON), R.drawable.baseline_flash_on_white_24)
                }
                2 -> {
                    camera.flash = Flash.AUTO
                    cameraViewModel.updateFlashButton(getString(R.string.text_flash_AUTO), R.drawable.baseline_flash_auto_white_24)
                }
                3 -> {
                    camera.flash = Flash.TORCH
                    cameraViewModel.updateFlashButton(getString(R.string.text_flash_TORCH), R.drawable.baseline_flash_on_white_24)
                }
            }
        }

        relative_go_gallery.run {
            setOnClickListener {
                val intent = Intent(this@CameraActivity, GalleryActivity::class.java)
                startActivity(intent)
            }
        }

        btn_one_to_one.setOnClickListener {
            cameraViewModel.updateViewByCaptureSize(false)
            setCaptureSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1, 1)
        }

        btn_three_to_four.setOnClickListener {
            cameraViewModel.updateViewByCaptureSize(false)
            setCaptureSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3, 4)
        }

        btn_nine_to_sixteen.setOnClickListener {
            cameraViewModel.updateViewByCaptureSize(true)
            setCaptureSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 9, 16)
        }

        relative_grid.setOnClickListener {
            camera.run {
                grid = when (cameraViewModel.getNextGridType()) {
                    0 -> {
                        cameraViewModel.updateGridButton(context.getString(R.string.text_grid),
                                if (cameraViewModel.isCaptureSizeFull()) {
                                    R.drawable.baseline_grid_off_white_36
                                } else {
                                    R.drawable.baseline_grid_off_black_36
                                })
                        Grid.OFF
                    }
                    1 -> {
                        cameraViewModel.updateGridButton(context.getString(R.string.text_grid_three_by_three),
                                if (cameraViewModel.isCaptureSizeFull()) {
                                    R.drawable.baseline_grid_on_white_36
                                } else {
                                    R.drawable.baseline_grid_on_black_36
                                })
                        Grid.DRAW_3X3
                    }
                    2 -> {
                        cameraViewModel.updateGridButton(context.getString(R.string.text_grid_four_by_four),
                                if (cameraViewModel.isCaptureSizeFull()) {
                                    R.drawable.baseline_grid_on_white_36
                                } else {
                                    R.drawable.baseline_grid_on_black_36
                                })
                        Grid.DRAW_4X4
                    }
                    else -> {
                        cameraViewModel.updateGridButton(context.getString(R.string.text_grid_phi),
                                if (cameraViewModel.isCaptureSizeFull()) {
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
                cameraViewModel.updateShowMoreLayout(if (relative_more_control_buttons.visibility == View.VISIBLE) {
                    clearFocus()
                    View.GONE
                } else {
                    requestFocus()
                    View.VISIBLE
                })
            }

            setOnFocusChangeListener { _, hasFocus ->
                cameraViewModel.updateShowMoreLayout(if (!hasFocus) View.GONE else View.VISIBLE)
            }
        }

        btn_capture_size.run {
            setOnClickListener {
                cameraViewModel.updateShowCaptureSizeLayout(if (relative_capture_size_buttons.visibility == View.VISIBLE) {
                    clearFocus()
                    View.GONE
                } else {
                    requestFocus()
                    View.VISIBLE
                })
            }

            setOnFocusChangeListener { _, hasFocus ->
                cameraViewModel.updateShowCaptureSizeLayout(if (!hasFocus) View.GONE else View.VISIBLE) }
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
                            if (GlobalApplication.globalApplicationContext.isFromDiary) {
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
        updateGalleryButton = {
            GalleryFragment.loadImages("YogiDiary").run {
                Glide.with(this@CameraActivity)
                        .load(if (isEmpty()) null else this[0].pathOfImage)
                        .error(if (cameraViewModel.isCaptureSizeFull()) {
                            R.drawable.baseline_collections_white_24
                        } else {
                            R.drawable.baseline_collections_black_24
                        })
                        .apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(btn_go_gallery)
            }
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