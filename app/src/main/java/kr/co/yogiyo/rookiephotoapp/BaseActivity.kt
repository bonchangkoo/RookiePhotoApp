package kr.co.yogiyo.rookiephotoapp

import android.support.v7.app.AppCompatActivity
import android.widget.Toast

open class BaseActivity : AppCompatActivity() {

    protected fun showToast(toastMessage: String?) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    protected fun showToast(stringId: Int) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show()
    }

    companion object {

        @JvmStatic
        protected val EDIT_SELECTED_PHOTO = 0
        @JvmStatic
        protected val EDIT_CAPTURED_PHOTO = 1
    }
}
