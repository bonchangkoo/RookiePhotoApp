package kr.co.yogiyo.rookiephotoapp

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

    companion object {

        const val EDIT_SELECTED_PHOTO = 0
        const val EDIT_CAPTURED_PHOTO = 1
    }
}
