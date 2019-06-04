package kr.co.yogiyo.rookiephotoapp.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;

import kr.co.yogiyo.rookiephotoapp.R;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    public static final String PREFERENCE_KEY = "key";
    public static final String BACKUP_DIALOG_KEY = "backup_dialog";
    public static final String RESTORE_DIALOG_KEY = "restore_dialog";
    public static final String SIGN_DIALOG_KEY = "sign_dialog";

    private static final String PREFERENCE_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private Preference backupDialogPreference;
    private Preference restoreDialogPreference;

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
        Preference signDialogPreference = findPreference(SIGN_DIALOG_KEY);
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
                ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_sign);
                dialogFragment = SignDialogFragment.newInstance();
            } else {
                // TODO : 백업/복원인지 BackupRestoreDialogFragment 에게 어떻게 알릴지..
                switch (preference.getKey()) {
                    case SIGN_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_signout);
                        dialogFragment = SignOutDialogFragment.newInstance();
                        break;
                    case BACKUP_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_backup_restore);
                        ((SettingsDialogPreference) preference).setDialogTitle(getString(R.string.text_do_backup));
                        ((SettingsDialogPreference) preference).setDialogMessage(getString(R.string.text_ask_backup));
                        dialogFragment = BackupRestoreDialogFragment.newInstance();
                        break;
                    case RESTORE_DIALOG_KEY:
                        ((SettingsDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_backup_restore);
                        ((SettingsDialogPreference) preference).setDialogTitle(getString(R.string.text_do_restore));
                        ((SettingsDialogPreference) preference).setDialogMessage(getString(R.string.text_ask_restore));
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


