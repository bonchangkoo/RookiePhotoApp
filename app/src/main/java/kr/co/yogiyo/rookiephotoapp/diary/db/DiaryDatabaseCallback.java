package kr.co.yogiyo.rookiephotoapp.diary.db;

public interface DiaryDatabaseCallback {

    void onDiaryAdded();

    void onDiaryByIdFinded(Diary diary);

    void onDiaryUpdated();

    void onDiaryDeleted();


}
