package kr.co.yogiyo.rookiephotoapp.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabaseCallback;
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryManager;

public class DiaryDetailActivity extends BaseActivity implements DiaryDatabaseCallback, View.OnClickListener {

    private DiaryDatabase diaryDatabase;

    private ImageButton backImageButton;
    private ImageButton diaryDeleteImageButton;
    private ImageButton diaryEditImageButton;

    private TextView detailDateTextView;
    private TextView detailTimeTextView;
    private ImageView detailPhotoImageView;
    private TextView detailDescriptionTextView;

    private static int diaryIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        if (diaryDatabase == null) {
            diaryDatabase = DiaryDatabase.getDatabase(DiaryDetailActivity.this);
        }

        diaryIdx = getIntent().getIntExtra("DIARY_IDX", 0);

        initView();
        setViewData(diaryIdx);
    }

    private void initView() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        backImageButton = findViewById(R.id.ib_back);
        diaryDeleteImageButton = findViewById(R.id.ib_diary_delete);
        diaryEditImageButton = findViewById(R.id.ib_diary_edit);

        backImageButton.setOnClickListener(this);
        diaryDeleteImageButton.setOnClickListener(this);
        diaryEditImageButton.setOnClickListener(this);

        detailDateTextView = findViewById(R.id.tv_diary_detail_date);
        detailTimeTextView = findViewById(R.id.tv_diary_detail_time);
        detailPhotoImageView = findViewById(R.id.iv_diary_detail_photo);
        detailDescriptionTextView = findViewById(R.id.tv_diary_detail_description);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                onBackPressed();
                break;
            case R.id.ib_diary_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(DiaryDetailActivity.this);
                builder.setPositiveButton(getString(R.string.text_dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteDiary(diaryIdx);
                    }
                });
                builder.setNegativeButton(getString(R.string.text_dialog_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setTitle("다이어리 삭제");
                dialog.setMessage("정말로 삭제하시겠습니까?");
                dialog.show();
                break;
            case R.id.ib_diary_edit:
                Intent diaryEditActivityIntent = new Intent(this, DiaryEditActivity.class);
                diaryEditActivityIntent.putExtra("DIARY_IDX", diaryIdx);
                startActivity(diaryEditActivityIntent);
                finish();
                break;
        }
    }

    private void setViewData(int diaryIndex) {
        LocalDiaryManager.getInstance(DiaryDetailActivity.this).findDiaryById(DiaryDetailActivity.this, diaryIndex);
    }

    private void setDateAndTime(Date dateAndTime) {

        SimpleDateFormat monthFormat = new SimpleDateFormat("M", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

        String month = monthFormat.format(dateAndTime);
        String day = dayFormat.format(dateAndTime);

        detailDateTextView.setText(String.format("%s월 %s일", month, day));

        SimpleDateFormat hourFormat = new SimpleDateFormat("hh", Locale.getDefault());
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.getDefault());
        SimpleDateFormat meridiemFormat = new SimpleDateFormat("aa", Locale.ENGLISH);

        String hour = hourFormat.format(dateAndTime);
        String minute = minuteFormat.format(dateAndTime);
        String meridiem = meridiemFormat.format(dateAndTime);

        detailTimeTextView.setText(String.format("%s:%s%s", hour, minute, meridiem));
    }

    private void deleteDiary(int idx) {

        compositeDisposable.add(diaryDatabase.diaryDao().findDiaryById(idx)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Diary>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Diary diary) {
                        if (diary != null)
                            LocalDiaryManager.getInstance(DiaryDetailActivity.this).deleteDiary(DiaryDetailActivity.this, diary);

                    }
                }));
    }

    @Override
    public void onDiaryAdded() {
        // NO ACTION
    }

    @Override
    public void onDiaryByIdFinded(Diary diary) {
        setDateAndTime(diary.getDate());
        detailPhotoImageView.setImageURI(Uri.fromFile(new File(YOGIDIARY_PATH, diary.getImage())));
        detailDescriptionTextView.setText(diary.getDescription());
    }

    @Override
    public void onDiaryUpdated() {
        // NO ACTION
    }

    @Override
    public void onDiaryDeleted() {
        showToast(R.string.text_diary_detail_deleted);
        finish();
    }

    @Override
    public void onDiaryError(String errorMessage) {
        showToast(errorMessage);
    }
}
