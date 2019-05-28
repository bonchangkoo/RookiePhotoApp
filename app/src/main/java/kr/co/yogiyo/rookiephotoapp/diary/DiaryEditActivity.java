package kr.co.yogiyo.rookiephotoapp.diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import kr.co.yogiyo.rookiephotoapp.Constants;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabaseCallback;
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryManager;
import kr.co.yogiyo.rookiephotoapp.edit.EditPhotoActivity;

public class DiaryEditActivity extends BaseActivity implements View.OnClickListener, DiaryDatabaseCallback {

    private final static String TAG = DiaryEditActivity.class.getSimpleName();

    private final static int DIARY_ADD = -1;

    private DiaryDatabase diaryDatabase;

    private ImageButton backImageButton;
    private TextView toolbarNameTextView;
    private ImageButton diarySaveImageButton;

    private TextView editDateTextView;
    private TextView editTimeTextView;
    private ImageButton editPhotoImageButton;
    private TextView editDescriptionTextView;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    private static int diaryIdx;

    private Uri selectedUri;
    private int updateHour;
    private int updateMinute;

    private String photoFileName;
    private boolean isPhotoUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        if (diaryDatabase == null) {
            diaryDatabase = DiaryDatabase.getDatabase(this);
        }

        diaryIdx = getIntent().getIntExtra("DIARY_IDX", -1);

        initView();
        setViewData(diaryIdx);
    }

    private void initView() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        backImageButton = findViewById(R.id.ib_back);
        toolbarNameTextView = findViewById(R.id.tv_subject);
        if (diaryIdx == DIARY_ADD) {
            toolbarNameTextView.setText(R.string.text_diary_add_title);
        }
        diarySaveImageButton = findViewById(R.id.ib_diary_save);

        backImageButton.setOnClickListener(this);
        diarySaveImageButton.setOnClickListener(this);

        editDateTextView = findViewById(R.id.tv_diary_edit_date);
        editTimeTextView = findViewById(R.id.tv_diary_edit_time);
        editPhotoImageButton = findViewById(R.id.ib_diary_edit_photo);
        editDescriptionTextView = findViewById(R.id.et_diary_edit_description);

        editDateTextView.setOnClickListener(this);
        editTimeTextView.setOnClickListener(this);
        editPhotoImageButton.setOnClickListener(this);
        editDescriptionTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                onBackPressed();
                break;
            case R.id.ib_diary_save:
                saveViewData();
                finish();
                break;
            case R.id.tv_diary_edit_date:
                datePickerDialog.show();
                break;
            case R.id.tv_diary_edit_time:
                timePickerDialog.show();
                break;
            case R.id.ib_diary_edit_photo:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DiaryEditActivity.this);
                alertDialog.setTitle("선택하시오");
                String[] selectStr = {"사진 촬영", "사진 선택"};
                alertDialog.setItems(selectStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent photoCaptureIntent = new Intent(DiaryEditActivity.this, CameraActivity.class);
                            startActivity(photoCaptureIntent);
                        } else if (which == 1) {
                            Intent doStartEditPhotoActivityIntent = new Intent(DiaryEditActivity.this, EditPhotoActivity.class);
                            doStartEditPhotoActivityIntent.putExtra(getString(R.string.edit_photo_category_number), EDIT_SELECTED_PHOTO);
                            doStartEditPhotoActivityIntent.putExtra(STARTING_POINT, TAG);
                            startActivityForResult(doStartEditPhotoActivityIntent, Constants.REQUEST_DIARY_PICK_GALLERY);
                        }
                    }
                });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
                break;
        }
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

    private void setViewData(int idx) {
        if (idx == DIARY_ADD) {
            Date currentTime = Calendar.getInstance().getTime();
            setDateAndTime(currentTime);
        } else {
            LocalDiaryManager.getInstance(this).findDiaryById(this, idx);
        }
    }

    private void setDateAndTime(Date dateAndTime) {

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("M", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

        String year = yearFormat.format(dateAndTime);
        String month = monthFormat.format(dateAndTime);
        String day = dayFormat.format(dateAndTime);

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
                if (requestCode == Constants.REQUEST_DIARY_PICK_GALLERY && data != null) {
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

    private void saveViewData() {
        if (diaryIdx == DIARY_ADD) {
            String updateDescription = editDescriptionTextView.getText().toString();

            Date time = getDateAndTime();

            LocalDiaryManager.getInstance(DiaryEditActivity.this)
                    .insertDiary(DiaryEditActivity.this, time, time.getTime() + ".jpg", updateDescription);

            try {
                copyFileToDownloads(selectedUri, time.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            updateDiary(diaryIdx);
        }
    }

    private void copyFileToDownloads(Uri croppedFileUri, long time) throws Exception {

        if (!YOGIDIARY_PATH.exists()) {
            if (YOGIDIARY_PATH.mkdirs()) {
                Log.d(TAG, getString(R.string.text_mkdir_success));
            } else {
                Log.d(TAG, getString(R.string.text_mkdir_fail));
            }
        }

        String downloadsDirectoryPath = YOGIDIARY_PATH.getPath() + "/";
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

    private void updateDiary(int idx) {

        compositeDisposable.add(diaryDatabase.diaryDao().findDiaryById(idx)
                .subscribeOn(Schedulers.single())
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
                }));
    }

    @Override
    public void onDiaryAdded() {
        showToast(R.string.text_diary_added);
        finish();
    }

    @Override
    public void onDiaryByIdFinded(Diary diary) {
        setDateAndTime(diary.getDate());
        photoFileName = diary.getImage();
        editPhotoImageButton.setImageURI(Uri.fromFile(new File(YOGIDIARY_PATH, photoFileName)));
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

    @Override
    public void onDiaryError(String errorMessage) {
        showToast(errorMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }
}
