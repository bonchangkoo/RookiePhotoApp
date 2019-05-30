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
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PreviewActivity : BaseActivity() {

    var captrueImageUri: Uri? = null

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

        capturedImageBitmap.run {
            showToast(String.format("width: %d, height: %d \nsize : %f MB",
                    this.width, this.height, getApproximateFileMegabytes(this)))
        }

        captrueImageUri = getImageUri(this@PreviewActivity, capturedImageBitmap)
    }

    private fun initView() {

        btn_back_capture.setOnClickListener {
            onBackPressed()
        }

        btn_add_diary.setOnClickListener {

            startActivity(Intent(this@PreviewActivity, DiariesActivity::class.java))

            val startDiaryEditActivityIntent = Intent(this@PreviewActivity, DiaryEditActivity::class.java).apply {
                putExtra("DIARY_IDX", -1)
                val stream = ByteArrayOutputStream()
                capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                putExtra("BITMAP_FROM_PREVIEW", byteArray)
            }
            startActivity(startDiaryEditActivityIntent)
            finish()
        }

        btn_save_photo.setOnClickListener {
            captrueImageUri?.run {
                FileToDownloads(capturedImageBitmap)
            }
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

            val doStartEditPhotoActivityIntent = Intent(this, EditPhotoActivity::class.java).apply {

                var uri: Uri = getImageUri(this@PreviewActivity, capturedImageBitmap)
                putExtra(getString(R.string.edit_photo_category_number), EDIT_CAPTURED_PHOTO)
                putExtra(getString(R.string.capture_photo_uri), uri)
            }
            startActivity(doStartEditPhotoActivityIntent)
            finish()
        }
    }

    @Throws(Exception::class)
    private fun FileToDownloads(bitmap: Bitmap) {
        if (!YOGIDIARY_PATH.exists()) {
            if (!YOGIDIARY_PATH.mkdirs()) {
                return finish()
            }
        }

        val downloadsDirectoryPath = YOGIDIARY_PATH.path + "/"
        val filename = String.format(Locale.getDefault(), "%d%s", Calendar.getInstance().timeInMillis, ".jpg")

        val file = File(downloadsDirectoryPath, filename)

        val outStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))


        showToast(R.string.notification_image_saved)
        finish()
    }

    // Bitmap을 Uri로 변환하는 함수
    private fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Image", null)
        return Uri.parse(path)
    }

    companion object {

        lateinit var capturedImageBitmap: Bitmap

        private const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102

        private fun getApproximateFileMegabytes(bitmap: Bitmap) =
                (bitmap.rowBytes * bitmap.height / 1024 / 1024).toFloat()
    }
}
