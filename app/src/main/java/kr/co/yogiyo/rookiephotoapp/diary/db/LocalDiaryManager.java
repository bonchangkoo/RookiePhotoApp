package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.content.Context;

import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocalDiaryManager {
    private static LocalDiaryManager INSTANCE;

    private Context context;

    public LocalDiaryManager(Context context) {
        this.context = context;
    }

    public static LocalDiaryManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDiaryManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalDiaryManager(context);
                }
            }
        }
        return INSTANCE;
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
}