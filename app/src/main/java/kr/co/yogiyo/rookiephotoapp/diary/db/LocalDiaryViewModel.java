package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;

public class LocalDiaryViewModel extends AndroidViewModel {

    private CompositeDisposable compositeDisposable;
    private DiaryDatabase diaryDatabase;


    public LocalDiaryViewModel(@NonNull Application application) {
        super(application);
        diaryDatabase = DiaryDatabase.getDatabase(application);
        compositeDisposable = new CompositeDisposable();
    }

    public Completable insertDiary(final Date date, final String image, final String description) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Diary diary = new Diary(date, image, description);
                diaryDatabase.diaryDao().insertDiary(diary);
            }
        });
    }

    public Completable insertDiary(final int idx, final Date date, final String image, final String description) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Diary diary = new Diary(idx, date, image, description);
                diaryDatabase.diaryDao().insertDiary(diary);
            }
        });
    }

    public Single<List<Diary>> findDiaries() {
        return diaryDatabase.diaryDao().findDiaries();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}