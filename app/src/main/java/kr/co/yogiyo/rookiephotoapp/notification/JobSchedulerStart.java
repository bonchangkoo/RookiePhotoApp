package kr.co.yogiyo.rookiephotoapp.notification;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class JobSchedulerStart {

    private static final int JOB_ID = 1111;

    public static void start(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobInfo jobInfo;

            /* TODO : 파라미터로 시간값 전달하려면
             * PersistableBundle bundle = new PersistableBundle();
             * bundle.putLong("KEY", "VALUE);
             * builder.setExtras(bundle)
             * */
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, NotificationJobService.class))
                        .setRequiresStorageNotLow(true)
                        .build();
            } else {
                // TODO : lower than O(26) version 처리 완성 필요
                jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, NotificationJobIntentService.class))
                        .build();
            }

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobInfo);
        } else {
            // TODO : lower than LOLLIPOP(21) version 처리
        }
    }
}