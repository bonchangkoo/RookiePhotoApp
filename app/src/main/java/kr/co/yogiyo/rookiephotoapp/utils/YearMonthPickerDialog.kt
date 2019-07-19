package kr.co.yogiyo.rookiephotoapp.utils

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import kotlinx.android.synthetic.main.picker_year_month.view.*
import kr.co.yogiyo.rookiephotoapp.R
import java.util.Calendar

class YearMonthPickerDialog : DialogFragment() {

    private lateinit var listener: DatePickerDialog.OnDateSetListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.picker_year_month, null)

        val yearPicker = view.picker_year.apply {
            maxValue = MAX_YEAR
            minValue = MIN_YEAR
            value = Calendar.getInstance().run {
                timeInMillis = arguments!!.getLong(MILLISECOND_TIME)
                get(Calendar.YEAR)
            }
        }

        val monthPicker = view.picker_month.apply {
            maxValue = MAX_MONTH
            minValue = MIN_MONTH
            value = Calendar.getInstance().run {
                timeInMillis = arguments!!.getLong(MILLISECOND_TIME)
                get(Calendar.MONTH) + 1
            }
        }

        view.btn_confirm.run {
            setOnClickListener {
                listener.onDateSet(null, yearPicker.value, monthPicker.value - 1, 1)
                dismiss()
            }
        }

        view.btn_cancel.run {
            setOnClickListener { dismiss() }
        }

        return AlertDialog.Builder(context)
                .setView(view)
                .create()
    }

    fun setListener(listener: DatePickerDialog.OnDateSetListener) {
        this.listener = listener
    }

    companion object {

        private const val MAX_YEAR = 2099
        private const val MIN_YEAR = 1980
        private const val MAX_MONTH = 12
        private const val MIN_MONTH = 1
        private const val MILLISECOND_TIME = "millisecond_time"

        fun newInstance(millisecondTime: Long): YearMonthPickerDialog {
            return YearMonthPickerDialog().apply {
                arguments = Bundle().apply {
                    putLong(MILLISECOND_TIME, millisecondTime)
                }
            }
        }
    }
}
