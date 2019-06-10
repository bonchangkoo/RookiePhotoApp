package kr.co.yogiyo.rookiephotoapp.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;

import static kr.co.yogiyo.rookiephotoapp.Constants.firebaseAuth;

public class SettingsActivity extends BaseActivity implements AuthNavigator {

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
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showToast(getString(R.string.text_signin_success));
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        callback.onSuccess(user);
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

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showToast(getString(R.string.text_signup_success));
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        showToast(getString(R.string.text_sign_fail));
                        callback.onFail();
                    }
                });
    }

    @Override
    public void signOut(final SignCallback callback) {
        try {
            firebaseAuth.signOut();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            callback.onSuccess(user);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail();
        }
    }
}
