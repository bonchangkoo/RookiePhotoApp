package kr.co.yogiyo.rookiephotoapp.diary.main

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase

class DiariesViewModelFactory(private val diariesRepository: DiariesRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(DiariesViewModel::class.java) -> DiariesViewModel(diariesRepository, Application())
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T

    companion object {
        @Volatile
        private var INSTANCE: DiariesViewModelFactory? = null

        fun getInstance(application: Application) =
                INSTANCE ?: synchronized(DiariesViewModelFactory::class.java) {
                    INSTANCE ?: DiariesViewModelFactory(
                            DiariesRepository.getInstance(DiaryDatabase.getDatabase(application)!!.diaryDao())
                    )
                }
    }
}