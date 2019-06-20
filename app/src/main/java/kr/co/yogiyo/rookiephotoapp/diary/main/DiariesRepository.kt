package kr.co.yogiyo.rookiephotoapp.diary.main

import io.reactivex.Flowable
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDao
import java.util.Date

class DiariesRepository private constructor(private val diaryDao: DiaryDao) {

    fun findDiariesBetweenDates(from: Date, to: Date): Flowable<List<Diary>> {
        return diaryDao.findDiariesBetweenDates(from, to)
    }

    companion object {

        private var instance: DiariesRepository? = null

        fun getInstance(diaryDao: DiaryDao) =
                instance ?: synchronized(this) {
                    DiariesRepository(diaryDao).also { instance = it }
                }
    }
}