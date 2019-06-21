package kr.co.yogiyo.rookiephotoapp.settings;

import android.os.Bundle;
import android.widget.ImageButton;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.GlobalApplication;
import kr.co.yogiyo.rookiephotoapp.R;

public class SettingsActivity extends BaseActivity implements AuthNavigator {

    public static final String PREFERENCE_KEY = "key";
    public static final String BACKUP_DIALOG_KEY = "backup_dialog";
    public static final String RESTORE_DIALOG_KEY = "restore_dialog";
    public static final String SIGN_DIALOG_KEY = "sign_dialog";
    public static final String PREFERENCE_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";
    public static final String SWITCH_REMINDER_KEY = "switch_reminder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton backspaceButton = findViewById(R.id.btn_backspace);
        backspaceButton.setOnClickListener(v -> onBackPressed());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.list_container, SettingsFragment.newInstance())
                .commit();
    }

    @Override
    public void signInWithEmailAndPassword(final String email, final String password,
                                           final SignCallback callback) {
        if (email.length() == 0 || password.length() == 0) {
            callback.onFail();
            return;
        }
        GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showToast(getString(R.string.text_signin_success));
                        callback.onSuccess(GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser());
                    } else {
                        showToast(getString(R.string.text_sign_fail));
                        callback.onFail();
                    }
                });
    }

    @Override
    public void createUserWithEmailAndPassword(final String email, final String password,
                                               final SignCallback callback) {
        if (email.length() == 0 || password.length() == 0) {
            callback.onFail();
            return;
        }
        GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showToast(getString(R.string.text_signup_success));
                        callback.onSuccess(GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser());
                    } else {
                        showToast(getString(R.string.text_sign_fail));
                        callback.onFail();
                    }
                });
    }

    @Override
    public void signOut(final SignCallback callback) {
        try {
            GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().signOut();
            callback.onSuccess(GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail();
        }
    }
}
