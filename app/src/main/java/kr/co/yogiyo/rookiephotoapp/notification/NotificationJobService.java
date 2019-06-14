package kr.co.yogiyo.rookiephotoapp.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotificationJobService extends JobService {

    private static final String TAG = NotificationJobService.class.getSimpleName();
    private static final int dayMillisecond = 1000 * 60 * 60 * 24;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // TODO : 인자로 시간값 받으려면 -> params.getExtras().getLong("KEY");
    // TODO : WorkManager를 대신 사용하는 것으로 구현
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob()");

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + dayMillisecond);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        intent.setAction(NotificationConstant.ALARM_NOTIFICATION);
        intent.putExtra(NotificationConstant.NOTIFICATION_CATEGORY, NotificationConstant.CATEGORY_ALARM_ON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NotificationConstant.NOTIFICATION_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), dayMillisecond, pendingIntent);

        /* TODO : 버전별 시간 설정 구현 필요 or WorkManager 구현
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millisecond, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millisecond, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millisecond, pendingIntent);
        }*/

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
