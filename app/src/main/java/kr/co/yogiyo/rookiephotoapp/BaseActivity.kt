package kr.co.yogiyo.rookiephotoapp

import android.support.design.widget.Snackbar
import android.content.Context
import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity : AppCompatActivity() {

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private lateinit var progressBar: ProgressBar

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

    fun showSnackbar(view: View, toastMessage: String?){
        Snackbar.make(view, toastMessage.toString(), Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackbar(view: View, stringId: Int){
        Snackbar.make(view, stringId, Snackbar.LENGTH_SHORT).show()
    }

    fun addProgressBarInto(layout: RelativeLayout) {
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(200, 200)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(progressBar, params)
        progressBar.visibility = View.GONE
    }

    fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideLoading() {
        progressBar.visibility = View.GONE
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
                    setOnShowListener(if(onShowListener == null){
                        DialogInterface.OnShowListener {dialog ->
                            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this@BaseActivity, R.color.color_FDB32D))
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this@BaseActivity, R.color.color_FDB32D))
                        }
                    } else onShowListener)
                }
    }

    companion object {

        const val EDIT_SELECTED_PHOTO = 0
        const val EDIT_CAPTURED_PHOTO = 1
    }
}