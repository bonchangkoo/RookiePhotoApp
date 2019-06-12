package kr.co.yogiyo.rookiephotoapp.camera.capture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_preview.*
import kr.co.yogiyo.rookiephotoapp.*
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity
import java.io.ByteArrayOutputStream


class PreviewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        initImageView()
        initView()

    }

    private fun initImageView() {

        preview_image.run {
            visibility = View.VISIBLE
            setImageBitmap(capturedImageBitmap)
        }
    }

    private fun initView() {

        btn_back_capture.setOnClickListener {
            onBackPressed()
        }

        if (GlobalApplication.globalApplicationContext.fromDiary) {
            btn_add_diary.visibility = View.INVISIBLE
        }

        btn_add_diary.setOnClickListener {

            startActivity(Intent(this@PreviewActivity, DiariesActivity::class.java))

            val startDiaryEditActivityIntent = Intent(this@PreviewActivity, DiaryEditActivity::class.java).apply {
                putExtra("DIARY_IDX", -1)
                ByteArrayOutputStream().run {
                    capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
                    val byteArray = this.toByteArray()
                    putExtra("BITMAP_FROM_PREVIEW", byteArray)
                }
            }
            startActivity(startDiaryEditActivityIntent)
            finish()
        }

        btn_save_photo.setOnClickListener {
            if (GlobalApplication.globalApplicationContext.fromDiary) {
                val intent = Intent(this@PreviewActivity, CameraActivity::class.java)
                applicationContext.saveBitmapToInternalStorage(capturedImageBitmap)

                setResult(Constants.RESULT_CAPTURED_PHOTO, intent)
                finish()
            } else {
                capturedImageBitmap?.run {
                    applicationContext.bitmapToDownloads(this)
                    showToast(R.string.notification_image_saved)
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

    override fun onDestroy() {
        super.onDestroy()
        if (!capturedImageBitmap.isRecycled) {
            capturedImageBitmap.recycle()
        }
    }

    private fun editCapturedPhoto() {

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
                var uri: Uri = applicationContext.getImageUri(capturedImageBitmap)
                putExtra(getString(R.string.edit_photo_category_number), EDIT_CAPTURED_PHOTO)
                putExtra(getString(R.string.capture_photo_uri), uri)
            }

            if (GlobalApplication.globalApplicationContext.fromDiary) {
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
