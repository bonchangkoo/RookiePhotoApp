package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import io.reactivex.Maybe;

@Dao
public interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDiary(Diary diary);

    @Query("SELECT * FROM diaries WHERE idx = :diaryId")
    Maybe<Diary> findDiaryById(String diaryId);

    @Update
    void updateDiary(Diary diary);

    @Delete
    void deleteDiary(Diary diary);

}
