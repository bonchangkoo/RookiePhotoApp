package kr.co.yogiyo.rookiephotoapp.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;

public class SettingsActivity extends BaseActivity implements AuthNavigator {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO : global application에 구현하면 어떨지 고민
        firebaseAuth = FirebaseAuth.getInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, SettingsFragment.newInstance())
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showToast(getString(R.string.text_signin_success));
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            callback.onSuccess(user);
                        } else {
                            showToast(getString(R.string.text_sign_fail));
                            callback.onFail();
                        }
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showToast(getString(R.string.text_signup_success));
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            callback.onSuccess(user);
                        } else {
                            showToast(getString(R.string.text_sign_fail));
                            callback.onFail();
                        }
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
