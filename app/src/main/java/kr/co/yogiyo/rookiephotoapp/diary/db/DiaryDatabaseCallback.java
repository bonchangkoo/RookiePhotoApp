package kr.co.yogiyo.rookiephotoapp.diary.db;

import java.util.List;

public interface DiaryDatabaseCallback {

    void onDiaryAdded();

    void onDiaryByIdFinded(Diary diary);

    void onDiariesBetweenDatesFinded(List<Diary> diaries);

    void onDiaryUpdated();

    void onDiaryDeleted();

    void onDiaryError(String errorMessage);

}
