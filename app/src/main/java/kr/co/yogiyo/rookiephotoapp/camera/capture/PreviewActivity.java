package kr.co.yogiyo.rookiephotoapp.camera.capture;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import kr.co.yogiyo.rookiephotoapp.R;

public class PreviewActivity extends AppCompatActivity {

    private ImageView PreviewImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        PreviewImageView = findViewById(R.id.preview_image);

        Bitmap bitmap = ResultHolder.getBitmap();

        PreviewImageView.setVisibility(View.VISIBLE);
        PreviewImageView.setImageBitmap(bitmap);

        Toast.makeText(this, String.format("width: %d, height: %d \nsize : %f MB",
                bitmap.getWidth(), bitmap.getHeight(), getApproximateFileMegabytes(bitmap)),
                Toast.LENGTH_SHORT).show();
    }

    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024 / 1024;
    }
}