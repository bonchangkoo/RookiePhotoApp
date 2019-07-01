package kr.co.yogiyo.rookiephotoapp.diary

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository

class DiaryEditViewModelFactory (
        private val diaryRepository: DiaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DiaryEditViewModel(diaryRepository, Application()) as T
    }

}