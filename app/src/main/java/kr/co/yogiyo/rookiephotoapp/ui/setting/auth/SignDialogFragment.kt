package kr.co.yogiyo.rookiephotoapp.ui.setting.auth

import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.View

import kotlinx.android.synthetic.main.dialog_fragment_sign.view.*

import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.ui.setting.SettingsActivity

class SignDialogFragment : PreferenceDialogFragmentCompat() {

    lateinit var signInWithEmailAndPassword: (String, String) -> Unit
    lateinit var createUserWithEmailAndPassword: (String, String) -> Unit
    lateinit var onFail: () -> Unit

    // TODO : 콜백을 RxJava로 바꾸기
    // TODO : 구글 로그인 실패 A non-recoverable sign in failure occurred (status code: 12500)
    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        view?.btn_signin?.setOnClickListener {
            (context as SettingsActivity).showLoading(true)
            signInWithEmailAndPassword(view.edit_email?.text.toString(), view.edit_password?.text.toString())
        }
        view?.btn_signup?.setOnClickListener {
            (context as SettingsActivity).showLoading(true)
            createUserWithEmailAndPassword(view.edit_email?.text.toString(), view.edit_password?.text.toString())
        }
        view?.relative_signup?.setOnClickListener {
            dialog.setTitle(R.string.text_signup)
            view.run {
                text_show_sign_fail.visibility = View.GONE
                btn_signin?.visibility = View.GONE
                relative_signup?.visibility = View.GONE
                btn_signup?.visibility = View.VISIBLE
            }
        }

        onFail = {
            view?.text_show_sign_fail?.run {
                text = getString(R.string.text_sign_fail)
                visibility = View.VISIBLE
            }
            (context as SettingsActivity).run {
                showLoading(false)
                showToast(R.string.text_sign_fail)
            }
        }
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        builder?.run {
            setTitle(R.string.text_signin)
            setPositiveButton(null, null)
            setNegativeButton(null, null)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        // Do nothing
    }

    fun onSuccess() {
        (context as SettingsActivity).showLoading(false)
        dismiss()
    }

    companion object {

        fun newInstance(): SignDialogFragment {
            return SignDialogFragment()
        }
    }
}