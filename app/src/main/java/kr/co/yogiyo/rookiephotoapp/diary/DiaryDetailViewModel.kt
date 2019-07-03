package kr.co.yogiyo.rookiephotoapp.diary

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import android.net.Uri
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DiaryDetailViewModel(private val dairyRepository: DiaryRepository, application: Application) : AndroidViewModel(application) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    val dateTextLabel: ObservableField<String> = ObservableField(String.format("%s월 %s일", 1, 1))
    val timeTextLabel: ObservableField<String> = ObservableField(String.format("%s:%s%s", 12, 0, "AM"))
    val detailImageUri: ObservableField<Uri> = ObservableField()
    val descriptionTextLabel: ObservableField<String> = ObservableField("내용")

    lateinit var diaryDeleteComplete: () -> Unit
    lateinit var diaryDeleteError: () -> Unit

    fun loadViewData(diaryIndex: Int) {
        compositeDisposable.add(dairyRepository.findDiaryById(diaryIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { diary ->
                    setDateAndTime(diary.date!!)
                    detailImageUri.set(Uri.fromFile(File(Constants.FOONCARE_PATH, diary.image)))

                    descriptionTextLabel.set(diary.description)
                })
    }

    fun deleteDiary(idx: Int) {
        compositeDisposable.add(dairyRepository.findDiaryById(idx)
                .subscribeOn(Schedulers.single())
                .flatMapCompletable {
                    dairyRepository.deleteDiary(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    diaryDeleteComplete()
                }, {
                    diaryDeleteError()
                }))

    }
    

    private fun setDateAndTime(dateAndTime: Date) {

        val monthFormat = SimpleDateFormat("M", Locale.getDefault())
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

        val month = monthFormat.format(dateAndTime)
        val day = dayFormat.format(dateAndTime)

        dateTextLabel.set(String.format("%s월 %s일", month, day))

        val hourFormat = SimpleDateFormat("hh", Locale.getDefault())
        val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
        val meridiemFormat = SimpleDateFormat("aa", Locale.ENGLISH)

        val hour = hourFormat.format(dateAndTime)
        val minute = minuteFormat.format(dateAndTime)
        val meridiem = meridiemFormat.format(dateAndTime)

        timeTextLabel.set(String.format("%s:%s%s", hour, minute, meridiem))
    }

    override fun onCleared() {
        super.onCleared()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }
}