package kr.co.yogiyo.rookiephotoapp.diary

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository

class DiaryDetailViewModelFactory
(
        private val diaryRepository: DiaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DiaryDetailViewModel(diaryRepository, Application()) as T
    }

}