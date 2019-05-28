package kr.co.yogiyo.rookiephotoapp.diary.db;

import java.util.List;

public interface DiaryDatabaseCallback {

    void onDiariesBetweenDatesFinded(List<Diary> diaries);
}
