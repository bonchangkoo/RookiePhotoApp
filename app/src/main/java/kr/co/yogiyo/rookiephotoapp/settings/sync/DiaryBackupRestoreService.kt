package kr.co.yogiyo.rookiephotoapp.settings.sync

import io.reactivex.Flowable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface DiaryBackupRestoreService {
    @GET("restful/diary/")
    fun getDiaries(@Query("uid") uid: String): Flowable<List<RestoredDiary>>

    @GET("{image}")
    fun getImage(@Path("image") imagePath: String): Flowable<ResponseBody>

    @POST("restful/diary/")
    fun postDiary(@Body body: RequestBody): Flowable<ResponseBody>

    @FormUrlEncoded
    @POST("restful/diary/clear/")
    fun postClearDiary(@Field("uid") uid: String): Flowable<ResponseBody>
}