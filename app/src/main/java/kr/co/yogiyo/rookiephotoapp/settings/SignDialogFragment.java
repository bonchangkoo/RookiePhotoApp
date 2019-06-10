package kr.co.yogiyo.rookiephotoapp.settings;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import kr.co.yogiyo.rookiephotoapp.R;

import static kr.co.yogiyo.rookiephotoapp.Constants.PREFERENCE_KEY;
import static kr.co.yogiyo.rookiephotoapp.Constants.SIGN_DIALOG_KEY;

public class SignDialogFragment extends PreferenceDialogFragmentCompat
        implements View.OnClickListener, SignCallback {

    private Context context;

    private Button signinButton;
    private Button signupButton;
    private RelativeLayout signupRelativeButton;
    private EditText emailEdit;
    private EditText passwordEdit;
    private TextView showSignFailText;

    public static SignDialogFragment newInstance() {
        return new SignDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        RelativeLayout signinDialogRelative = view.findViewById(R.id.relative_signin_dialog);
        signinButton = view.findViewById(R.id.btn_signin);
        signupButton = view.findViewById(R.id.btn_signup);
        signupRelativeButton = view.findViewById(R.id.relative_signup);
        emailEdit = view.findViewById(R.id.edit_email);
        passwordEdit = view.findViewById(R.id.edit_password);
        showSignFailText = view.findViewById(R.id.text_show_sign_fail);
        ((SettingsActivity) context).addProgressBarInto(signinDialogRelative);

        signinButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);
        signupRelativeButton.setOnClickListener(this);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setTitle(getString(R.string.text_signin))
                .setPositiveButton(null, null)
                .setNegativeButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // Do nothing
    }

    // TODO : 콜백을 RxJava로 바꿀 수 있을지 고민하기
    // TODO : 구글 로그인 실패 A non-recoverable sign in failure occurred (status code: 12500)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signin:
                ((SettingsActivity) context).showLoading();
                ((AuthNavigator) context).signInWithEmailAndPassword(
                        emailEdit.getText().toString(), passwordEdit.getText().toString(), this);
                break;
            case R.id.btn_signup:
                ((SettingsActivity) context).showLoading();
                ((AuthNavigator) context).createUserWithEmailAndPassword(
                        emailEdit.getText().toString(), passwordEdit.getText().toString(), this);
                break;
            case R.id.relative_signup:
                getDialog().setTitle(getString(R.string.text_signup));
                signinButton.setVisibility(View.GONE);
                signupRelativeButton.setVisibility(View.GONE);
                signupButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onSuccess(FirebaseUser user) {
        String preferenceKey = getArguments().getString(PREFERENCE_KEY);
        if (preferenceKey.equals(SIGN_DIALOG_KEY)) {
            Preference preference = getPreference();
            preference.setTitle(user.getEmail());
        }
        ((SettingsActivity) context).showToast(getString(R.string.text_signin_success));
        dismiss();
    }

    @Override
    public void onFail() {
        showSignFailText.setText(getString(R.string.text_sign_fail));
        showSignFailText.setVisibility(View.VISIBLE);
        ((SettingsActivity) context).hideLoading();
    }
}