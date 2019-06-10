package kr.co.yogiyo.rookiephotoapp.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.Constants;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryViewModel;
import kr.co.yogiyo.rookiephotoapp.settings.sync.DiaryBackupRestore;
import kr.co.yogiyo.rookiephotoapp.settings.sync.RestoredDiary;
import okhttp3.ResponseBody;

public class BackupRestoreDialogFragment extends PreferenceDialogFragmentCompat implements View.OnClickListener {

    private static final String TAG = BackupRestoreDialogFragment.class.getSimpleName();

    private Compressor imageCompressor;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private DiaryBackupRestore diaryBackupRestore = new DiaryBackupRestore();

    private LocalDiaryViewModel localDiaryViewModel;

    private TextView askBackupRestoreText;
    private TextView cancelText;
    private TextView okText;

    private Context context;

    public static BackupRestoreDialogFragment newInstance() {
        return new BackupRestoreDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localDiaryViewModel = ViewModelProviders.of(this).get(LocalDiaryViewModel.class);
        imageCompressor = new Compressor(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        RelativeLayout backupRestoreDialogRelative = view.findViewById(R.id.relative_backup_restore_dialog);
        askBackupRestoreText = view.findViewById(R.id.text_ask_backup_restore);
        cancelText = view.findViewById(R.id.text_cancel);
        okText = view.findViewById(R.id.text_ok);
        ((SettingsActivity) context).addProgressBarInto(backupRestoreDialogRelative);

        String preferenceKey = getArguments().getString(Constants.PREFERENCE_KEY);
        switch (preferenceKey) {
            case Constants.BACKUP_DIALOG_KEY:
                askBackupRestoreText.setText(getString(R.string.text_ask_backup));
                break;
            case Constants.RESTORE_DIALOG_KEY:
                askBackupRestoreText.setText(getString(R.string.text_ask_restore));
                break;
        }

        cancelText.setOnClickListener(this);
        okText.setOnClickListener(this);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        String preferenceKey = getArguments().getString(Constants.PREFERENCE_KEY);
        switch (preferenceKey) {
            case Constants.BACKUP_DIALOG_KEY:
                builder.setTitle(getString(R.string.text_do_backup));
                break;
            case Constants.RESTORE_DIALOG_KEY:
                builder.setTitle(getString(R.string.text_do_restore));
                break;
        }
        builder.setPositiveButton(null, null)
                .setNegativeButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // Do nothing
    }

    // TODO : 콜백을 RxJava로 바꿀 수 있을지 고민하기
    // TODO : 구글 로그인 실패 A non-recoverable sign in failure occurred (status code: 12500)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_cancel:
                dismiss();
                break;
            case R.id.text_ok:
                if (getArguments() == null) {
                    dismiss();
                }
                setLoadingDialog();
                String preferenceKey = getArguments().getString(Constants.PREFERENCE_KEY);
                switch (preferenceKey) {
                    case Constants.BACKUP_DIALOG_KEY:
                        executeBackup();
                        break;
                    case Constants.RESTORE_DIALOG_KEY:
                        executeRestore();
                        break;
                }
                break;
        }
    }

    // TODO : 백업/복원 실행할 때 다른 작업 못하도록 또는 바로 클릭 못하도록 또는 화면 분할 (작업 진행도 표시 필요할 듯)
    // TODO : 리사이징한 사진 복구 어떻게 할지 고민
    private void executeBackup() {
        compositeDisposable.add(localDiaryViewModel.findDiaries().toFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(diaries -> {
                    if (diaries.isEmpty()) {
                        throw new Exception(Constants.NO_DATA);
                    }

                    compositeDisposable.add(diaryBackupRestore.executePostClearDiary(Constants.firebaseAuth.getCurrentUser())
                            .subscribe(responseBody -> {
                                // Do nothing
                            }, throwable -> {
                                // Do nothing
                            })
                    );

                    return diaries;
                })
                .flatMapIterable(diaries -> diaries)
                .flatMap((Function<Diary, Publisher<ResponseBody>>) diary -> {
                    if (diary.getImage() != null) {
                        File imageFile = new File(Constants.YOGIDIARY_PATH, diary.getImage());
                        if (imageFile.isFile()) {
                            imageCompressor.setDestinationDirectoryPath(
                                    Constants.YOGIDIARY_PATH.getAbsolutePath() + File.separator + Constants.COMPRESSED_FOLDER_NAME)
                                    .compressToFile(imageFile);
                        }
                    }

                    return diaryBackupRestore.executePostDiary(Constants.firebaseAuth.getCurrentUser(), diary);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseDiariesBody -> {
                    Log.d(TAG, "body string " + responseDiariesBody.string());
                    if (responseDiariesBody.string().contains(Constants.ERROR_MESSAGE)) {
                        throw new Exception();
                    }
                }, throwable -> {
                    ((SettingsActivity) context).hideLoading();
                    BackupRestoreDialogFragment.this.getDialog().dismiss();

                    if (Constants.NO_DATA.equals(throwable.getMessage())) {
                        ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_no_backup_data)).create().show();
                    } else if (throwable.getMessage().contains(Constants.FAILED_TO_CONNECT)) {
                        ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_network_error)).create().show();
                    } else {
                        ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_backup_fail)).create().show();
                    }

                    Log.d(TAG, throwable.getMessage());
                }, () -> {
                    ((SettingsActivity) context).hideLoading();
                    BackupRestoreDialogFragment.this.getDialog().dismiss();
                    ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_backup_success)).create().show();
                })
        );
    }

    private void executeRestore() {
        compositeDisposable.add(diaryBackupRestore.executeGetDiaries(Constants.firebaseAuth.getCurrentUser())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMapIterable(restoredDiaries -> {
                    if (restoredDiaries.isEmpty()) {
                        throw new Exception(Constants.NO_DATA);
                    }

                    return restoredDiaries;
                })
                .map(restoredDiary -> {
                    Date date = Constants.serverDateFormat.parse(restoredDiary.getDatetime());

                    String image = null;
                    if (restoredDiary.getImage() != null) {
                        image = restoredDiary.getImage().substring(restoredDiary.getImage().lastIndexOf(File.separator) + 1);
                    }

                    localDiaryViewModel.insertDiary(restoredDiary.getDiaryId(), date, image, restoredDiary.getDescription())
                            .subscribe();

                    return restoredDiary;
                })
                .filter(restoredDiary -> restoredDiary.getImage() != null)
                .flatMap((Function<RestoredDiary, Publisher<Pair<String, ResponseBody>>>) restoredDiary ->
                        diaryBackupRestore.executeGetImage(restoredDiary.getImage().substring(restoredDiary.getImage().indexOf(File.separator) + 1)))
                .map(pair -> {
                    String imageFileName = pair.first.substring(pair.first.lastIndexOf(File.separator) + 1);
                    boolean writtenToDisk = BackupRestoreDialogFragment.this.writeResponseBodyToDisk(imageFileName, pair.second);

                    return writtenToDisk;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(writtenToDisk -> Log.d(TAG, "success = " + writtenToDisk), throwable -> {
                    ((SettingsActivity) context).hideLoading();
                    BackupRestoreDialogFragment.this.getDialog().dismiss();

                    if (throwable.getMessage().equals(Constants.NO_DATA)) {
                        ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_no_restore_data)).create().show();
                    } else if (throwable.getMessage().contains(Constants.FAILED_TO_CONNECT)) {
                        ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_network_error)).create().show();
                    } else {
                        ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_restore_fail)).create().show();
                    }

                    Log.d(TAG, throwable.getMessage());
                }, () -> {
                    ((SettingsActivity) context).hideLoading();
                    BackupRestoreDialogFragment.this.getDialog().dismiss();
                    ((SettingsActivity) context).buildAlertDialog(context, getString(R.string.text_restore_success)).create().show();
                })
        );
    }

    private boolean writeResponseBodyToDisk(String imageFileName, ResponseBody body) {
        try {
            File restoredImageFile = new File(Constants.YOGIDIARY_PATH, imageFileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(restoredImageFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                context.sendBroadcast((new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(restoredImageFile))));

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void setLoadingDialog() {
        ((SettingsActivity) context).showLoading();
        askBackupRestoreText.setText(getString(R.string.text_please_wait));
        cancelText.setVisibility(View.GONE);
        okText.setVisibility(View.GONE);
    }
}