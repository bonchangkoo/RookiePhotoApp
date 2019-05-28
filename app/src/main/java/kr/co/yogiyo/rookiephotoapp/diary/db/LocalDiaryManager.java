package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.content.Context;

import java.util.List;
import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.BaseActivity;
import kr.co.yogiyo.rookiephotoapp.R;

public class LocalDiaryManager extends BaseActivity {

    private Context context;
    private static LocalDiaryManager INSTANCE;
    private DiaryDatabase db;

    public static LocalDiaryManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LocalDiaryManager(context);
        }
        return INSTANCE;
    }

    public LocalDiaryManager(Context context) {
        this.context = context;

        if (db == null) {
            db = DiaryDatabase.getDatabase(context);
        }

    }

    public void insertDiary(final DiaryDatabaseCallback databaseCallback, final Date date, final String image, final String description) {
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                Diary diary = new Diary(date, image, description);
                db.diaryDao().insertDiary(diary);
            }
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // Do nothing
                    }

                    @Override
                    public void onComplete() {
                        databaseCallback.onDiaryAdded();
                    }

                    @Override
                    public void onError(Throwable e) {
                        databaseCallback.onDiaryError(context.getString(R.string.text_cant_add_diary));
                    }
                });
    }

    public void findDiaryById(final DiaryDatabaseCallback databaseCallback, final int diaryId) {
        compositeDisposable.add(db.diaryDao().findDiaryById(diaryId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Diary>() {
                    @Override
                    public void accept(Diary diary) {
                        databaseCallback.onDiaryByIdFinded(diary);
                    }
                }));
    }
  
    // TODO : 날짜 순으로 정렬
    // TODO : rxjava 조사 필요, compositeDisposable.add(안에 코드 구현), 사용해제(중요)
    public void findDiariesBetweenDates(final DiaryDatabaseCallback diaryDatabaseCallback, Date from, Date to) {
        DiaryDatabase.getInstance(context)
                .diaryDao()
                .findDiariesBetweenDates(from, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Diary>>() {
                    @Override
                    public void accept(List<Diary> diaries) {
                        diaryDatabaseCallback.onDiariesBetweenDatesFinded(diaries);
                    }
                });
    }

    public void updateDiary(final DiaryDatabaseCallback databaseCallback, final Diary diary, final Date date, final String image, final String description) {
        diary.setDate(date);
        diary.setImage(image);
        diary.setDescription(description);

        Completable.fromAction(new Action() {
            @Override
            public void run() {
                db.diaryDao().updateDiary(diary);
            }
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //do noting
                    }

                    @Override
                    public void onComplete() {
                        databaseCallback.onDiaryUpdated();
                    }

                    @Override
                    public void onError(Throwable e) {
                        databaseCallback.onDiaryError(context.getString(R.string.text_cant_update_diary));
                    }
                });
    }

    public void deleteDiary(final DiaryDatabaseCallback databaseCallback, final Diary diary) {
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                db.diaryDao().deleteDiary(diary);
            }
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //do noting
                    }

                    @Override
                    public void onComplete() {
                        databaseCallback.onDiaryDeleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        databaseCallback.onDiaryError(context.getString(R.string.text_cant_delete_diary));
                    }
                });
    }
}
