package kr.co.yogiyo.rookiephotoapp.settings

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.util.Log

import com.google.firebase.auth.FirebaseUser
import com.google.gson.JsonParser

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

import id.zelory.compressor.Compressor
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryViewModel
import kr.co.yogiyo.rookiephotoapp.settings.sync.DiaryBackupRestore
import okhttp3.ResponseBody

class BackupRestoreDialogFragment : PreferenceDialogFragmentCompat() {

    private val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private val imageCompressor by lazy { Compressor(context) }

    private val compositeDisposable = CompositeDisposable()

    private val diaryBackupRestore = DiaryBackupRestore()

    private val localDiaryViewModel by lazy {
        ViewModelProviders.of(this).get(LocalDiaryViewModel::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        arguments?.let { arguments ->
            when (arguments.getString(SettingsActivity.PREFERENCE_KEY)) {
                SettingsActivity.BACKUP_DIALOG_KEY -> {
                    builder?.run {
                        setTitle(R.string.text_do_backup)
                        setMessage(R.string.text_ask_backup)
                    }
                }
                SettingsActivity.RESTORE_DIALOG_KEY -> {
                    builder?.run {
                        setTitle(R.string.text_do_restore)
                        setMessage(R.string.text_ask_restore)
                    }
                }
            }
            builder?.run {
                setPositiveButton(R.string.text_dialog_ok, null)
                setNegativeButton(R.string.text_dialog_no) { _, _ -> dismiss() }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog.setOnShowListener { dialog ->
            arguments?.let { arguments ->
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    setLoadingDialog(true)
                    when (arguments.getString(SettingsActivity.PREFERENCE_KEY)) {
                        SettingsActivity.BACKUP_DIALOG_KEY -> {
                            executeBackup(GlobalApplication.globalApplicationContext.firebaseAuth.currentUser)
                        }
                        SettingsActivity.RESTORE_DIALOG_KEY -> {
                            executeRestore(GlobalApplication.globalApplicationContext.firebaseAuth.currentUser)
                        }
                    }
                }
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        // Do nothing
    }

    // TODO : 백업/복원 실행할 때 다른 작업 못하도록 또는 바로 클릭 못하도록 또는 화면 분할 (작업 진행도 표시 필요할 듯)
    // TODO : 리사이징한 사진 복구 어떻게 할지 고민
    private fun executeBackup(currentUser: FirebaseUser?) {
        compositeDisposable.add(
                localDiaryViewModel.findDiaries().toFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap { diaries ->
                            if (diaries.isEmpty()) {
                                throw Exception(NO_DATA)
                            } else {
                                val flowableDiaries = Flowable.just(diaries)
                                Flowable.zip(
                                        flowableDiaries,
                                        diaryBackupRestore.executePostClearDiary(currentUser!!),
                                        BiFunction<List<Diary>,
                                                ResponseBody,
                                                Pair<Flowable<List<Diary>>, ResponseBody>> { _, responseBody ->
                                            Pair(flowableDiaries, responseBody)
                                        }
                                )
                            }
                        }
                        .flatMap {
                            if (JsonParser().parse(it.second.string()).asJsonObject
                                            .get(SERVER_RESPONSE_KEY).asString != SERVER_RESPONSE_CLEAR_SUCCESS) {
                                throw Exception(FAILED_TO_CONNECT)
                            } else {
                                it.first
                            }
                        }
                        .flatMapIterable { diaries -> diaries }
                        .flatMap { diary ->
                            if (diary.image != null) {
                                val imageFile = File(Constants.FOONCARE_PATH, diary.image)
                                if (imageFile.isFile) {
                                    imageCompressor.setDestinationDirectoryPath(
                                            Constants.FOONCARE_PATH.absolutePath + File.separator + DiaryBackupRestore.COMPRESSED_FOLDER_NAME)
                                            .compressToFile(imageFile)
                                }
                            }

                            diaryBackupRestore.executePostDiary(currentUser!!, diary)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ responseDiariesBody ->
                            Log.d(TAG, "body string " + responseDiariesBody.string())
                            if (responseDiariesBody.string().contains(ERROR_MESSAGE)) {
                                throw Exception()
                            }
                        }, { throwable ->
                            val errorMessageResource = throwable.message?.let { message ->
                                when {
                                    message == NO_DATA -> R.string.text_no_backup_data
                                    message.contains(FAILED_TO_CONNECT) -> R.string.text_network_error
                                    else -> R.string.text_backup_fail
                                }
                            } ?: R.string.text_backup_fail

                            (context as SettingsActivity).createAlertDialog(context!!, null, getString(errorMessageResource),
                                    getString(R.string.text_confirm), null, null, null).show()

                            setLoadingDialog(false)

                            Log.d(TAG, throwable.message)
                        }, {
                            (context as SettingsActivity).createAlertDialog(context!!, null, getString(R.string.text_backup_success),
                                    getString(R.string.text_confirm), null, null, null).show()

                            setLoadingDialog(false)
                        })
        )
    }

    private fun executeRestore(currentUser: FirebaseUser?) {
        compositeDisposable.add(
                diaryBackupRestore.executeGetDiaries(currentUser!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMapIterable { restoredDiaries ->
                            if (restoredDiaries.isEmpty()) {
                                throw Exception(NO_DATA)
                            }

                            restoredDiaries
                        }
                        .map { restoredDiary ->
                            val date = serverDateFormat.parse(restoredDiary.datetime)
                            val image: String? = restoredDiary.image?.substring(restoredDiary.image!!.lastIndexOf(File.separator) + 1)

                            localDiaryViewModel.insertDiary(restoredDiary.diaryId, date, image, restoredDiary.description)
                                    .subscribe()

                            restoredDiary
                        }
                        .filter {
                            it.image != null
                        }
                        .flatMap { restoredDiary ->
                            diaryBackupRestore.executeGetImage(restoredDiary.image!!.substring(restoredDiary.image!!.indexOf(File.separator) + 1))
                        }
                        .map { pair ->
                            val imageFileName = pair.first.substring(pair.first.lastIndexOf(File.separator) + 1)
                            val writtenToDisk = this@BackupRestoreDialogFragment.writeResponseBodyToDisk(imageFileName, pair.second)

                            writtenToDisk
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ writtenToDisk -> Log.d(TAG, "success = $writtenToDisk") },
                                { throwable ->
                                    val errorMessageResource = throwable.message?.let { message ->
                                        when {
                                            message == NO_DATA || message.contains(NO_BACKUP_HISTORY) -> R.string.text_no_restore_data
                                            message.contains(FAILED_TO_CONNECT) -> R.string.text_network_error
                                            else -> R.string.text_restore_fail
                                        }
                                    } ?: R.string.text_restore_fail

                                    (context as SettingsActivity).createAlertDialog(context!!, null, getString(errorMessageResource),
                                            getString(R.string.text_confirm), null, null, null).show()

                                    setLoadingDialog(false)

                                    Log.d(TAG, throwable.message)
                                }, {
                            (context as SettingsActivity).createAlertDialog(context!!, null, getString(R.string.text_restore_success),
                                    getString(R.string.text_confirm), null, null, null).show()

                            setLoadingDialog(false)
                        })
        )
    }

    private fun writeResponseBodyToDisk(imageFileName: String, body: ResponseBody): Boolean {
        if (!Constants.FOONCARE_PATH.exists()) {
            if (!Constants.FOONCARE_PATH.mkdirs()) {
                return false
            }
        }

        val restoredImageFile = File(Constants.FOONCARE_PATH, imageFileName)

        try {
            body.byteStream().use { inputStream ->
                FileOutputStream(restoredImageFile).use { outputStream ->
                    val fileReader = ByteArray(4096)

                    val fileSize = body.contentLength()
                    var fileSizeDownloaded: Long = 0

                    var read = inputStream.read(fileReader)
                    while (read != -1) {
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()
                        Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")

                        read = inputStream.read(fileReader)
                    }

                    outputStream.flush()

                    context!!.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(restoredImageFile)))

                    return true
                }
            }
        } catch (e: IOException) {
            return false
        }
    }

    private fun setLoadingDialog(visible: Boolean) {
        (context as SettingsActivity).showLoading(visible)
        if (visible) {
            dialog.hide()
        } else {
            dismiss()
        }
    }

    companion object {

        private val TAG = BackupRestoreDialogFragment::class.java.simpleName
        private const val NO_DATA = "no_data"
        private const val NO_BACKUP_HISTORY = "Expected BEGIN_ARRAY but was BEGIN_OBJECT"
        private const val FAILED_TO_CONNECT = "Failed to connect"
        private const val ERROR_MESSAGE = "error_message"
        private const val SERVER_RESPONSE_KEY = "message"
        private const val SERVER_RESPONSE_CLEAR_SUCCESS = "clear success"

        fun newInstance(): BackupRestoreDialogFragment {
            return BackupRestoreDialogFragment()
        }
    }
}