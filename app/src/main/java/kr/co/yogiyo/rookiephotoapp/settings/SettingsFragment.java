package kr.co.yogiyo.rookiephotoapp.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import kr.co.yogiyo.rookiephotoapp.R;

import static kr.co.yogiyo.rookiephotoapp.Constants.BACKUP_DIALOG_KEY;
import static kr.co.yogiyo.rookiephotoapp.Constants.PREFERENCE_DIALOG_TAG;
import static kr.co.yogiyo.rookiephotoapp.Constants.PREFERENCE_KEY;
import static kr.co.yogiyo.rookiephotoapp.Constants.RESTORE_DIALOG_KEY;
import static kr.co.yogiyo.rookiephotoapp.Constants.SIGN_DIALOG_KEY;
import static kr.co.yogiyo.rookiephotoapp.Constants.firebaseAuth;

// TODO : preference엔 databinding이 지원되지 않는다고 해서 observe로 구현해야 할 것 같음
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    private Preference backupDialogPreference;
    private Preference restoreDialogPreference;
    private Preference signDialogPreference;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        signDialogPreference = findPreference(SIGN_DIALOG_KEY);
        backupDialogPreference = findPreference(BACKUP_DIALOG_KEY);
        restoreDialogPreference = findPreference(RESTORE_DIALOG_KEY);

        if (firebaseAuth.getCurrentUser() == null) {
            signDialogPreference.setTitle(getString(R.string.text_need_to_signin));
        } else {
            signDialogPreference.setTitle(firebaseAuth.getCurrentUser().getEmail());
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof SettingsDialogPreference) {
            if (firebaseAuth.getCurrentUser() == null) {
                preference = signDialogPreference;
                ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_sign);
                dialogFragment = SignDialogFragment.newInstance();
            } else {
                switch (preference.getKey()) {
                    case SIGN_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_signout);
                        dialogFragment = SignOutDialogFragment.newInstance();
                        break;
                    case BACKUP_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_backup_restore);
                        dialogFragment = BackupRestoreDialogFragment.newInstance();
                        break;
                    case RESTORE_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_backup_restore);
                        dialogFragment = BackupRestoreDialogFragment.newInstance();
                        break;
                }
            }

            Bundle bundle = new Bundle(1);
            bundle.putString(PREFERENCE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null && getFragmentManager() != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), PREFERENCE_DIALOG_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case BACKUP_DIALOG_KEY:
                onDisplayPreferenceDialog(backupDialogPreference);

                return true;
            case RESTORE_DIALOG_KEY:
                onDisplayPreferenceDialog(restoreDialogPreference);

                return true;
            default:
                return false;
        }
    }
}


