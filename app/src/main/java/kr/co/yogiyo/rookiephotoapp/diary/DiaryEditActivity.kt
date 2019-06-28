package kr.co.yogiyo.rookiephotoapp.diary

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_diary_edit.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.camera.capture.PreviewActivity
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryViewModel
import kr.co.yogiyo.rookiephotoapp.gallery.GalleryActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DiaryEditActivity : BaseActivity(), View.OnClickListener {

    private var localDiaryViewModel: LocalDiaryViewModel? = null

    private var backImageButton: ImageButton? = null
    private var toolbarNameTextView: TextView? = null
    private var diarySaveImageButton: ImageButton? = null

    private var editDateTextView: TextView? = null
    private var editTimeTextView: TextView? = null
    private var editPhotoImageButton: ImageButton? = null
    private var editDescriptionTextView: TextView? = null

    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null

    private var selectedUri: Uri? = null
    private var updateHour: Int = 0
    private var updateMinute: Int = 0

    private var photoFileName: String? = null
    private var isPhotoUpdate = false
    private var isBitmap = false


    private var dateAndTime: Date
        get() {
            val datePicker = datePickerDialog!!.datePicker
            val year = datePicker.year
            val month = datePicker.month
            val day = datePicker.dayOfMonth

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, updateHour, updateMinute)

            return calendar.time
        }
        set(dateAndTime) {

            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val monthFormat = SimpleDateFormat("M", Locale.getDefault())
            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

            val year = yearFormat.format(dateAndTime)
            val month = monthFormat.format(dateAndTime)
            val day = dayFormat.format(dateAndTime)

            editDateTextView!!.text = String.format("%s월 %s일", month, day)
            datePickerDialog = DatePickerDialog(this@DiaryEditActivity, R.style.PickerTheme, dateListener, Integer.valueOf(year), Integer.valueOf(month) - 1, Integer.valueOf(day))

            val hourFormat = SimpleDateFormat("hh", Locale.getDefault())
            val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
            val meridiemFormat = SimpleDateFormat("aa", Locale.ENGLISH)

            val hour = hourFormat.format(dateAndTime)
            val minute = minuteFormat.format(dateAndTime)
            val meridiem = meridiemFormat.format(dateAndTime)

            editTimeTextView!!.text = String.format("%s:%s%s", hour, minute, meridiem)

            var applyMerdiemHour = Integer.valueOf(hour)

            if (meridiem == "PM" && applyMerdiemHour < 12) {
                applyMerdiemHour = applyMerdiemHour + 12
            }

            timePickerDialog = TimePickerDialog(this@DiaryEditActivity, R.style.PickerTheme, timeListener, applyMerdiemHour, Integer.valueOf(minute), false)

            updateHour = applyMerdiemHour
            updateMinute = Integer.valueOf(minute)
        }

    private val dateListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth -> editDateTextView!!.text = String.format(Locale.getDefault(), "%d월 %d일", month + 1, dayOfMonth) }

    private val timeListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        var hourOfDay = hourOfDay
        view.setIs24HourView(false)
        updateHour = hourOfDay
        updateMinute = minute

        val meridiem: String
        val minuteStr: String

        if (hourOfDay > 12) {
            hourOfDay = hourOfDay - 12
            meridiem = "PM"
        } else if (hourOfDay == 12) {
            meridiem = "PM"
        } else {
            meridiem = "AM"
        }

        if (minute < 10) {
            minuteStr = "0$minute"
        } else {
            minuteStr = "" + minute
        }

        editTimeTextView!!.text = String.format(Locale.getDefault(), "%d:%s%s", hourOfDay, minuteStr, meridiem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_edit)

        localDiaryViewModel = ViewModelProviders.of(this).get(LocalDiaryViewModel::class.java)

        diaryIdx = intent.getIntExtra(Constants.DIARY_IDX, -1)

        initView()
        setViewData(diaryIdx)
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        backImageButton = findViewById(R.id.ib_back)
        toolbarNameTextView = findViewById(R.id.tv_subject)
        if (diaryIdx == DIARY_ADD) {
            toolbarNameTextView!!.setText(R.string.text_diary_add_title)
        }
        diarySaveImageButton = findViewById(R.id.ib_diary_save)

        backImageButton!!.setOnClickListener(this)
        diarySaveImageButton!!.setOnClickListener(this)

        editDateTextView = findViewById(R.id.tv_diary_edit_date)
        editTimeTextView = findViewById(R.id.tv_diary_edit_time)
        editPhotoImageButton = findViewById(R.id.ib_diary_edit_photo)
        editDescriptionTextView = findViewById(R.id.et_diary_edit_description)

        editDateTextView!!.setOnClickListener(this)
        editTimeTextView!!.setOnClickListener(this)
        editPhotoImageButton!!.setOnClickListener(this)
        editDescriptionTextView!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ib_back -> onBackPressed()
            R.id.ib_diary_save -> {
                saveViewData()
                finish()
            }
            R.id.tv_diary_edit_date -> datePickerDialog!!.show()
            R.id.tv_diary_edit_time -> timePickerDialog!!.show()
            R.id.ib_diary_edit_photo -> {
                val alertDialog = AlertDialog.Builder(this@DiaryEditActivity)
                alertDialog.setTitle("선택하시오")
                val selectStr = arrayOf("사진 촬영", "사진 선택")
                alertDialog.setItems(selectStr) { dialog, which ->
                    GlobalApplication.globalApplicationContext.isFromDiary = true

                    if (which == 0) {
                        val photoCaptureIntent = Intent(this@DiaryEditActivity, CameraActivity::class.java)
                        startActivityForResult(photoCaptureIntent, Constants.REQUEST_DIARY_CAPTURE_PHOTO)
                    } else if (which == 1) {
                        val doStartEditPhotoActivityIntent = Intent(this@DiaryEditActivity, GalleryActivity::class.java)
                        if (GlobalApplication.globalApplicationContext.isFromDiary) {
                            startActivityForResult(doStartEditPhotoActivityIntent, Constants.REQUEST_DIARY_CAPTURE_PHOTO)
                        } else {
                            startActivity(doStartEditPhotoActivityIntent)
                        }
                    }
                }
                val dialog = alertDialog.create()
                dialog.show()
            }
        }
    }

    override fun onBackPressed() {
        createAlertDialog(this@DiaryEditActivity, "작성 취소", "정말로 취소하시겠습니까?",
                getString(R.string.text_dialog_ok), getString(R.string.text_dialog_no),
                DialogInterface.OnClickListener { dialog, id ->
                    if (id == DialogInterface.BUTTON_POSITIVE)
                        finish()
                    else
                        dialog.dismiss()
                }, null).show()
    }

    private fun setViewData(idx: Int) {
        if (idx == DIARY_ADD) {
            val currentTime = Calendar.getInstance().time
            dateAndTime = currentTime

            if (intent.hasExtra("FROM_PREVIEW")) {
                isBitmap = true
                selectedBitmap = PreviewActivity.capturedImageBitmap
                Glide.with(this)
                        .load(selectedBitmap)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(editPhotoImageButton!!)

            } else if (intent.data != null) {
                val uri = intent.data
                selectedUri = uri
                Glide.with(this)
                        .load(selectedUri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(editPhotoImageButton!!)
                GlobalApplication.globalApplicationContext.isFromDiary = true
            }
        } else {
            compositeDisposable.add(localDiaryViewModel!!.findDiaryById(idx)!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { diary ->
                        dateAndTime = diary.date
                        photoFileName = diary.image
                        Glide.with(this@DiaryEditActivity)
                                .load(Constants.YOGIDIARY_PATH.toString() + File.separator + photoFileName)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(editPhotoImageButton!!)
                        editDescriptionTextView!!.text = diary.description
                    })
        }
    }

    fun loadBitmapFromInternalStorage(context: Context): Bitmap? {
        val fileInputStream: FileInputStream
        var bitmap: Bitmap? = null
        try {
            fileInputStream = context.openFileInput("temp.jpg")
            bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> if ((requestCode == Constants.REQUEST_DIARY_PICK_GALLERY || requestCode == Constants.REQUEST_DIARY_CAPTURE_PHOTO) && data != null) {
                selectedUri = data.data
                Glide.with(this@DiaryEditActivity)
                        .load(selectedUri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                showToast(R.string.toast_cannot_retrieve_selected_image)
                                return false
                            }

                            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                                isPhotoUpdate = true
                                isBitmap = false
                                return false
                            }
                        })
                        .into(editPhotoImageButton!!)
            }
            Constants.RESULT_CAPTURED_PHOTO -> if (requestCode == Constants.REQUEST_DIARY_CAPTURE_PHOTO && data != null) {
                isBitmap = true
                selectedBitmap = loadBitmapFromInternalStorage(applicationContext)
                Glide.with(this@DiaryEditActivity)
                        .load(selectedBitmap)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(editPhotoImageButton!!)
                isPhotoUpdate = true
            }
        }
    }

    private fun saveViewData() {
        if (diaryIdx == DIARY_ADD) {
            val updateDescription = editDescriptionTextView!!.text.toString()

            val time = dateAndTime

            localDiaryViewModel!!.insertDiary(time, time.time.toString() + ".jpg", updateDescription)
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {
                            // Do nothing
                        }

                        override fun onComplete() {
                            showToast(R.string.text_diary_added)
                            finish()
                        }

                        override fun onError(e: Throwable) {
                            showToast(getString(R.string.text_cant_add_diary))
                        }
                    })

            try {
                if (isBitmap) {
                    bitmapToDownloads(selectedBitmap, time.time)
                } else {
                    copyFileToDownloads(selectedUri, time.time)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            updateDiary(diaryIdx)
        }
    }

    @Throws(Exception::class)
    private fun copyFileToDownloads(croppedFileUri: Uri?, time: Long) {

        if (!Constants.YOGIDIARY_PATH.exists()) {
            if (Constants.YOGIDIARY_PATH.mkdirs()) {
                Log.d(TAG, getString(R.string.text_mkdir_success))
            } else {
                Log.d(TAG, getString(R.string.text_mkdir_fail))
            }
        }

        val downloadsDirectoryPath = Constants.YOGIDIARY_PATH.path + "/"
        val filename = String.format(Locale.getDefault(), "%d%s", time, ".jpg")

        val saveFile = File(downloadsDirectoryPath, filename)

        val inStream = FileInputStream(File(croppedFileUri!!.path))
        val outStream = FileOutputStream(saveFile)
        val inChannel = inStream.channel
        val outChannel = outStream.channel
        inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)))
        showToast(R.string.notification_image_saved)
    }

    @Throws(Exception::class)
    private fun bitmapToDownloads(bitmap: Bitmap?, time: Long) {

        if (!Constants.YOGIDIARY_PATH.exists()) {
            if (Constants.YOGIDIARY_PATH.mkdirs()) {
                Log.d(TAG, getString(R.string.text_mkdir_success))
            } else {
                Log.d(TAG, getString(R.string.text_mkdir_fail))
            }
        }

        val downloadsDirectoryPath = Constants.YOGIDIARY_PATH.path + "/"
        val filename = String.format(Locale.getDefault(), "%d%s", time, ".jpg")

        val saveFile = File(downloadsDirectoryPath, filename)

        val outStream = FileOutputStream(saveFile)
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)))
        showToast(R.string.notification_image_saved)
    }


    private fun updateDiary(idx: Int) {
        compositeDisposable.add(localDiaryViewModel!!.findDiaryById(idx)!!
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Consumer<Diary> {
                    override fun accept(t: Diary) {
                        if (t != null) {
                            val time = dateAndTime
                            var image = photoFileName
                            val updatedDescription = editDescriptionTextView!!.text.toString()

                            if (isPhotoUpdate) {
                                image = time.time.toString() + ".jpg"
                                try {
                                    if (isBitmap) {

                                        bitmapToDownloads(selectedBitmap, time.time)
                                    } else {
                                        copyFileToDownloads(selectedUri, time.time)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }

                            localDiaryViewModel!!
                                    .updateDiary(t, time, image, updatedDescription)
                                    .subscribeOn(Schedulers.single())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : CompletableObserver {
                                        override fun onSubscribe(d: Disposable) {
                                            //do noting
                                        }

                                        override fun onComplete() {
                                            showToast(R.string.text_diary_updated)
                                            finish()
                                        }

                                        override fun onError(e: Throwable) {
                                            showToast(getString(R.string.text_cant_update_diary))
                                        }
                                    })
                        }
                    }


                }))
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalApplication.globalApplicationContext.isFromDiary = false
    }

    companion object {

        private val TAG = DiaryEditActivity::class.java.simpleName
        private val BITMAP_FROM_PREVIEW = "BITMAP_FROM_PREVIEW"

        private val DIARY_ADD = -1

        private var diaryIdx: Int = 0
        private var selectedBitmap: Bitmap? = null
    }
}
