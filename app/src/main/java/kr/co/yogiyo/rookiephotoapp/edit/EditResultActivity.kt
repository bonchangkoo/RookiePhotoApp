package kr.co.yogiyo.rookiephotoapp.edit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_edit_result.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.Constants.YOGIDIARY_PATH
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class EditResultActivity : BaseActivity() {

    private var editPhotoUri: Uri? = null
        get() {
            field = intent.data
            if (field == null) {
                showToast(R.string.dont_load_captured_photo)
            }
            return field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_result)

        setView()
        setSettingAndResultActionBar()
    }

    // UCropView setting
    private fun setView() {
        editPhotoUri?.let {
            iv_edited_photo.setImageURI(it)
        }
    }

    private fun setSettingAndResultActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            if (!GlobalApplication.globalApplicationContext.isFromDiary) {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.mipmap.diary_add) // 왼쪽에 아이콘 배치(홈 아이콘 대체)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_result, menu)
        val downloadItem = menu.findItem(R.id.menu_download)

        if (GlobalApplication.globalApplicationContext.isFromDiary) {
            downloadItem.setIcon(R.mipmap.diary_save)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_download -> {
                if (GlobalApplication.globalApplicationContext.isFromDiary) {
                    Intent(this@EditResultActivity, EditPhotoActivity::class.java).apply {
                        data = editPhotoUri
                        setResult(Constants.RESULT_EDIT_PHOTO, this)
                        finish()
                    }
                } else {
                    saveCroppedImage()
                }
            } // 이미지 저장
            android.R.id.home -> {
                startActivity(Intent(this@EditResultActivity, DiariesActivity::class.java))

                val startDiaryEditActivityIntent = Intent(this@EditResultActivity, DiaryEditActivity::class.java).apply {
                    putExtra("DIARY_IDX", -1)
                    data = editPhotoUri
                }
                startActivity(startDiaryEditActivityIntent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveCroppedImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission(this@EditResultActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@EditResultActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
        } else {
            editPhotoUri?.let {
                if ("file" == it.scheme) {
                    try {
                        copyFileToDownloads(it)
                    } catch (e: Exception) {
                        showToast(e.message)
                        Log.e(TAG, it.toString(), e)
                    }
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun copyFileToDownloads(croppedFileUri: Uri) {

        if (!YOGIDIARY_PATH.exists()) {
            if (!YOGIDIARY_PATH.mkdirs()) {
                return finish()
            }
        }

        val downloadsDirectoryPath = YOGIDIARY_PATH.path + "/"
        val filename = String.format(Locale.getDefault(), "%d_%s", Calendar.getInstance().timeInMillis, croppedFileUri.lastPathSegment)

        val saveFile = File(downloadsDirectoryPath, filename)

        val inStream = FileInputStream(File(croppedFileUri.path))
        val outStream = FileOutputStream(saveFile)
        val inChannel = inStream.channel
        val outChannel = outStream.channel
        inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)))

        showToast(R.string.notification_image_saved)
        finish()
    }

    companion object {

        private val TAG = EditResultActivity::class.java.simpleName

        private const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102

        fun startWithUri(context: Context, uri: Uri) {
            val intent = Intent(context, EditResultActivity::class.java).apply {
                this.data = uri
            }
            context.startActivity(intent)
        }
    }
}

