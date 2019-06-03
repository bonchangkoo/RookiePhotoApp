package kr.co.yogiyo.rookiephotoapp.settings.sync;

import android.util.Pair;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.settings.RestoredDiary;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class DiaryBackupRestore {

    private static final MediaType MEDIA_TYPE_MULTIPART = MediaType.parse("multipart/form-data");
    private static final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/*");

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private DiaryBackupRestoreService diaryBackupRestoreService = ServiceGenerator.createService(DiaryBackupRestoreService.class);

    public Flowable<List<RestoredDiary>> executeGetDiaries(FirebaseUser currentUser) {
        return diaryBackupRestoreService.getDiaries(currentUser.getUid());
    }

    public Flowable<Pair<String, ResponseBody>> executeGetImage(final String imagePath) {
        return diaryBackupRestoreService.getImage(imagePath)
                .map(responseBody -> new Pair<>(imagePath, responseBody));
    }

    public Flowable<ResponseBody> executePostDiary(FirebaseUser currentUser, Diary diary) {
        File imageFile = new File(BaseActivity.YOGIDIARY_PATH, "compressed" + File.separator + diary.getImage());

        RequestBody imageFileBody = RequestBody.create(MEDIA_TYPE_IMAGE, imageFile);

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MEDIA_TYPE_MULTIPART);
        builder.addFormDataPart("uid", currentUser.getUid());
        builder.addFormDataPart("date", simpleDateFormat.format(diary.getDate()));
        builder.addFormDataPart("description", diary.getDescription());
        builder.addFormDataPart("diary_id", String.valueOf(diary.getIdx()));
        builder.addFormDataPart("image", imageFile.getName(), imageFileBody);

        return diaryBackupRestoreService.postDiary(builder.build());
    }

    public Flowable<ResponseBody> executePostClearDiary(FirebaseUser currentUser) {
        return diaryBackupRestoreService.postClearDiary(currentUser.getUid());
    }
}