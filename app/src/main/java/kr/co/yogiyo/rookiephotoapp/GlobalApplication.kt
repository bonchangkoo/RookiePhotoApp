package kr.co.yogiyo.rookiephotoapp

import android.app.Application
import kr.co.yogiyo.rookiephotoapp.notification.JobSchedulerStart

class GlobalApplication : Application() {

    var isFromDiary: Boolean = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        isFromDiary = false
        JobSchedulerStart.start(applicationContext)
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
