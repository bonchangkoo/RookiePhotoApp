package kr.co.yogiyo.rookiephotoapp.settings;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

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

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final String PREFERENCE_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";
    private static final String BACKUP_KEY = "backup";
    private static final String RESTORE_KEY = "restore";

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private Compressor imageCompressor;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private DiaryBackupRestore diaryBackupRestore = new DiaryBackupRestore();

    private LocalDiaryViewModel localDiaryViewModel;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private Context context;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
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
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Preference backupPreference = findPreference(BACKUP_KEY);
        Preference restorePreference = findPreference(RESTORE_KEY);
        backupPreference.setOnPreferenceClickListener(this);
        restorePreference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof LoginDialogPreference) {
            dialogFragment = new LoginPreferenceDialogFragmentCompat();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null && getFragmentManager() != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), PREFERENCE_DIALOG_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (firebaseAuth.getCurrentUser() == null) {
            Preference loginDialogPreference = findPreference("dialog");
            onDisplayPreferenceDialog(loginDialogPreference);
            Toast.makeText(getContext(), "fail", Toast.LENGTH_SHORT).show();

            return false;
        }

        switch (preference.getKey()) {
            case BACKUP_KEY:
                // TODO : 백업/복원 실행할 때 다른 작업 못하도록 또는 바로 클릭 못하도록 또는 화면 분할 (작업 진행도 표시 필요할 듯)
                // TODO : 리사이징한 사진 복구 어떻게 할지 고민
                // TODO : 없는 사진 업로드할 때 예외 발생 -> 예외 발생 시 백업 데이터 삭제 못하게 하기 (순서 : 1. 파일 확인, 2. 서버 파일 삭제, 3. 업로드)
                /* TODO : 없는 사진 업로드할 경우 처리
                 * 방법 1. 임시 이미지 파일(이미지, 아이콘 등)로 대체 -> 서버에 내용은 같고 이름만 다른 이미지 파일로 계속 저장됨
                 * 방법 2. 서버에 이미지 parameter 없이 업로드 가능한지 확인
                 *        가능하면 -> parameter 비어놓고 api 호출
                 *        불가능하면 -> 방법 1 사용
                 * */
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
                        .doOnComplete(() -> SettingsFragment.this.buildAlertDialog(context, "백업 성공").create().show())
                        .subscribe(responseDiariesBody -> Log.d(TAG, responseDiariesBody.string()))
                );

                return true;
            case RESTORE_KEY:
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
                            boolean writtenToDisk = SettingsFragment.this.writeResponseBodyToDisk(imageFileName, pair.second);

                            return writtenToDisk;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(() -> SettingsFragment.this.buildAlertDialog(context, "복원 성공").create().show())
                        .subscribe(writtenToDisk -> Log.i(TAG, "success = " + writtenToDisk))
                );

                return true;
            default:
                return false;
        }
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

    private AlertDialog.Builder buildAlertDialog(Context context, String message) {
        return new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .setNegativeButton(null, null);
    }

}


