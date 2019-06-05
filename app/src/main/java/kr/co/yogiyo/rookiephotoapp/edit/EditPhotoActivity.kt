package kr.co.yogiyo.rookiephotoapp.edit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.Constants.*
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import java.io.File

class EditPhotoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)

        doSeparateIntent()
    }

    private fun doSeparateIntent() {
        intent?.let {
            val photoCategoryNumber = it.getIntExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO)

            when (photoCategoryNumber) {
                EDIT_SELECTED_PHOTO -> {
                    pickFromGallery()
                }
                EDIT_CAPTURED_PHOTO -> {
                    val capturedPhotoUri = it.getParcelableExtra<Uri>(getString(R.string.capture_photo_uri))
                    capturedPhotoUri?.let {
                        startCrop(capturedPhotoUri)
                    } ?: showToast(R.string.dont_load_captured_photo)
                }
            }
        } ?: finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_CANCELED -> finish()
        }

        data?.let {
            when (resultCode) {
                Activity.RESULT_OK -> when (requestCode) {
                    REQUEST_PICK_GALLERY -> it.data?.let { uri ->
                        startCrop(uri)
                    } ?: showToast(R.string.toast_cannot_retrieve_selected_image)

                    UCrop.REQUEST_CROP ->
                        handleCropResult(it)
                }
                RESULT_EDIT_PHOTO -> when (requestCode) {
                    REQUEST_DIARY_PICK_GALLERY -> it.data?.let { selectedUri ->
                        val intent = Intent(this@EditPhotoActivity, DiaryEditActivity::class.java).apply {
                            this.data = selectedUri
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } ?: finish()
                    REQUEST_DIARY_CAPTURE_PHOTO -> it.data?.let { selectedUri ->
                        val intent = Intent(this@EditPhotoActivity, PreviewActivity::class.java).apply {
                            this.data = selectedUri
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } ?: finish()
                }
                UCrop.RESULT_ERROR -> handleCropError(data)
            }
        }
    }

    private fun handleCropResult(result: Intent) {

        UCrop.getOutput(result)?.let { resultUri ->

            globalApp.fromDiary?.run {
                if (this) {
                    val intent = Intent(this@EditPhotoActivity, EditResultActivity::class.java).apply {
                        data = resultUri
                    }
                    startActivityForResult(intent, REQUEST_DIARY_CAPTURE_PHOTO)
                } else {
                    EditResultActivity.startWithUri(this@EditPhotoActivity, resultUri)
                    finish()
                }
            }
        } ?: showToast(R.string.toast_cannot_retrieve_cropped_image)
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)

        cropError?.let {
            Log.e(TAG, "handleCropError: ", cropError)
            showToast(cropError.message)
        } ?: showToast(R.string.toast_unexpected_error)
    }

    private fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_READ_AND_WRITE_ACCESS_PERMISSION)
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val mimeTypes = arrayOf("image/jpeg", "image/png")
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_PICK_GALLERY)
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationFileName = "$SAMPLE_CROPPED_IMAGE_NAME.jpg"

        val uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationFileName))).apply {
            // Crop Gestures는 SCALE만 가능하게 옵션 설정
            val options = UCrop.Options().apply {
                setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.SCALE)
            }
            withOptions(options)
        }
        uCrop.start(this@EditPhotoActivity)
    }

    companion object {
        private val TAG = EditPhotoActivity::class.java.simpleName

        const val EDIT_SELECTED_PHOTO = 0
        const val EDIT_CAPTURED_PHOTO = 1

        private const val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage"
    }
}

