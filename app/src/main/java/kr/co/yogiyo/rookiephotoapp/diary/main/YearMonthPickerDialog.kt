package kr.co.yogiyo.rookiephotoapp.diary.main

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.Button
import android.widget.NumberPicker
import kr.co.yogiyo.rookiephotoapp.R
import java.util.*

class YearMonthPickerDialog : DialogFragment() {

    private lateinit var listener: DatePickerDialog.OnDateSetListener
    private lateinit var yearPicker: NumberPicker
    private lateinit var monthPicker: NumberPicker
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.picker_year_month, null)

        val builder = AlertDialog.Builder(context).apply {
            yearPicker = view.findViewById<NumberPicker>(R.id.picker_year).apply {
                maxValue = MAX_YEAR
                minValue = MIN_YEAR
                value = Calendar.getInstance().run {
                    timeInMillis = arguments!!.getLong(MILLISECOND_TIME)
                    get(Calendar.YEAR)
                }
            }

            monthPicker = view.findViewById<NumberPicker>(R.id.picker_month).apply {
                maxValue = MAX_MONTH
                minValue = MIN_MONTH
                value = Calendar.getInstance().run {
                    timeInMillis = arguments!!.getLong(MILLISECOND_TIME)
                    get(Calendar.MONTH) + 1
                }
            }

            confirmButton = view.findViewById<Button>(R.id.btn_confirm).apply {
                setOnClickListener {
                    listener.onDateSet(null, yearPicker.value, monthPicker.value - 1, 1)
                    dismiss()
                }
            }
            cancelButton = view.findViewById<Button>(R.id.btn_cancel).apply {
                setOnClickListener { dismiss() }
            }

            setView(view)
        }

        return builder.create()
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
