package kr.co.yogiyo.rookiephotoapp

import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import java.io.File

open class BaseActivity : AppCompatActivity() {

    protected open fun destroy() {
        compositeDisposable?.run {
            if (!this.isDisposed) {
                this.dispose()
            }
        }
    }

    protected fun showToast(toastMessage: String?) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    protected fun showToast(stringId: Int) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show()
    }

    companion object {

        @JvmField
        val EDIT_SELECTED_PHOTO = 0
        @JvmField
        val EDIT_CAPTURED_PHOTO = 1
        @JvmField
        var compositeDisposable: CompositeDisposable? = CompositeDisposable()
        @JvmField
        val STARTING_POINT = "startingPoint"
        @JvmField
        val YOGIDIARY_PATH = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YogiDiary")

    }
}
