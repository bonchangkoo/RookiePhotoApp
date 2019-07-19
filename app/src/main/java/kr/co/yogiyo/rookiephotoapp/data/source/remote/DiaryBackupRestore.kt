package kr.co.yogiyo.rookiephotoapp.data.source.remote

import android.util.Pair
import com.google.firebase.auth.FirebaseUser
import io.reactivex.Flowable
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.data.model.Diary
import kr.co.yogiyo.rookiephotoapp.data.model.RestoredDiary
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class DiaryBackupRestore {

    private val diaryBackupRestoreService = ServiceGenerator.createService(DiaryBackupRestoreService::class.java)
    private val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun executeGetDiaries(currentUser: FirebaseUser): Flowable<List<RestoredDiary>> {
        return diaryBackupRestoreService.getDiaries(currentUser.uid)
    }

    fun executeGetImage(imagePath: String): Flowable<Pair<String, ResponseBody>> {
        return diaryBackupRestoreService.getImage(imagePath)
                .map { responseBody -> Pair(imagePath, responseBody) }
    }

    fun executePostDiary(currentUser: FirebaseUser, diary: Diary): Flowable<ResponseBody> {
        val builder = MultipartBody.Builder().apply {
            setType(MEDIA_TYPE_MULTIPART!!)
            addFormDataPart("diary_id", diary.idx.toString())
            addFormDataPart("date", serverDateFormat.format(diary.date))
            diary.image?.let { diaryImage ->
                File(Constants.FOONCARE_PATH, COMPRESSED_FOLDER_NAME + File.separator + diaryImage).let { imageFile ->
                    if (imageFile.isFile) {
                        addFormDataPart("image", imageFile.name, RequestBody.create(MEDIA_TYPE_IMAGE, imageFile))
                    }
                }
            }
            diary.description?.let {
                addFormDataPart("description", it)
            }
            addFormDataPart("uid", currentUser.uid)
        }

        return diaryBackupRestoreService.postDiary(builder.build())
    }

    fun executePostClearDiary(currentUser: FirebaseUser): Flowable<ResponseBody> {
        return diaryBackupRestoreService.postClearDiary(currentUser.uid)
    }

    companion object {
        private val MEDIA_TYPE_MULTIPART = MediaType.parse("multipart/form-data")
        private val MEDIA_TYPE_IMAGE = MediaType.parse("image/*")
        const val COMPRESSED_FOLDER_NAME = "compressed"
    }
}