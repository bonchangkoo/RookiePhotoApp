package kr.co.yogiyo.rookiephotoapp

import android.content.Context
import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
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
        with(loadingDialogFragment) {
            when {
                visible -> show(this@BaseActivity.supportFragmentManager, "show_loading")
                else -> dismiss()
            }
        }
    }

    fun createAlertDialog(context: Context,
                          title: String? = null, message: String? = null,
                          positiveButtonText: String? = null, negativeButtonText: String? = null,
                          onClickListener: DialogInterface.OnClickListener? = null,
                          onShowListener: DialogInterface.OnShowListener? = null): AlertDialog {

        return AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, onClickListener)
                .setNegativeButton(negativeButtonText, onClickListener)
                .create().apply {
                    setOnShowListener(onShowListener ?: DialogInterface.OnShowListener { dialog ->
                        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(ContextCompat.getColor(this@BaseActivity, R.color.color_fdb32d))
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                .setTextColor(ContextCompat.getColor(this@BaseActivity, R.color.color_fdb32d))
                    })
                }
    }

    companion object {

        const val EDIT_SELECTED_PHOTO = 0
        const val EDIT_CAPTURED_PHOTO = 1
    }
}
