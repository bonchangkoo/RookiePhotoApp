package kr.co.yogiyo.rookiephotoapp.settings

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btn_backspace.setOnClickListener { onBackPressed() }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.list_container, SettingsFragment.newInstance())
                .commit()
    }

    companion object {

        const val PREFERENCE_KEY = "key"
        const val BACKUP_DIALOG_KEY = "backup_dialog"
        const val RESTORE_DIALOG_KEY = "restore_dialog"
        const val SIGN_DIALOG_KEY = "sign_dialog"
        const val PREFERENCE_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG"
    }
}
