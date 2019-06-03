package kr.co.yogiyo.rookiephotoapp.diary.db;

import java.util.List;

public interface DiaryDatabaseCallback {

    void onDiaryAdded();

    void onDiaryByIdFinded(Diary diary);

    void onDiaryUpdated();

    void onDiaryDeleted();

    void onDiariesFinded(List<Diary> diaries);
}