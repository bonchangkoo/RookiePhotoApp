package kr.co.yogiyo.rookiephotoapp.camera.capture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import kr.co.yogiyo.rookiephotoapp.R;

public class PreviewActivity extends AppCompatActivity {

    private ImageView PreviewImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        PreviewImageView = findViewById(R.id.preview_image);

        byte[] jpeg = ResultHolder.getImage();

        if (jpeg != null) {
            PreviewImageView.setVisibility(View.VISIBLE);

            Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

            if (bitmap == null) {
                finish();
                return;
            }

            PreviewImageView.setImageBitmap(bitmap);
        }


    }
}