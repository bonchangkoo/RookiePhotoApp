package kr.co.yogiyo.rookiephotoapp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = MyBroadcastReceiver.class.getSimpleName();

    public static void generateNotification(Context context) {
        createNotificationChannel(context);

        Intent cameraIntent = new Intent(context, CameraActivity.class);
        TaskStackBuilder cameraStackBuilder = TaskStackBuilder.create(context)
                .addParentStack(CameraActivity.class)
                .addNextIntent(cameraIntent);
        PendingIntent cameraPendingIntent = cameraStackBuilder.getPendingIntent(
                NotificationConstant.PENDING_REQUEST_CODE_CAMERA, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent diaryIntent = new Intent(context, DiaryEditActivity.class);
        TaskStackBuilder diaryStackBuilder = TaskStackBuilder.create(context)
                .addParentStack(DiaryEditActivity.class)
                .addNextIntent(diaryIntent);
        PendingIntent diaryPendingIntent = diaryStackBuilder.getPendingIntent(
                NotificationConstant.PENDING_REQUEST_CODE_DIARY, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(NotificationConstant.CLOSE_NOTIFICATION);
        intent.putExtra(NotificationConstant.NOTIFICATION_ID, NotificationConstant.CLOSE_NOTIFICATION_ID);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(
                context, NotificationConstant.NOTIFICATION_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_check_eating);
        notificationLayout.setOnClickPendingIntent(R.id.btn_start_camera, cameraPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.btn_start_diary_add, diaryPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.btn_close_notification, closePendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationConstant.CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_add_white_24)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(cameraPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NotificationConstant.NOTIFICATION_CODE, builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = context.getString(R.string.text_explain_channel);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NotificationConstant.CHANNEL_ID, NotificationConstant.CHANNEL_NAME, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        } else {
            // TODO: O(26) 하위 버전 구현
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive()");

        String action = intent.getAction();
        Log.d(TAG, action);
        switch (action) {
            case NotificationConstant.CLOSE_NOTIFICATION:
                if (intent.getIntExtra(NotificationConstant.NOTIFICATION_ID, -1) == NotificationConstant.CLOSE_NOTIFICATION_ID) {
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                    notificationManagerCompat.cancel(NotificationConstant.NOTIFICATION_CODE);
                }
                break;
            case NotificationConstant.ALARM_NOTIFICATION:
                if (intent.getStringExtra(NotificationConstant.NOTIFICATION_CATEGORY).equals(NotificationConstant.CATEGORY_ALARM_ON)) {
                    generateNotification(context);
                }
                break;
        }
    }
}