package kr.co.yogiyo.rookiephotoapp.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;

import kr.co.yogiyo.rookiephotoapp.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String PREFERENCE_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";
    private static final String SIGN_DIALOG_KEY = "sign_dialog";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private Preference signDialogPreference;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        signDialogPreference = findPreference(SIGN_DIALOG_KEY);

        if (firebaseAuth.getCurrentUser() == null) {
            signDialogPreference.setTitle(getString(R.string.text_need_to_signin));
        } else {
            signDialogPreference.setTitle(firebaseAuth.getCurrentUser().getEmail());
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof SignDialogPreference) {
            if (firebaseAuth.getCurrentUser() == null) {
                ((SignDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_sign);
                dialogFragment = new SignDialogFragment();
            } else {
                ((SignDialogPreference) preference).setDialogLayoutResource(R.layout.dialog_fragment_signout);
                dialogFragment = new SignOutDialogFragment();
            }

            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null && getFragmentManager() != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), PREFERENCE_DIALOG_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
