package kr.co.yogiyo.rookiephotoapp

import android.app.Application
import com.google.firebase.auth.FirebaseAuth

class GlobalApplication : Application() {

    var isFromDiary: Boolean = false

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

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
