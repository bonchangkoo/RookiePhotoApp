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

    /**
     * Insert a diary in the database. If the diary already exists, replace it.
     *
     * @param diary the diary to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDiary(Diary diary);

    /**
     * Select a diary by id.
     *
     * @param diaryId the diary id.
     * @return the diary with diaryId.
     */
    @Query("SELECT * FROM diaries WHERE idx = :diaryId")
    Maybe<Diary> findDiaryById(String diaryId);

    /**
     * Update a diary
     *
     * @param diary the diary
     */
    @Update
    void updateDiary(Diary diary);

    /**
     * Delete a diary by id.
     *
     * @param diary the diary
     */

    @Delete
    void deleteDiary(Diary diary);

}
