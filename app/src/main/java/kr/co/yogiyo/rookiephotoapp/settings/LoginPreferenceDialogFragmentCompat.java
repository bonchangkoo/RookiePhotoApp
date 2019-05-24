package kr.co.yogiyo.rookiephotoapp.settings;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import kr.co.yogiyo.rookiephotoapp.R;

public class LoginPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
        implements View.OnClickListener, LoginCallback {

    private AuthNavigator authNavigator;

    private EditText emailEdit;
    private EditText passwordEdit;
    private TextView showSignFailText;
    private ProgressBar signProgress;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AuthNavigator) {
            this.authNavigator = (AuthNavigator) context;
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Button signinBtn = view.findViewById(R.id.btn_signin);
        TextView signupTextBtn = view.findViewById(R.id.text_signup);
        emailEdit = view.findViewById(R.id.edit_email);
        passwordEdit = view.findViewById(R.id.edit_password);
        showSignFailText = view.findViewById(R.id.text_show_sign_fail);
        signProgress = view.findViewById(R.id.progress_sign);

        signinBtn.setOnClickListener(this);
        signupTextBtn.setOnClickListener(this);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // Do nothing
    }

    // TODO : 콜백 추가해서 다이얼로그 종료해야 함
    // TODO : 구글 로그인 실패 A non-recoverable sign in failure occurred (status code: 12500)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signin:
                signProgress.setVisibility(View.VISIBLE);
                authNavigator.signInWithEmailAndPassword(emailEdit.getText().toString(), passwordEdit.getText().toString(), this);
                break;
            case R.id.text_signup:
                signProgress.setVisibility(View.VISIBLE);
                authNavigator.createUserWithEmailAndPassword(emailEdit.getText().toString(), passwordEdit.getText().toString(), this);
                break;
        }
    }

    @Override
    public void onSuccess(FirebaseUser user) {
        // TODO : 가입 성공 후 코드
        dismiss();
    }

    @Override
    public void onFail() {
        showSignFailText.setText(getString(R.string.text_sign_fail));
        showSignFailText.setVisibility(View.VISIBLE);
        signProgress.setVisibility(View.GONE);
    }
}