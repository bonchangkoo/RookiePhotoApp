package kr.co.yogiyo.rookiephotoapp.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.Constants;
import kr.co.yogiyo.rookiephotoapp.GlobalApplication;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryViewModel;
import kr.co.yogiyo.rookiephotoapp.settings.sync.DiaryBackupRestore;
import kr.co.yogiyo.rookiephotoapp.settings.sync.RestoredDiary;
import okhttp3.ResponseBody;

public class BackupRestoreDialogFragment extends PreferenceDialogFragmentCompat implements DialogInterface.OnShowListener {

    private static final String TAG = BackupRestoreDialogFragment.class.getSimpleName();
    private final String NO_DATA = "no_data";
    private final String NO_BACKUP_HISTORY = "Expected BEGIN_ARRAY but was BEGIN_OBJECT";
    private final String FAILED_TO_CONNECT = "Failed to connect";
    private final String ERROR_MESSAGE = "error_message";

    private SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

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

        String preferenceKey = getArguments().getString(SettingsActivity.PREFERENCE_KEY);
        switch (preferenceKey) {
            case SettingsActivity.BACKUP_DIALOG_KEY:
                askBackupRestoreText.setText(getString(R.string.text_ask_backup));
                okText.setOnClickListener(v -> {
                    setLoadingDialog();
                    executeBackup(GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser());
                });
                break;
            case SettingsActivity.RESTORE_DIALOG_KEY:
                askBackupRestoreText.setText(getString(R.string.text_ask_restore));
                okText.setOnClickListener(v -> {
                    setLoadingDialog();
                    executeRestore(GlobalApplication.getGlobalApplicationContext().getFirebaseAuth().getCurrentUser());
                });
                break;
        }
        cancelText.setOnClickListener(v -> dismiss());
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        String preferenceKey = getArguments().getString(SettingsActivity.PREFERENCE_KEY);
        switch (preferenceKey) {
            case SettingsActivity.BACKUP_DIALOG_KEY:
                builder.setTitle(getString(R.string.text_do_backup));
                break;
            case SettingsActivity.RESTORE_DIALOG_KEY:
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

    // TODO : 백업/복원 실행할 때 다른 작업 못하도록 또는 바로 클릭 못하도록 또는 화면 분할 (작업 진행도 표시 필요할 듯)
    // TODO : 리사이징한 사진 복구 어떻게 할지 고민
    private void executeBackup(FirebaseUser currentUser) {
        compositeDisposable.add(
                localDiaryViewModel.findDiaries().toFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(diaries -> {
                            if (diaries.isEmpty()) {
                                throw new Exception(NO_DATA);
                            }

                            compositeDisposable.add(
                                    diaryBackupRestore.executePostClearDiary(currentUser)
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
                                            Constants.YOGIDIARY_PATH.getAbsolutePath() + File.separator + DiaryBackupRestore.COMPRESSED_FOLDER_NAME)
                                            .compressToFile(imageFile);
                                }
                            }

                            return diaryBackupRestore.executePostDiary(currentUser, diary);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseDiariesBody -> {
                            Log.d(TAG, "body string " + responseDiariesBody.string());
                            if (responseDiariesBody.string().contains(ERROR_MESSAGE)) {
                                throw new Exception();
                            }
                        }, throwable -> {
                            ((SettingsActivity) context).hideLoading();
                            BackupRestoreDialogFragment.this.getDialog().dismiss();

                            if (NO_DATA.equals(throwable.getMessage())) {
                                ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_no_backup_data),
                                        getString(R.string.text_confirm), null, null, this).show();
                            } else if (throwable.getMessage().contains(FAILED_TO_CONNECT)) {
                                ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_network_error),
                                        getString(R.string.text_confirm), null, null, this).show();
                            } else {
                                ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_backup_fail),
                                        getString(R.string.text_confirm), null, null, this).show();
                            }

                            Log.d(TAG, throwable.getMessage());
                        }, () -> {
                            ((SettingsActivity) context).hideLoading();
                            BackupRestoreDialogFragment.this.getDialog().dismiss();

                            ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_backup_success),
                                    getString(R.string.text_confirm), null, null, this).show();
                        })
        );
    }

    private void executeRestore(FirebaseUser currentUser) {
        compositeDisposable.add(
                diaryBackupRestore.executeGetDiaries(currentUser)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMapIterable(restoredDiaries -> {
                            if (restoredDiaries.isEmpty()) {
                                throw new Exception(NO_DATA);
                            }

                            return restoredDiaries;
                        })
                        .map(restoredDiary -> {
                            Date date = serverDateFormat.parse(restoredDiary.getDatetime());

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
                        .subscribe(writtenToDisk -> Log.d(TAG, "success = " + writtenToDisk),
                                throwable -> {
                                    ((SettingsActivity) context).hideLoading();
                                    BackupRestoreDialogFragment.this.getDialog().dismiss();

                                    if (throwable.getMessage().equals(NO_DATA) || throwable.getMessage().contains(NO_BACKUP_HISTORY)) {
                                        ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_no_restore_data),
                                                getString(R.string.text_confirm), null, null, this).show();
                                    } else if (throwable.getMessage().contains(FAILED_TO_CONNECT)) {
                                        ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_network_error),
                                                getString(R.string.text_confirm), null, null, this).show();
                                    } else {
                                        ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_restore_fail),
                                                getString(R.string.text_confirm), null, null, this).show();
                                    }

                                    Log.d(TAG, throwable.getMessage());
                                }, () -> {
                                    ((SettingsActivity) context).hideLoading();
                                    BackupRestoreDialogFragment.this.getDialog().dismiss();

                                    ((SettingsActivity) context).createAlertDialog(context, null, getString(R.string.text_restore_success),
                                            getString(R.string.text_confirm), null, null, this).show();
                                })
        );
    }

    private boolean writeResponseBodyToDisk(String imageFileName, ResponseBody body) {
        if (!Constants.YOGIDIARY_PATH.exists()) {
            if (!Constants.YOGIDIARY_PATH.mkdirs()) {
                return false;
            }
        }

        File restoredImageFile = new File(Constants.YOGIDIARY_PATH, imageFileName);

        try (InputStream inputStream = body.byteStream();
             OutputStream outputStream = new FileOutputStream(restoredImageFile)) {
            byte[] fileReader = new byte[4096];

            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            int read = inputStream.read(fileReader);
            while (read != -1) {
                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;
                Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);

                read = inputStream.read(fileReader);
            }

            outputStream.flush();

            context.sendBroadcast((new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(restoredImageFile))));

            return true;
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

    @Override
    public void onShow(DialogInterface dialog) {
        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.color_FF0000));
    }
}