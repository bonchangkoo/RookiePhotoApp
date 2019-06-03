package kr.co.yogiyo.rookiephotoapp.settings.sync;

import java.util.List;

import io.reactivex.Flowable;
import kr.co.yogiyo.rookiephotoapp.settings.RestoredDiary;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DiaryBackupRestoreService {
    @GET("restful/diary/")
    Flowable<List<RestoredDiary>> getDiaries(@Query("uid") String uid);

    @GET("{image}")
    Flowable<ResponseBody> getImage(@Path("image") String imagePath);

    @POST("restful/diary/")
    Flowable<ResponseBody> postDiary(@Body RequestBody body);

    @FormUrlEncoded
    @POST("restful/diary/clear/")
    Flowable<ResponseBody> postClearDiary(@Field("uid") String uid);
}