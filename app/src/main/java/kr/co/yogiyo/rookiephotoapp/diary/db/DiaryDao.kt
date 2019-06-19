package kr.co.yogiyo.rookiephotoapp.diary.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

import java.util.Date

import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDiary(diary: Diary)

    @Query("SELECT * FROM diaries WHERE idx = :diaryId")
    fun findDiaryById(diaryId: Int): Single<Diary>

    @Update
    fun updateDiary(diary: Diary)

    @Delete
    fun deleteDiary(diary: Diary)

    @Query("SELECT * FROM diaries")
    fun findDiaries(): Single<List<Diary>>

    @Query("SELECT * FROM diaries WHERE date BETWEEN :from AND :to")
    fun findDiariesBetweenDates(from: Date, to: Date): Flowable<List<Diary>>
}
