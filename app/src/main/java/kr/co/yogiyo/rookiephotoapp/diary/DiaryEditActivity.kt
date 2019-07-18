package kr.co.yogiyo.rookiephotoapp.diary

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_diary_edit.*
import kr.co.yogiyo.rookiephotoapp.*
import kr.co.yogiyo.rookiephotoapp.Constants.*
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityDiaryEditBinding
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditViewModel.Companion.imageFileName
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditViewModel.Companion.isPhotoUpdate
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryActivity
import java.io.File
import java.util.*

class DiaryEditActivity : BaseActivity() {

    private lateinit var activityDiaryEditBinding: ActivityDiaryEditBinding
    private lateinit var diaryEditViewModel: DiaryEditViewModel

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog

    private val diaryIdx: Int by lazy {
        intent.getIntExtra(DIARY_IDX, -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialize()
        initView()
        diaryEditViewModel.initViewModel()

        setViewData(diaryIdx)
    }

    override fun onBackPressed() {
        createAlertDialog(this@DiaryEditActivity, getString(R.string.text_cancel_editing), getString(R.string.text_cancel_editing_question),
                getString(R.string.text_dialog_ok), getString(R.string.text_dialog_no),
                DialogInterface.OnClickListener { dialog, id ->
                    if (id == DialogInterface.BUTTON_POSITIVE)
                        finish()
                    else
                        dialog.dismiss()
                }, null).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        intent?.run {
            when (resultCode) {
                Activity.RESULT_OK -> if (requestCode == REQUEST_DIARY_PICK_GALLERY || requestCode == REQUEST_DIARY_CAPTURE_PHOTO) {
                    imageUri = this.data
                    Glide.with(this@DiaryEditActivity)
                            .load(imageUri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(e: GlideException?, model: Any,
                                                          target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                    showToast(R.string.toast_cannot_retrieve_selected_image)
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable, model: Any,
                                                             target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                                    isPhotoUpdate = true
                                    isBitmapOrUri = URI_TYPE
                                    return false
                                }
                            })
                            .into(ib_diary_edit_photo)
                }

                RESULT_CAPTURED_PHOTO -> if (requestCode == REQUEST_DIARY_CAPTURE_PHOTO) {
                    isBitmapOrUri = BITMAP_TYPE
                    imageBitmap = applicationContext.loadBitmapFromInternalStorage()
                    Glide.with(this@DiaryEditActivity)
                            .load(imageBitmap)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(ib_diary_edit_photo)
                    isPhotoUpdate = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalApplication.globalApplicationContext.isFromDiary = false
    }

    private fun initialize() {

        val dao = DiaryDatabase.getDatabase(this)!!.diaryDao()
        val repository = DiaryRepository.getInstance(dao)
        val viewModelFactory = DiaryEditViewModelFactory(repository)

        activityDiaryEditBinding = DataBindingUtil.setContentView(this, R.layout.activity_diary_edit)
        diaryEditViewModel = ViewModelProviders.of(this, viewModelFactory).get(DiaryEditViewModel::class.java)
        activityDiaryEditBinding.viewModel = diaryEditViewModel
    }

    private fun DiaryEditViewModel.initViewModel() {

        loadDiaryImageComplete = {
            Glide.with(this@DiaryEditActivity)
                    .load(FOONCARE_PATH.toString() + File.separator + it)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ib_diary_edit_photo)
        }

        diaryInsertComplete = {
            bitmapOrUriDownloads()
            showToast(R.string.text_diary_added)
            finish()
        }

        diaryInsertError = {
            showToast(getString(R.string.text_cant_add_diary))
        }

        diaryUpdateComplete = {
            if (isPhotoUpdate) {
                bitmapOrUriDownloads()
            }
            showToast(R.string.text_diary_updated)
            finish()
        }

        diaryUpdateError = {
            showToast(getString(R.string.text_cant_update_diary))
        }

        initDatePickerAndTimePickerListener = { year: Int, month: Int, day: Int, hour: Int, minute: Int ->
            datePickerDialog = DatePickerDialog(this@DiaryEditActivity, R.style.PickerTheme, datePickerDialogListener,
                    year, month - 1, day)
            timePickerDialog = TimePickerDialog(this@DiaryEditActivity, R.style.PickerTheme, timePickerDialogListener,
                    hour, minute, false)
        }

    }

    private fun initView() {
        setSupportActionBar(toolbar)

        ib_back.setOnClickListener {
            onBackPressed()
        }

        if (diaryIdx == DIARY_ADD) {
            tv_subject.setText(R.string.text_diary_add_title)
        }

        ib_diary_save.setOnClickListener {
            saveViewData()
            finish()
        }

        tv_diary_edit_date.setOnClickListener {
            datePickerDialog.show()
        }

        tv_diary_edit_time.setOnClickListener {
            timePickerDialog.show()
        }

        ib_diary_edit_photo.setOnClickListener {
            val selectStr = arrayOf(getString(R.string.text_capture_photo), getString(R.string.text_pick_gallery))

            val alertDialog = AlertDialog.Builder(this@DiaryEditActivity).apply {
                this.setTitle(getString(R.string.text_do_select_photo_action))
                this.setItems(selectStr) { _, which ->
                    GlobalApplication.globalApplicationContext.isFromDiary = true

                    when (which) {
                        CAPTURE_PHOTO -> {
                            val photoCaptureIntent = Intent(this@DiaryEditActivity, CameraActivity::class.java)
                            startActivityForResult(photoCaptureIntent, REQUEST_DIARY_CAPTURE_PHOTO)
                        }
                        PICK_GALLERY -> {
                            val doStartEditPhotoActivityIntent = Intent(this@DiaryEditActivity, GalleryActivity::class.java)
                            startActivityForResult(doStartEditPhotoActivityIntent, REQUEST_DIARY_PICK_GALLERY)
                        }
                    }
                }
            }
            val dialog = alertDialog.create()
            dialog.show()
        }
    }

    private val datePickerDialogListener = DatePickerDialog.OnDateSetListener { _, _, month, dayOfMonth ->
        diaryEditViewModel.setDateTextLabel(month + 1, dayOfMonth)
    }

    private val timePickerDialogListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        view.setIs24HourView(false)
        diaryEditViewModel.setTimeTextLabel(hourOfDay, minute)
    }

    private fun setViewData(idx: Int) {
        when (idx) {
            DIARY_ADD -> {
                val currentDateAndTime = Calendar.getInstance().time
                diaryEditViewModel.initDateAndTime(currentDateAndTime)

                intent?.run {
                    if (this.hasExtra("FROM_PREVIEW")) {
                        isBitmapOrUri = BITMAP_TYPE
                        imageBitmap = PreviewActivity.capturedImageBitmap
                        Glide.with(this@DiaryEditActivity)
                                .load(imageBitmap)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ib_diary_edit_photo)
                    }

                    this.data?.run {
                        isBitmapOrUri = URI_TYPE
                        imageUri = this
                        Glide.with(this@DiaryEditActivity)
                                .load(imageUri)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ib_diary_edit_photo)
                        GlobalApplication.globalApplicationContext.isFromDiary = true
                    }
                }
            }
            else -> diaryEditViewModel.loadDiaryViewData(idx)
        }
    }

    private fun saveViewData() {
        if (diaryIdx == DIARY_ADD) {
            diaryEditViewModel.insertDiaryViewData()
        } else {
            diaryEditViewModel.updateDiaryViewData(diaryIdx)
        }
    }

    private fun bitmapOrUriDownloads() {
        try {
            when (isBitmapOrUri) {
                BITMAP_TYPE -> imageBitmap?.run {
                    applicationContext.bitmapToDownloads(this, imageFileName)
                }
                URI_TYPE -> imageUri?.run {
                    applicationContext.copyFileToDownloads(this, imageFileName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val DIARY_ADD = -1
        private const val CAPTURE_PHOTO = 0
        private const val PICK_GALLERY = 1
        private const val BITMAP_TYPE = true
        private const val URI_TYPE = false

        private var imageUri: Uri? = null
        private var imageBitmap: Bitmap? = null
        private var isBitmapOrUri = URI_TYPE
    }
}
