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

import com.google.firebase.auth.FirebaseAuth;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryViewModel;
import kr.co.yogiyo.rookiephotoapp.settings.sync.DiaryBackupRestore;
import okhttp3.ResponseBody;

public class BackupRestoreDialogFragment extends PreferenceDialogFragmentCompat implements View.OnClickListener {

    private static final String TAG = BackupRestoreDialogFragment.class.getSimpleName();

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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

        String preferenceKey = getArguments().getString(SettingsFragment.PREFERENCE_KEY);
        switch (preferenceKey) {
            case SettingsFragment.BACKUP_DIALOG_KEY:
                askBackupRestoreText.setText(getString(R.string.text_ask_backup));
                break;
            case SettingsFragment.RESTORE_DIALOG_KEY:
                askBackupRestoreText.setText(getString(R.string.text_ask_restore));
                break;
        }

        cancelText.setOnClickListener(this);
        okText.setOnClickListener(this);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        String preferenceKey = getArguments().getString(SettingsFragment.PREFERENCE_KEY);
        switch (preferenceKey) {
            case SettingsFragment.BACKUP_DIALOG_KEY:
                builder.setTitle(getString(R.string.text_do_backup));
                break;
            case SettingsFragment.RESTORE_DIALOG_KEY:
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
    // TODO : 취소할 때 로그인/회원가입 요청 취소할 수 있는지 조사
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
                String preferenceKey = getArguments().getString(SettingsFragment.PREFERENCE_KEY);
                setLoadingDialog();
                switch (preferenceKey) {
                    case SettingsFragment.BACKUP_DIALOG_KEY:
                        executeBackup();
                        break;
                    case SettingsFragment.RESTORE_DIALOG_KEY:
                        executeRestore();
                        break;
                }
                break;
        }
    }

    // TODO : 백업/복원 실행할 때 다른 작업 못하도록 또는 바로 클릭 못하도록 또는 화면 분할 (작업 진행도 표시 필요할 듯)
    // TODO : 리사이징한 사진 복구 어떻게 할지 고민
    // TODO : 없는 사진 업로드할 때 예외 발생 -> 예외 발생 시 백업 데이터 삭제 못하게 하기 (순서 : 1. 파일 확인, 2. 서버 파일 삭제, 3. 업로드)
    /* TODO : 없는 사진 업로드할 경우 처리
     * 방법 1. 임시 이미지 파일(이미지, 아이콘 등)로 대체 -> 서버에 내용은 같고 이름만 다른 이미지 파일로 계속 저장됨
     * 방법 2. 서버에 이미지 parameter 없이 업로드 가능한지 확인
     *        가능하면 -> parameter 비어놓고 api 호출
     *        불가능하면 -> 방법 1 사용
     * */
    private void executeBackup() {
        compositeDisposable.add(diaryBackupRestore.executePostClearDiary(firebaseAuth.getCurrentUser())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap((Function<ResponseBody, Publisher<List<Diary>>>) responseBody ->
                        localDiaryViewModel.findDiaries().toFlowable())
                .flatMapIterable(diaries -> diaries)
                .flatMap((Function<Diary, Publisher<ResponseBody>>) diary -> {
                    File imageFile = new File(BaseActivity.YOGIDIARY_PATH, diary.getImage());
                    imageCompressor.setDestinationDirectoryPath(
                            BaseActivity.YOGIDIARY_PATH.getAbsolutePath() + File.separator + "compressed")
                            .compressToFile(imageFile);

                    return diaryBackupRestore.executePostDiary(firebaseAuth.getCurrentUser(), diary);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    BackupRestoreDialogFragment.this.getDialog().dismiss();
                    ((SettingsActivity) context).buildAlertDialog(context, "백업 성공").create().show();
                })
                .subscribe(responseDiariesBody -> {
                    ((SettingsActivity) context).hideLoading();
                    Log.d(TAG, responseDiariesBody.string());
                })
        );
    }

    private void executeRestore() {
        compositeDisposable.add(diaryBackupRestore.executeGetDiaries(firebaseAuth.getCurrentUser())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMapIterable(restoredDiaries -> restoredDiaries)
                .map(restoredDiary -> {
                    Date date = simpleDateFormat.parse(restoredDiary.getDate());
                    String image = restoredDiary.getImage().substring(restoredDiary.getImage().lastIndexOf(File.separator) + 1);
                    localDiaryViewModel.insertDiary(restoredDiary.getDiaryId(), date, image, restoredDiary.getDescription())
                            .subscribe();

                    return restoredDiary;
                })
                .flatMap((Function<RestoredDiary, Publisher<Pair<String, ResponseBody>>>) restoredDiary ->
                        diaryBackupRestore.executeGetImage(restoredDiary.getImage().substring(1)))
                .map(pair -> {
                    String imageFileName = pair.first.substring(pair.first.lastIndexOf(File.separator) + 1);
                    boolean writtenToDisk = BackupRestoreDialogFragment.this.writeResponseBodyToDisk(imageFileName, pair.second);

                    return writtenToDisk;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    BackupRestoreDialogFragment.this.getDialog().dismiss();
                    ((SettingsActivity) context).buildAlertDialog(context, "복원 성공").create().show();
                })
                .subscribe(writtenToDisk -> {
                    ((SettingsActivity) context).hideLoading();
                    Log.d(TAG, "success = " + writtenToDisk);
                })
        );
    }

    private boolean writeResponseBodyToDisk(String imageFileName, ResponseBody body) {
        try {
            File restoredImageFile = new File(BaseActivity.YOGIDIARY_PATH, imageFileName);

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