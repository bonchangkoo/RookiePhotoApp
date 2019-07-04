package kr.co.yogiyo.rookiephotoapp.notification

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWork(val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        ReminderNotification.generateNotification(context)
        enqueueReminder()

        return Result.success()
    }

    companion object {
        const val TAG_OUTPUT = "check_eating"

        private val workManager by lazy {
            WorkManager.getInstance()
        }

        @JvmStatic
        fun enqueueReminder() {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }
            val timeDifference = dueDate.timeInMillis - currentDate.timeInMillis

            workManager.run {
                cancelAllWorkByTag(TAG_OUTPUT)
                enqueue(OneTimeWorkRequest.Builder(ReminderWork::class.java)
                        .setInitialDelay(timeDifference, TimeUnit.MILLISECONDS)
                        .addTag(TAG_OUTPUT)
                        .build())
            }
        }
    }
}