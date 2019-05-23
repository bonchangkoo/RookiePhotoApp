package kr.co.yogiyo.rookiephotoapp.camera.capture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_preview.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity
import java.io.ByteArrayOutputStream

class PreviewActivity : BaseActivity() {

    private lateinit var capturedImageBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        initImageView()
        initView()
    }

    private fun initImageView() {

        capturedImageBitmap = ResultHolder.bitmap

        preview_image.visibility = View.VISIBLE
        preview_image.setImageBitmap(capturedImageBitmap)

        capturedImageBitmap.run {
            showToast(String.format("width: %d, height: %d \nsize : %f MB",
                    capturedImageBitmap.width, capturedImageBitmap.height, getApproximateFileMegabytes(capturedImageBitmap)))
        }
    }

    private fun initView() {

        btn_back_capture.setOnClickListener {
            onBackPressed()
        }

        btn_edit.setOnClickListener {
            editCapturedPhoto()
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
            val uri = getImageUri(this@PreviewActivity, capturedImageBitmap)

            val doStartEditPhotoActivityIntent = Intent(this, EditPhotoActivity::class.java).apply {
                putExtra(getString(R.string.edit_photo_category_number), EDIT_CAPTURED_PHOTO)
                putExtra(getString(R.string.capture_photo_uri), uri)
            }
            startActivity(doStartEditPhotoActivityIntent)
            finish()
        }
    }

    // Bitmap을 Uri로 변환하는 함수
    private fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Image", null)
        return Uri.parse(path)
    }

    companion object {

        private const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102

        private fun getApproximateFileMegabytes(bitmap: Bitmap): Float {
            return (bitmap.rowBytes * bitmap.height / 1024 / 1024).toFloat()

        }
    }
}
