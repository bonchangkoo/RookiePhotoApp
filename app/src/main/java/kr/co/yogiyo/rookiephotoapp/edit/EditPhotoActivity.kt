package kr.co.yogiyo.rookiephotoapp.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.Constants.*
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryActivity
import java.io.File

class EditPhotoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)

        doSeparateIntent()
    }

    private fun doSeparateIntent() {
        intent?.let {
            photoCategoryNumber = it.getIntExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO)

            when (photoCategoryNumber) {
                EDIT_SELECTED_PHOTO -> {
                    it.data?.let { uri ->
                        startCrop(uri)
                    }
                }
                EDIT_CAPTURED_PHOTO -> {
                    val capturedPhotoUri = it.getParcelableExtra<Uri>(getString(R.string.capture_photo_uri))
                    capturedPhotoUri?.let {
                        startCrop(capturedPhotoUri)
                    } ?: showToast(R.string.dont_load_captured_photo)
                }
                else -> showToast(R.string.toast_unexpected_error)
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
                    UCrop.REQUEST_CROP ->
                        handleCropResult(it)
                }
                RESULT_EDIT_PHOTO -> when (requestCode) {
                    REQUEST_DIARY_PICK_GALLERY -> it.data?.let { selectedUri ->
                        val intent = Intent(this@EditPhotoActivity, GalleryActivity::class.java).apply {
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

            if (GlobalApplication.globalApplicationContext.isFromDiary) {
                val intent = Intent(this@EditPhotoActivity, EditResultActivity::class.java).apply {
                    data = resultUri

                }

                startActivityForResult(intent, if (photoCategoryNumber == EDIT_CAPTURED_PHOTO) REQUEST_DIARY_CAPTURE_PHOTO else REQUEST_DIARY_PICK_GALLERY)

            } else {
                EditResultActivity.startWithUri(this@EditPhotoActivity, resultUri)
                finish()
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

        private var photoCategoryNumber : Int = EDIT_SELECTED_PHOTO

        private const val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage"
    }
}

