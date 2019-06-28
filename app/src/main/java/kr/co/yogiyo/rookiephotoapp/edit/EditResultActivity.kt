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
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_edit_result.*
import kr.co.yogiyo.rookiephotoapp.*
import kr.co.yogiyo.rookiephotoapp.Constants.DIARY_IDX
import kr.co.yogiyo.rookiephotoapp.Constants.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity

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

        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        if (GlobalApplication.globalApplicationContext.isFromDiary) {
            ib_diary_add.visibility = View.INVISIBLE
            ib_download.run {
                setImageResource(R.mipmap.diary_save)
                setOnClickListener {
                    Intent(this@EditResultActivity, EditPhotoActivity::class.java).apply {
                        data = editPhotoUri
                        setResult(Constants.RESULT_EDIT_PHOTO, this)
                        finish()
                    }
                }
            }
        } else {
            ib_diary_add.setOnClickListener {
                startActivity(Intent(this@EditResultActivity, DiariesActivity::class.java))
                val startDiaryEditActivityIntent = Intent(this@EditResultActivity, DiaryEditActivity::class.java).apply {
                    putExtra(DIARY_IDX, -1)
                    data = editPhotoUri
                }
                startActivity(startDiaryEditActivityIntent)
                finish()
            }
            ib_download.setOnClickListener {
                saveCroppedImage()
            }
        }

        editPhotoUri?.let {
            Glide.with(this)
                    .load(it)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(iv_edited_photo)
        }


    }

    private fun saveCroppedImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission
                (this@EditResultActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@EditResultActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
        } else {
            editPhotoUri?.let {
                if ("file" == it.scheme) {
                    try {
                        if (applicationContext.copyFileToDownloads(it)) {
                            showToast(R.string.notification_image_saved)
                        }
                        finish()
                    } catch (e: Exception) {
                        showToast(e.message)
                        Log.e(TAG, it.toString(), e)
                    }
                }
            }
        }
    }

    companion object {

        private val TAG = EditResultActivity::class.java.simpleName

        fun startWithUri(context: Context, uri: Uri) {
            val intent = Intent(context, EditResultActivity::class.java).apply {
                this.data = uri
            }
            context.startActivity(intent)
        }
    }
}

