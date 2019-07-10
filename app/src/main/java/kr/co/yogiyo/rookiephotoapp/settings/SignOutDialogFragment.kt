package kr.co.yogiyo.rookiephotoapp.settings

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceDialogFragmentCompat

import kr.co.yogiyo.rookiephotoapp.R

class SignOutDialogFragment : PreferenceDialogFragmentCompat() {

    lateinit var signOut: () -> Unit

    // TODO : 콜백을 RxJava로 바꿀 수 있을지 고민하기
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        builder?.run {
            setTitle(R.string.text_signout)
            setMessage(R.string.text_ask_signout)
            setPositiveButton(R.string.text_dialog_ok, null)
            setNegativeButton(R.string.text_dialog_no) { _, _ -> dismiss() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog.setOnShowListener { dialog ->
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                (context as SettingsActivity).showLoading(true)
                signOut()
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        // Do nothing
    }

    fun onSuccess() {
        (context as SettingsActivity).run {
            showLoading(false)
            showToast(R.string.text_signout_success)
        }
        dismiss()
    }

    fun onFail() {
        (context as SettingsActivity).run {
            showLoading(false)
            showToast(R.string.text_signout_fail)
        }
        dismiss()
    }

    companion object {

        fun newInstance(): SignOutDialogFragment {
            return SignOutDialogFragment()
        }
    }
}