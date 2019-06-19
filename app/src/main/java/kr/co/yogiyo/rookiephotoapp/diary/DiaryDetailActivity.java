package kr.co.yogiyo.rookiephotoapp.diary;


import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.Constants;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryViewModel;

public class DiaryDetailActivity extends BaseActivity implements View.OnClickListener {

    private LocalDiaryViewModel localDiaryViewModel;

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

        localDiaryViewModel = ViewModelProviders.of(this).get(LocalDiaryViewModel.class);

        diaryIdx = getIntent().getIntExtra("DIARY_IDX", 0);
        Log.d(DiaryDetailActivity.class.getSimpleName(), String.valueOf(diaryIdx));

        initView();
        setViewData(diaryIdx);
    }

    private void initView() {
        setSupportActionBar(findViewById(R.id.toolbar));

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
                createAlertDialog(DiaryDetailActivity.this, "다이어리 삭제", "정말로 삭제하시겠습니까?",
                        getString(R.string.text_dialog_ok), getString(R.string.text_dialog_no), (dialog, id) -> {
                            if (id == DialogInterface.BUTTON_POSITIVE) deleteDiary(diaryIdx);
                            else dialog.dismiss();
                        }, null).show();
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
        getCompositeDisposable().add(localDiaryViewModel.findDiaryById(diaryIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(diary -> {
                    setDateAndTime(diary.getDate());
                    Glide.with(DiaryDetailActivity.this)
                            .load(Constants.YOGIDIARY_PATH.getAbsolutePath() + File.separator + diary.getImage())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(detailPhotoImageView);
                    detailDescriptionTextView.setText(diary.getDescription());
                }));
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
        getCompositeDisposable().add(localDiaryViewModel.findDiaryById(idx)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(diary -> {
                    if (diary != null)
                        localDiaryViewModel.deleteDiary(diary)
                                .subscribeOn(Schedulers.single())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        //Do noting
                                    }

                                    @Override
                                    public void onComplete() {
                                        showToast(R.string.text_diary_detail_deleted);
                                        finish();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        showToast(getString(R.string.text_cant_delete_diary));
                                    }
                                });
                }));
    }
}
