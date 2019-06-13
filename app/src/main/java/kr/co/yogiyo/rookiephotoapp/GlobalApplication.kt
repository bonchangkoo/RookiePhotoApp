package kr.co.yogiyo.rookiephotoapp

import android.app.Application
import android.graphics.Bitmap

class GlobalApplication : Application() {

    var isFromDiary: Boolean = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        isFromDiary = false
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
