package kr.co.yogiyo.rookiephotoapp.ui.setting

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.ui.setting.backup_restore.BackupRestoreDialogFragment
import kr.co.yogiyo.rookiephotoapp.ui.setting.auth.SignDialogFragment
import kr.co.yogiyo.rookiephotoapp.ui.setting.auth.SignOutDialogFragment

// TODO : preference엔 databinding이 지원되지 않는다고 해서 observe로 구현해
class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    private val backupDialogPreference by lazy { findPreference(SettingsActivity.BACKUP_DIALOG_KEY) }
    private val restoreDialogPreference by lazy { findPreference(SettingsActivity.RESTORE_DIALOG_KEY) }
    private val signDialogPreference by lazy { findPreference(SettingsActivity.SIGN_DIALOG_KEY) }
    private val signOutDialogFragment by lazy {
        SignOutDialogFragment.newInstance().apply {
            signOut = {
                try {
                    GlobalApplication.globalApplicationContext.firebaseAuth.signOut()
                    signDialogPreference.title = getString(R.string.text_need_to_signin)
                    this.onSuccess()
                } catch (e: Exception) {
                    e.printStackTrace()
                    this.onFail()
                }
            }
        }
    }
    private val signDialogFragment by lazy {
        SignDialogFragment.newInstance().apply {
            signInWithEmailAndPassword = { email, password ->
                if (email.isEmpty() || password.isEmpty()) {
                    this.onFail()
                } else {
                    GlobalApplication.globalApplicationContext.firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    this.onSuccess()
                                    (context as SettingsActivity).showToast(R.string.text_signin_success)
                                    signDialogPreference.title = GlobalApplication.globalApplicationContext.firebaseAuth.currentUser?.email
                                            ?: getString(R.string.text_need_to_signin)
                                } else {
                                    this.onFail()
                                }
                            }
                }
            }
            createUserWithEmailAndPassword = { email, password ->
                if (email.isEmpty() || password.isEmpty()) {
                    this.onFail()
                } else {
                    GlobalApplication.globalApplicationContext.firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    this.onSuccess()
                                    (context as SettingsActivity).showToast(R.string.text_signup_success)
                                    signDialogPreference.title = GlobalApplication.globalApplicationContext.firebaseAuth.currentUser?.email
                                            ?: getString(R.string.text_need_to_signin)
                                } else {
                                    this.onFail()
                                }
                            }
                }
            }
        }
    }

    private lateinit var dialogFragment: DialogFragment

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        signDialogPreference.title = GlobalApplication.globalApplicationContext.firebaseAuth.currentUser?.email
                ?: getString(R.string.text_need_to_signin)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is SettingsDialogPreference) {
            if (GlobalApplication.globalApplicationContext.firebaseAuth.currentUser == null) {
                preference.dialogLayoutResource = R.layout.dialog_fragment_sign
                dialogFragment = signDialogFragment
            } else {
                preference.dialogLayoutResource = 0
                when (preference.key) {
                    SettingsActivity.SIGN_DIALOG_KEY -> { dialogFragment = signOutDialogFragment }
                    SettingsActivity.BACKUP_DIALOG_KEY, SettingsActivity.RESTORE_DIALOG_KEY -> {
                        dialogFragment = BackupRestoreDialogFragment.newInstance()
                    }
                }
            }

            dialogFragment.arguments = Bundle(1).apply {
                putString(SettingsActivity.PREFERENCE_KEY, preference.key)
            }
        }

        fragmentManager?.let { fragmentManager ->
            dialogFragment.run {
                setTargetFragment(this@SettingsFragment, 0)
                show(fragmentManager, SettingsActivity.PREFERENCE_DIALOG_TAG)
            }
        } ?: super.onDisplayPreferenceDialog(preference)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return when (preference.key) {
            SettingsActivity.BACKUP_DIALOG_KEY -> {
                GlobalApplication.globalApplicationContext.firebaseAuth.currentUser?.let {
                    onDisplayPreferenceDialog(backupDialogPreference)
                } ?: onDisplayPreferenceDialog(signDialogPreference)
                true
            }
            SettingsActivity.RESTORE_DIALOG_KEY -> {
                GlobalApplication.globalApplicationContext.firebaseAuth.currentUser?.let {
                    onDisplayPreferenceDialog(restoreDialogPreference)
                } ?: onDisplayPreferenceDialog(signDialogPreference)
                true
            }
            else -> false
        }
    }

    companion object {

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}


