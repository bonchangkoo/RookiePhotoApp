package kr.co.yogiyo.rookiephotoapp.camera.capture

import android.Manifest
import android.app.Activity
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
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.Constants.YOGIDIARY_PATH
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

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

        capturedImageBitmap.run {
            showToast(String.format("width: %d, height: %d \nsize : %f MB",
                    this.width, this.height, getApproximateFileMegabytes(this)))
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
                val stream = ByteArrayOutputStream()
                capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                putExtra("BITMAP_FROM_PREVIEW", byteArray)
            }
            startActivity(startDiaryEditActivityIntent)
            finish()
        }

        btn_save_photo.setOnClickListener {
            if (GlobalApplication.globalApplicationContext.fromDiary) {
                val intent = Intent(this@PreviewActivity, CameraActivity::class.java)
                saveBitmapToInternalStorage(applicationContext, capturedImageBitmap)
                setResult(Constants.RESULT_CAPTURED_PHOTO, intent)
                finish()
            } else {
                capturedImageBitmap.run {
                    bitmapToDownloads(this)
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

                val uri: Uri = getImageUri(this@PreviewActivity, capturedImageBitmap)
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

    private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap) {

        val fileOutputStream: FileOutputStream?
        try {
            fileOutputStream = context.openFileOutput("temp.jpg", Context.MODE_PRIVATE)
            fileOutputStream.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun bitmapToDownloads(bitmap: Bitmap) {
        if (!YOGIDIARY_PATH.exists()) {
            if (!YOGIDIARY_PATH.mkdirs()) {
                return finish()
            }
        }

        val downloadsDirectoryPath = YOGIDIARY_PATH.path + "/"
        val filename = String.format(Locale.getDefault(), "%d%s", Calendar.getInstance().timeInMillis, ".jpg")

        val file = File(downloadsDirectoryPath, filename)

        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            it.flush()
        }

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

        showToast(R.string.notification_image_saved)
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
