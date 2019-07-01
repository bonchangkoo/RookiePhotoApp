package kr.co.yogiyo.rookiephotoapp.diary

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import android.net.Uri
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository

class DiaryEditViewModel (private val dairyRepository: DiaryRepository, application: Application) : AndroidViewModel(application) {

    val dateTextLabel: ObservableField<String> = ObservableField(String.format("%s월 %s일", 1, 1))
    val timeTextLabel: ObservableField<String> = ObservableField(String.format("%s:%s%s", 12, 0, "AM"))
    val editImageUri: ObservableField<Uri> = ObservableField()
    val descriptionTextLabel: ObservableField<String> = ObservableField("내용")
}