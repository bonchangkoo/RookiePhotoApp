package kr.co.yogiyo.rookiephotoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.diary.main.DiariesActivity

object ReminderNotification {

    private const val CHANNEL_ID = "channel_reminder"
    private const val NOTIFICATION_CODE = 888
    private const val PENDING_REQUEST_CODE_CAMERA = 301
    private const val PENDING_REQUEST_CODE_DIARY = 302
    private const val PENDING_REQUEST_CODE_DIARY_ADD = 303
    private val CHANNEL_NAME: CharSequence = "check_eating"

    fun generateNotification(context: Context) {
        createNotificationChannel(context)

        val reminderPendingIntent = TaskStackBuilder.create(context)
                .addParentStack(DiariesActivity::class.java)
                .addNextIntent(Intent(context, DiariesActivity::class.java))
                .getPendingIntent(
                        PENDING_REQUEST_CODE_DIARY,
                        PendingIntent.FLAG_UPDATE_CURRENT)

        val cameraPendingIntent = TaskStackBuilder.create(context)
                .addParentStack(CameraActivity::class.java)
                .addNextIntent(Intent(context, CameraActivity::class.java))
                .getPendingIntent(
                        PENDING_REQUEST_CODE_CAMERA,
                        PendingIntent.FLAG_UPDATE_CURRENT)

        val diaryPendingIntent = TaskStackBuilder.create(context)
                .addParentStack(DiaryEditActivity::class.java)
                .addNextIntent(Intent(context, DiaryEditActivity::class.java))
                .getPendingIntent(
                        PENDING_REQUEST_CODE_DIARY_ADD,
                        PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_add_white_24)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(context.getString(R.string.text_ask_food_title))
                .setContentText(context.getString(R.string.text_ask_food_message))
                .addAction(0, context.getString(R.string.text_start_camera), cameraPendingIntent)
                .addAction(0, context.getString(R.string.text_start_diary_add), diaryPendingIntent)
                .setContentIntent(reminderPendingIntent)
                .setAutoCancel(true)

        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_CODE, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}