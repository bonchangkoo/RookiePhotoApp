package kr.co.yogiyo.rookiephotoapp.settings;

import com.google.firebase.auth.FirebaseUser;

public interface SignCallback {
    void onSuccess(FirebaseUser user);

    void onFail();
}
