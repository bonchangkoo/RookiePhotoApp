package kr.co.yogiyo.rookiephotoapp

import android.app.DatePickerDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import kr.co.yogiyo.rookiephotoapp.diary.main.YearMonthPickerDialog
import java.util.*

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

    fun addProgressBarInto(layout: ViewGroup) {
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

    fun buildCalendarDialog(calendar: Calendar, listener: DatePickerDialog.OnDateSetListener): YearMonthPickerDialog {
        return YearMonthPickerDialog.newInstance(calendar.timeInMillis).apply {
            setListener(listener)
        }
    }

    companion object {

        const val EDIT_SELECTED_PHOTO = 0
        const val EDIT_CAPTURED_PHOTO = 1
    }
}
