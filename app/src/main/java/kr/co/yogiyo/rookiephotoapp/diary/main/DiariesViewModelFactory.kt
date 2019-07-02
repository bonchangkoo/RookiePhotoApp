package kr.co.yogiyo.rookiephotoapp.diary.main

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository

class DiariesViewModelFactory(private val diaryRepository: DiaryRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(DiariesViewModel::class.java) -> DiariesViewModel(diaryRepository, Application())
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T

    companion object {
        @Volatile
        private var INSTANCE: DiariesViewModelFactory? = null

        fun getInstance(application: Application) =
                INSTANCE ?: synchronized(DiariesViewModelFactory::class.java) {
                    INSTANCE ?: DiariesViewModelFactory(
                            DiaryRepository.getInstance(DiaryDatabase.getDatabase(application)!!.diaryDao())
                    )
                }
    }
}