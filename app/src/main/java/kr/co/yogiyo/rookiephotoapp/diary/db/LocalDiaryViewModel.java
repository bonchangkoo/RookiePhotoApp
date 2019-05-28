package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.R;

public class LocalDiaryViewModel extends AndroidViewModel {

    private CompositeDisposable compositeDisposable;

    private DiaryDatabase diaryDatabase;

    public LocalDiaryViewModel(@NonNull Application application) {
        super(application);
        diaryDatabase = DiaryDatabase.getDatabase(application);
        compositeDisposable = new CompositeDisposable();
    }

    public void insertDiary(final DiaryDatabaseCallback databaseCallback, final Date date, final String image, final String description) {
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                Diary diary = new Diary(date, image, description);
                diaryDatabase.diaryDao().insertDiary(diary);
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
                        databaseCallback.onDiaryError(getApplication().getString(R.string.text_cant_add_diary));
                    }
                });
    }

    public void findDiaryById(final DiaryDatabaseCallback databaseCallback, final int diaryId) {
        compositeDisposable.add(diaryDatabase.diaryDao().findDiaryById(diaryId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Diary>() {
                    @Override
                    public void accept(Diary diary) {
                        databaseCallback.onDiaryByIdFinded(diary);
                    }
                }));
    }

    public Flowable<Diary> findDiaryById(final int diaryId) {
        return diaryDatabase.diaryDao().findDiaryById(diaryId);
    }

    // TODO : 날짜 순으로 정렬
    // TODO : rxjava 조사 필요, compositeDisposable.add(안에 코드 구현), 사용해제(중요)
    public void findDiariesBetweenDates(final DiaryDatabaseCallback diaryDatabaseCallback, Date from, Date to) {
        compositeDisposable.add(diaryDatabase.diaryDao()
                .findDiariesBetweenDates(from, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Diary>>() {
                    @Override
                    public void accept(List<Diary> diaries) {
                        diaryDatabaseCallback.onDiariesBetweenDatesFinded(diaries);
                    }
                }));
    }

    public void updateDiary(final DiaryDatabaseCallback databaseCallback, final Diary diary, final Date date, final String image, final String description) {
        diary.setDate(date);
        diary.setImage(image);
        diary.setDescription(description);

        Completable.fromAction(new Action() {
            @Override
            public void run() {
                diaryDatabase.diaryDao().updateDiary(diary);
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
                        databaseCallback.onDiaryUpdated();
                    }

                    @Override
                    public void onError(Throwable e) {
                        databaseCallback.onDiaryError(getApplication().getString(R.string.text_cant_update_diary));
                    }
                });
    }

    public void deleteDiary(final DiaryDatabaseCallback databaseCallback, final Diary diary) {
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                diaryDatabase.diaryDao().deleteDiary(diary);
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
                        databaseCallback.onDiaryDeleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        databaseCallback.onDiaryError(getApplication().getString(R.string.text_cant_delete_diary));
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        compositeDisposable.dispose();
    }
}
