package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface DiaryDao {

    /**
     * Select all diaries from the diaries table.
     *
     * @return all diaries.
     */
    @Query("SELECT * FROM diaries")
    List<Diary> findDiaries();

    /**
     * Select a diary by id.
     *
     * @param diaryId the diary id.
     * @return the diary with diaryId.
     */
    @Query("SELECT * FROM diaries WHERE id = :diaryId")
    Diary findDiaryById(String diaryId);

    /**
     * Select a diary by id.
     *
     * @param from start of date range.
     * @param to   end of date range.
     * @return the diaries between from and to.
     */
    @Query("SELECT * FROM diaries WHERE date BETWEEN :from AND :to")
    Maybe<List<Diary>> findDiariesCreatedBetweenDates(Date from, Date to);

    /**
     * Insert a diary in the database. If the diary already exists, replace it.
     *
     * @param diary the diary to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDiary(Diary diary);
}
