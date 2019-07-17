package kr.co.yogiyo.rookiephotoapp

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import kr.co.yogiyo.rookiephotoapp.ui.setting.notification.ReminderWork
import com.google.firebase.auth.FirebaseAuth

class GlobalApplication : Application() {

    var isFromDiary: Boolean = false

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

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
