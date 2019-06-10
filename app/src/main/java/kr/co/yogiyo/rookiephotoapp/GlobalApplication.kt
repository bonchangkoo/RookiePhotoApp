package kr.co.yogiyo.rookiephotoapp

import android.app.Application

class GlobalApplication : Application() {

    var fromDiary: Boolean = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        fromDiary = false
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
