package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocalDiaryManager {
    private static final String DB_NAME = "Diaries.db";
    private Context context;
    private static LocalDiaryManager _instance;
    private DiaryDatabase db;

    public static LocalDiaryManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new LocalDiaryManager(context);
        }
        return _instance;
    }

    public LocalDiaryManager(Context context) {
        this.context = context;
        db = Room.databaseBuilder(context, DiaryDatabase.class, DB_NAME).build();
    }

    public void insertDiary(final DiaryDatabaseCallback databaseCallback, final int idx, final Date date, final String image, final String description) {
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                Diary diary = new Diary(idx,date, image, description);
                db.diaryDao().insertDiary(diary);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                databaseCallback.onDiaryAdded();
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    public void findDiaryById(final DiaryDatabaseCallback databaseCallback, final String diaryId) {
        db.diaryDao().findDiaryById(diaryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Diary>() {
                    @Override
                    public void accept(Diary diary) {
                        databaseCallback.onDiaryByIdFinded(diary);
                    }
                }).isDisposed();
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
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                databaseCallback.onDiaryUpdated();
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    public void deleteDiary(final DiaryDatabaseCallback databaseCallback, final Diary diary) {
        Completable.fromAction(new Action() {
            @Override
            public void run() {
                db.diaryDao().deleteDiary(diary);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                databaseCallback.onDiaryDeleted();
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }


}
