package kr.co.yogiyo.rookiephotoapp.camera.capture

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_preview.*
import kr.co.yogiyo.rookiephotoapp.*
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityPreviewBinding
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity


class PreviewActivity : BaseActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var previewViewModel: PreviewViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previewViewModel = ViewModelProviders.of(this).get(PreviewViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview)
        binding.viewModel = previewViewModel

        initView()
    }

    private fun initView() {

        btn_back_capture.setOnClickListener {
            onBackPressed()
        }

        preview_image.run {
            visibility = View.VISIBLE
            setImageBitmap(capturedImageBitmap)
        }

        if (GlobalApplication.globalApplicationContext.isFromDiary) {
            btn_add_diary.visibility = View.INVISIBLE

        }

        btn_add_diary.apply {
            visibility = previewViewModel.getAddDiaryVisibility()
            setOnClickListener {
                startActivity(Intent(this@PreviewActivity, DiariesActivity::class.java))

                val startDiaryEditActivityIntent = Intent(this@PreviewActivity, DiaryEditActivity::class.java).apply {
                    putExtra("DIARY_IDX", -1)
                    putExtra("FROM_PREVIEW", "pass bitmap")
                }
                startActivity(startDiaryEditActivityIntent)
                finish()
            }
        }

        btn_save_photo.setOnClickListener {
            if (GlobalApplication.globalApplicationContext.isFromDiary) {
                val intent = Intent(this@PreviewActivity, CameraActivity::class.java)
                applicationContext.saveBitmapToInternalStorage(capturedImageBitmap)
                setResult(Constants.RESULT_CAPTURED_PHOTO, intent)
                finish()
            } else {
                capturedImageBitmap.let {
                    val isSaveBitmap = applicationContext.bitmapToDownloads(it)
                    when {
                        isSaveBitmap -> {
                            showToast(R.string.notification_image_saved)
                        }
                        else -> {
                            showToast(getString(R.string.text_no_save_preview_image))
                            finish()
                        }
                    }
                }
            }
        }

        btn_edit.setOnClickListener {
            editCapturedPhoto()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                data?.let {
                    val intent = Intent(this@PreviewActivity, CameraActivity::class.java).apply {
                        this.data = it.data
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }


    private fun editCapturedPhoto() {

        // TODO : 후에 삭제할 코드
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION
            )
        } else {
            val doStartEditPhotoActivityIntent = Intent(this, EditPhotoActivity::class.java).apply {
                val uri: Uri = applicationContext.getImageUri(capturedImageBitmap)
                putExtra(getString(R.string.edit_photo_category_number), EDIT_CAPTURED_PHOTO)
                putExtra(getString(R.string.capture_photo_uri), uri)
            }

            if (GlobalApplication.globalApplicationContext.isFromDiary) {
                startActivityForResult(doStartEditPhotoActivityIntent, Constants.REQUEST_DIARY_CAPTURE_PHOTO)
            } else {
                startActivity(doStartEditPhotoActivityIntent)
                finish()
            }
        }
    }

    companion object {

        lateinit var capturedImageBitmap: Bitmap
        private const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102

    }
}
