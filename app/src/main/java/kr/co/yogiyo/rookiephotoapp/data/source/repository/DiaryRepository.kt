package kr.co.yogiyo.rookiephotoapp.data.source.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kr.co.yogiyo.rookiephotoapp.data.model.Diary
import kr.co.yogiyo.rookiephotoapp.data.source.local.DiaryDao
import java.util.*

class DiaryRepository private constructor(private val diaryDao: DiaryDao) {

    fun insertDiary(date: Date, image: String, description: String): Completable {
        return Completable.fromAction {
            val diary = Diary(date, image, description)
            diaryDao.insertDiary(diary)
        }
    }

    fun findDiaryById(diaryId: Int): Single<Diary> {
        return diaryDao.findDiaryById(diaryId)
    }

    fun findDiariesBetweenDates(from: Date, to: Date): Flowable<List<Diary>> {
        return diaryDao.findDiariesBetweenDates(from, to)
    }


    fun updateDiary(diary: Diary, date: Date, image: String, description: String): Completable {
        diary.date = date
        diary.image = image
        diary.description = description

        return Completable.fromAction { diaryDao.updateDiary(diary) }
    }

    fun deleteDiary(diary: Diary): Completable {
        return Completable.fromAction { diaryDao.deleteDiary(diary) }
    }

    companion object {
        @Volatile
        private var instance: DiaryRepository? = null

        fun getInstance(diaryDao: DiaryDao) =
                instance
                        ?: synchronized(this) {
                    instance
                            ?: DiaryRepository(diaryDao).also { instance = it }
                }
    }
}