package kr.co.yogiyo.rookiephotoapp

import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import kr.co.yogiyo.rookiephotoapp.util.LoadingDialogFragment

open class BaseActivity : AppCompatActivity() {

    private val loadingDialogFragment by lazy { LoadingDialogFragment() }

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    fun showToast(toastMessage: String?) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    fun showToast(stringId: Int) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show()
    }

    fun showLoading(visible: Boolean) {
        when {
            visible -> loadingDialogFragment.show(supportFragmentManager, "show_loading")
            else -> loadingDialogFragment.dismiss()
        }
    }

    companion object {

        const val EDIT_SELECTED_PHOTO = 0
        const val EDIT_CAPTURED_PHOTO = 1
    }
}
