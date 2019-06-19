package kr.co.yogiyo.rookiephotoapp.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import kr.co.yogiyo.rookiephotoapp.GlobalApplication;
import kr.co.yogiyo.rookiephotoapp.R;

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
        signDialogPreference = findPreference(SettingsActivity.SIGN_DIALOG_KEY);
        backupDialogPreference = findPreference(SettingsActivity.BACKUP_DIALOG_KEY);
        restoreDialogPreference = findPreference(SettingsActivity.RESTORE_DIALOG_KEY);

        if (GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser() == null) {
            signDialogPreference.setTitle(getString(R.string.text_need_to_signin));
        } else {
            signDialogPreference.setTitle(GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser().getEmail());
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof SettingsDialogPreference) {
            if (GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser() == null) {
                preference = signDialogPreference;
                ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_sign);
                dialogFragment = SignDialogFragment.newInstance();
            } else {
                switch (preference.getKey()) {
                    case SettingsActivity.SIGN_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_signout);
                        dialogFragment = SignOutDialogFragment.newInstance();
                        break;
                    case SettingsActivity.BACKUP_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_backup_restore);
                        dialogFragment = BackupRestoreDialogFragment.newInstance();
                        break;
                    case SettingsActivity.RESTORE_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_backup_restore);
                        dialogFragment = BackupRestoreDialogFragment.newInstance();
                        break;
                }
            }

            Bundle bundle = new Bundle(1);
            bundle.putString(SettingsActivity.PREFERENCE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null && getFragmentManager() != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), SettingsActivity.PREFERENCE_DIALOG_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case SettingsActivity.BACKUP_DIALOG_KEY:
                onDisplayPreferenceDialog(backupDialogPreference);
                return true;
            case SettingsActivity.RESTORE_DIALOG_KEY:
                onDisplayPreferenceDialog(restoreDialogPreference);
                return true;
            default:
                return false;
        }
    }
}


