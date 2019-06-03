package kr.co.yogiyo.rookiephotoapp.settings.sync;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import kr.co.yogiyo.rookiephotoapp.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    // TODO : IP 주소 github에 올리는 코드에 작성해도 괜찮은지
    public static final String BASE_URL = "http://45.32.46.136:8001";

    private static Retrofit retrofit;

    static {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            httpClientBuilder.addNetworkInterceptor(httpLoggingInterceptor);
        }

        OkHttpClient httpClient = httpClientBuilder.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        retrofit = builder.build();
    }

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}