package kr.co.yogiyo.rookiephotoapp.edit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_edit_result.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

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
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_note_add_white_36) // 왼쪽에 아이콘 배치(홈 아이콘 대체)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_result, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_download -> saveCroppedImage() // 이미지 저장
            android.R.id.home -> onBackPressed() // 임시로 back 기능으로 대체
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

        val yogiDiaryStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YogiDiary")

        if (!yogiDiaryStorageDir.exists()) {
            if (!yogiDiaryStorageDir.mkdirs()) {
                return finish()
            }
        }

        val downloadsDirectoryPath = yogiDiaryStorageDir.path + "/"
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

