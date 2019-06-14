package kr.co.yogiyo.rookiephotoapp.notification;

import android.arch.persistence.room.Update;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

public class NotificationJobIntentService extends JobIntentService {

    private static final String TAG = NotificationJobService.class.getSimpleName();
    private static final int JOB_ID = 1000;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, Update.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //TODO : if(intent action 같으면, 작업 처리)
    }
}