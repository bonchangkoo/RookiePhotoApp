package kr.co.yogiyo.rookiephotoapp.data.source.remote

import io.reactivex.Flowable
import kr.co.yogiyo.rookiephotoapp.data.model.RestoredDiary
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field

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