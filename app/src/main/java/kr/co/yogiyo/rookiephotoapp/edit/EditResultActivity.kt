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
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_edit_result.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class EditResultActivity : BaseActivity() {

    private var getEditPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_result)

        setView()
        setSettingAndResultActionBar()
    }

    // UCropView setting
    private fun setView() {
        getEditPhotoUri = intent.data
        getEditPhotoUri?.let {
            iv_edited_photo.setImageURI(it)
        } ?: showToast(R.string.dont_load_captured_photo)
    }

    private fun setSettingAndResultActionBar() {
        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        val actionBar = supportActionBar
        actionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_note_add_white_36) // 왼쪽에 아이콘 배치(홈 아이콘 대체)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_result, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == R.id.menu_download -> saveCroppedImage() // 이미지 저장
            item.itemId == android.R.id.home -> onBackPressed() // 임시로 back 기능으로 대체
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveCroppedImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission(this@EditResultActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@EditResultActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
        } else {
            getEditPhotoUri?.let {
                if ("file" == it.scheme) {
                    try {
                        copyFileToDownloads(it)
                    } catch (e: Exception) {
                        showToast(e.message)
                        Log.e(TAG, it.toString(), e)
                    }
                }
            } ?: showToast(R.string.toast_unexpected_error)
        }
    }

    @Throws(Exception::class)
    private fun copyFileToDownloads(croppedFileUri: Uri) {

        val yogiDiaryStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YogiDiary")

        if (!yogiDiaryStorageDir.exists()) {
            if (yogiDiaryStorageDir.mkdirs()) {
                Log.d(TAG, getString(R.string.text_mkdir_success))
            } else {
                Log.d(TAG, getString(R.string.text_mkdir_fail))
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
