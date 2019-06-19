package kr.co.yogiyo.rookiephotoapp

import android.app.Application
import android.content.Context
import kr.co.yogiyo.rookiephotoapp.notification.ReminderWork

class GlobalApplication : Application() {

    var isFromDiary: Boolean = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        isFromDiary = false

        if (GlobalApplication.globalApplicationContext
                        .getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
                        .getBoolean("switch_reminder", false)) {
            ReminderWork.enqueueReminder()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        instance = null
    }

    companion object {
        private var instance: GlobalApplication? = null

        @JvmStatic
        val globalApplicationContext: GlobalApplication
            get() {
                return instance as GlobalApplication
            }
    }
}
