package kr.co.yogiyo.rookiephotoapp.settings;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import kr.co.yogiyo.rookiephotoapp.R;

public class LoginDialogPreference extends DialogPreference {
    public LoginDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_fragment_login);
    }
}