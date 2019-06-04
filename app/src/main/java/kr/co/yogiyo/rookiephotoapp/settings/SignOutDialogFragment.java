package kr.co.yogiyo.rookiephotoapp.settings;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import kr.co.yogiyo.rookiephotoapp.R;

public class SignOutDialogFragment extends PreferenceDialogFragmentCompat
        implements View.OnClickListener, SignCallback {

    private Context context;

    public static SignOutDialogFragment newInstance(){
        return new SignOutDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        RelativeLayout signoutDialogRelative = view.findViewById(R.id.relative_signout_dialog);
        TextView cancelText = view.findViewById(R.id.text_cancel);
        TextView signoutText = view.findViewById(R.id.text_signout);
        ((SettingsActivity) context).addProgressBarInto(signoutDialogRelative);

        cancelText.setOnClickListener(this);
        signoutText.setOnClickListener(this);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setTitle("로그아웃")
                .setPositiveButton(null, null)
                .setNegativeButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // Do nothing
    }

    // TODO : 콜백을 RxJava로 바꿀 수 있을지 고민하기
    // TODO : 취소할 때 요청 취소할 수 있는지 조사
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_cancel:
                dismiss();
                break;
            case R.id.text_signout:
                ((SettingsActivity) context).showLoading();
                ((AuthNavigator) context).signOut(this);
                break;
        }
    }

    @Override
    public void onSuccess(FirebaseUser user) {
        Preference preference = getPreference();
        preference.setTitle(getString(R.string.text_need_to_signin));
        ((SettingsActivity) context).showToast("로그아웃 성공");
        dismiss();
    }

    @Override
    public void onFail() {
        ((SettingsActivity) context).showToast("로그아웃 실패");
        dismiss();
    }
}