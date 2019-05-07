package kr.co.yogiyo.rookiephotoapp.edit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import kr.co.yogiyo.rookiephotoapp.R;

public class EditResultActivity extends AppCompatActivity {

    private static final String TAG = EditResultActivity.class.getSimpleName();


    public static void startWithUri(@NonNull Context context, @NonNull Uri uri) {
        Intent intent = new Intent(context, EditResultActivity.class);
        intent.setData(uri);
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_result);
    }
}
