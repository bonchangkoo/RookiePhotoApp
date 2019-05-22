package kr.co.yogiyo.rookiephotoapp.diary.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface DiaryDao {

    @Query("SELECT * FROM diaries WHERE date BETWEEN :from AND :to")
    Maybe<List<Diary>> findDiariesBetweenDates(Date from, Date to);
}
