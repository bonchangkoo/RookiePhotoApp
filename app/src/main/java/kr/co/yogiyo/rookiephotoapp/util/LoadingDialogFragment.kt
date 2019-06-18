package kr.co.yogiyo.rookiephotoapp.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import kr.co.yogiyo.rookiephotoapp.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setView(View.inflate(context, R.layout.dialog_fragment_loading, null))
                .create().apply {
                    window?.run {
                        setBackgroundDrawableResource(android.R.color.transparent)
                        setDimAmount(0f)
                    }
                }
    }
}