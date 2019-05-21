package kr.co.yogiyo.rookiephotoapp.diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabaseCallback;
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryManager;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;

public class DiaryEditActivity extends BaseActivity implements View.OnClickListener, DiaryDatabaseCallback {

    private final static String TAG = DiaryEditActivity.class.getSimpleName();

    private final static String DIARY_ADD = "NEW";

    private DiaryDatabase diaryDatabase;

    private TextView editDateTextView;
    private TextView editTimeTextView;
    private ImageButton editPhotoImageButton;
    private TextView editDescriptionTextView;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    private static String diaryIdx;

    private Uri selectedUri;
    private int updateHour;
    private int updateMinute;

    private String photoFileName;
    private boolean isPhotoUpdate = false;

    private static int testDiaryIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        // 데이터베이스 생성
        if (diaryDatabase == null) {
            diaryDatabase = DiaryDatabase.getDatabase(this);
        }

        // 다이어리 인덱스
        diaryIdx = getIntent().getStringExtra("DIARY_IDX");

        // View 초기화
        doSetActionBar();
        initView();
        setViewData(diaryIdx);
    }

    private void doSetActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (diaryIdx.equals(DIARY_ADD)) { // 추가 화면일 경우 툴바 타이틀 변경
            toolbar.setTitle(R.string.text_diary_add_title);
        }
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_diary_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_diary_save) { // 해당 다이어리 저장 (DB)
            saveViewData();
            finish();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DiaryEditActivity.this);
        builder.setPositiveButton(getString(R.string.text_dialog_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.text_dialog_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setTitle("작성 취소");
        dialog.setMessage("정말로 취소하시겠습니까?");
        dialog.show();
    }

    private void initView() {
        editDateTextView = findViewById(R.id.tv_diary_edit_date);
        editTimeTextView = findViewById(R.id.tv_diary_edit_time);
        editPhotoImageButton = findViewById(R.id.ib_diary_edit_photo);
        editDescriptionTextView = findViewById(R.id.et_diary_edit_description);

        editDateTextView.setOnClickListener(this);
        editTimeTextView.setOnClickListener(this);
        editPhotoImageButton.setOnClickListener(this);
        editDescriptionTextView.setOnClickListener(this);
    }

    /**
     * 다이어리 데이터 적용
     * diaryIdx가 NEW일 경우, 현재 날짜와 시간으로 설정
     * NEW가 아닐 경우, Diary DB에서 다이어리 인덱스로 조회
     */
    private void setViewData(String idx) {
        if (idx != null && idx.equals(DIARY_ADD)) {
            Date CurrentTime = Calendar.getInstance().getTime();
            setDateAndTime(CurrentTime);
        } else {
            LocalDiaryManager.getInstance(DiaryEditActivity.this).findDiaryById(this, idx);
        }
    }

    /**
     * Date타입 인자를 받아 뷰에 날짜와 시간을 설정합니다
     *
     * @param dateAndTime Date타입
     */
    private void setDateAndTime(Date dateAndTime) {

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("M", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

        String year = yearFormat.format(dateAndTime);
        String month = monthFormat.format(dateAndTime);
        String day = dayFormat.format(dateAndTime);

        // 날짜 텍스트 및 datePicker 설정
        editDateTextView.setText(String.format("%s월 %s일", month, day));
        datePickerDialog = new DatePickerDialog
                (DiaryEditActivity.this, dateListener, Integer.valueOf(year), Integer.valueOf(month) - 1, Integer.valueOf(day));

        SimpleDateFormat hourFormat = new SimpleDateFormat("hh", Locale.getDefault());
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.getDefault());
        SimpleDateFormat meridiemFormat = new SimpleDateFormat("aa", Locale.ENGLISH);

        String hour = hourFormat.format(dateAndTime);
        String minute = minuteFormat.format(dateAndTime);
        String meridiem = meridiemFormat.format(dateAndTime);

        editTimeTextView.setText(String.format("%s:%s%s", hour, minute, meridiem));

        int applyMerdiemHour = Integer.valueOf(hour);

        if (meridiem.equals("PM")) {
            applyMerdiemHour = applyMerdiemHour + 12;
        }

        timePickerDialog = new TimePickerDialog(DiaryEditActivity.this, timeListener, applyMerdiemHour, Integer.valueOf(minute), false);

        updateHour = applyMerdiemHour;
        updateMinute = Integer.valueOf(minute);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == REQUEST_DIARY_PHOTO_SELECT && data != null) { // 사진 선택
                    selectedUri = data.getData();
                    if (selectedUri != null) {
                        editPhotoImageButton.setImageURI(null);
                        editPhotoImageButton.setImageURI(selectedUri);
                        isPhotoUpdate = true;
                    } else {
                        showToast(R.string.toast_cannot_retrieve_selected_image);
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_diary_edit_date:
                datePickerDialog.show();
                break;
            case R.id.tv_diary_edit_time:
                timePickerDialog.show();
                break;
            case R.id.ib_diary_edit_photo:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DiaryEditActivity.this);
                alertDialog.setTitle("선택하시오");
                String selectStr[] = {"사진 촬영", "사진 선택"};
                alertDialog.setItems(selectStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) { // 1.사진 촬영인 경우 CameraActivity로 이동
                            Intent photoCaptueIntent = new Intent(DiaryEditActivity.this, CameraActivity.class);
                            startActivity(photoCaptueIntent);
                        } else if (which == 1) { // 2.갤러리 선택인 경우, 갤러리로 이동 후 프리뷰로? 사진 선택 후 이미지 적용
                            Intent doStartEditPhotoActivityIntent = new Intent(DiaryEditActivity.this, EditPhotoActivity.class);
                            doStartEditPhotoActivityIntent.putExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO);
                            doStartEditPhotoActivityIntent.putExtra("startingPointActivity", TAG);
                            startActivityForResult(doStartEditPhotoActivityIntent, REQUEST_DIARY_PHOTO_SELECT);
                        }
                    }
                });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
                break;
        }
    }

    /**
     * 현재 설정한 날짜 및 시간을 가져오기
     *
     * @return Date 타입의 날짜와 시간
     */
    private Date getDateAndTime() {
        DatePicker datePicker = datePickerDialog.getDatePicker();
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, updateHour, updateMinute);

        return calendar.getTime();
    }

    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            editDateTextView.setText(String.format(Locale.getDefault(), "%d월 %d일", month + 1, dayOfMonth));
        }
    };

    private TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            view.setIs24HourView(false);
            updateHour = hourOfDay;
            updateMinute = minute;

            String meridiem;
            String minuteStr;

            if (hourOfDay > 12) {
                hourOfDay = hourOfDay - 12;
                meridiem = "PM";
            } else {
                meridiem = "AM";
            }

            if (minute < 10) {
                minuteStr = "0" + minute;
            } else {
                minuteStr = "" + minute;
            }

            editTimeTextView.setText(String.format(Locale.getDefault(), "%d:%s%s", hourOfDay, minuteStr, meridiem));
        }
    };

    /**
     * 다이어리 데이터 저장
     * diaryIdx가 NEW일 경우, Diary DB에 추가
     * NEW가 아닐 경우, Diary DB 업데이트
     */
    private void saveViewData() {
        if (diaryIdx.equals("NEW")) { // 추가
            String updateDescription = editDescriptionTextView.getText().toString();

            Date time = getDateAndTime();

            LocalDiaryManager.getInstance(DiaryEditActivity.this)
                    .insertDiary(DiaryEditActivity.this, testDiaryIndex++, time, time.getTime() + ".jpg", updateDescription);

            try {
                copyFileToDownloads(selectedUri, time.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else { // 업데이트
            updateDiary(diaryIdx);
        }
    }

    private void copyFileToDownloads(Uri croppedFileUri, long time) throws Exception {

        File yogiDiaryStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YogiDiary");

        if (!yogiDiaryStorageDir.exists()) {
            if (yogiDiaryStorageDir.mkdirs()) {
                Log.d(TAG, getString(R.string.text_mkdir_success));
            } else {
                Log.d(TAG, getString(R.string.text_mkdir_fail));
            }
        }

        String downloadsDirectoryPath = yogiDiaryStorageDir.getPath() + "/";
        String filename = String.format(Locale.getDefault(), "%d%s", time, ".jpg");

        File saveFile = new File(downloadsDirectoryPath, filename);

        FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(saveFile);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)));
        showToast(R.string.notification_image_saved);
    }

    private void updateDiary(String idx) {
        diaryDatabase.diaryDao().findDiaryById(idx)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Diary>() {

                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Diary diary) {
                        if (diary != null) {
                            Date time = getDateAndTime();
                            String updatedDescription = editDescriptionTextView.getText().toString();

                            if (isPhotoUpdate) {
                                LocalDiaryManager.getInstance(DiaryEditActivity.this)
                                        .updateDiary(DiaryEditActivity.this,
                                                diary, time, time.getTime() + ".jpg", updatedDescription);
                                try {
                                    copyFileToDownloads(selectedUri, time.getTime());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                LocalDiaryManager.getInstance(DiaryEditActivity.this)
                                        .updateDiary(DiaryEditActivity.this,
                                                diary, time, photoFileName, updatedDescription);
                            }

                        }
                    }
                }).isDisposed();
    }

    // callback 함수
    @Override
    public void onDiaryAdded() {
        showToast(R.string.text_diary_added);
        finish();
    }

    @Override
    public void onDiaryByIdFinded(Diary diary) {
        setDateAndTime(diary.getDate());
        photoFileName = diary.getImage();
        editPhotoImageButton.setImageURI(Uri.fromFile(new File(YOGIDIARY_PATH + photoFileName)));
        editDescriptionTextView.setText(diary.getDescription());
    }

    @Override
    public void onDiaryUpdated() {
        showToast(R.string.text_diary_updated);
        finish();
    }

    @Override
    public void onDiaryDeleted() {
        showToast(R.string.text_diary_deleted);
        finish();
    }
}
