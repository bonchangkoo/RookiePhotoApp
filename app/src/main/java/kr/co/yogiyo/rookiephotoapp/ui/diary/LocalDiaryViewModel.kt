package kr.co.yogiyo.rookiephotoapp.ui.diary

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

import java.util.Date

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kr.co.yogiyo.rookiephotoapp.data.model.Diary
import kr.co.yogiyo.rookiephotoapp.data.source.local.DiaryDatabase

class LocalDiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val diaryDatabase = DiaryDatabase.getDatabase(application)

    fun insertDiary(date: Date, image: String?, description: String?): Completable {
        return Completable.fromAction {
            val diary = Diary(date, image, description)
            diaryDatabase?.diaryDao()?.insertDiary(diary)
        }
    }

    fun insertDiary(idx: Int, date: Date, image: String?, description: String?): Completable {
        return Completable.fromAction {
            val diary = Diary(idx, date, image, description)
            diaryDatabase?.diaryDao()?.insertDiary(diary)
        }
    }

    fun findDiaryById(diaryId: Int): Single<Diary>? {
        return diaryDatabase?.diaryDao()?.findDiaryById(diaryId)
    }

    fun updateDiary(diary: Diary, date: Date, image: String?, description: String?): Completable {
        diary.date = date
        diary.image = image
        diary.description = description

        return Completable.fromAction { diaryDatabase?.diaryDao()?.updateDiary(diary) }
    }

    fun findDiaries(): Single<List<Diary>> {
        return diaryDatabase?.diaryDao()?.findDiaries() ?: Single.just(arrayListOf())
    }

    fun deleteDiary(diary: Diary): Completable {
        return Completable.fromAction { diaryDatabase?.diaryDao()?.deleteDiary(diary) }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
