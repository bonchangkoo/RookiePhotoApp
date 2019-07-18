package kr.co.yogiyo.rookiephotoapp.diary

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository
import java.text.SimpleDateFormat
import java.util.*

class DiaryEditViewModel(private val diaryRepository: DiaryRepository, application: Application) : AndroidViewModel(application) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    val dateTextLabel: ObservableField<String> = ObservableField(String.format("%s월 %s일", 1, 1))
    val timeTextLabel: ObservableField<String> = ObservableField(String.format("%s:%s%s", 12, 0, "AM"))
    val descriptionTextLabel: ObservableField<String> = ObservableField("")

    private var yearOfDatePicker: Int = 2019
    private var monthOfDatePicker: Int = 1
    private var dayOfDatePicker: Int = 1
    private var updateHourOfTimePicker: Int = 0
    private var updateMinuteOfTimePicker: Int = 0
    private var loadImageFileNameForCheckUpdate: String? = null

    lateinit var loadDiaryImageComplete: (photoFileName: String) -> Unit
    lateinit var diaryInsertComplete: () -> Unit
    lateinit var diaryInsertError: () -> Unit
    lateinit var diaryUpdateComplete: () -> Unit
    lateinit var diaryUpdateError: () -> Unit
    lateinit var initDatePickerAndTimePickerListener: (year: Int, month: Int, day: Int, hour: Int, minute: Int) -> Unit

    fun initDateAndTime(dateAndTime: Date) {

        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("M", Locale.getDefault())
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

        yearOfDatePicker = yearFormat.format(dateAndTime).toInt()
        monthOfDatePicker = monthFormat.format(dateAndTime).toInt()
        dayOfDatePicker = dayFormat.format(dateAndTime).toInt()

        val hourFormat = SimpleDateFormat("hh", Locale.getDefault())
        val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
        val meridiemFormat = SimpleDateFormat("aa", Locale.ENGLISH)

        val hour = hourFormat.format(dateAndTime).toInt()
        val minuteOfTimePicker = minuteFormat.format(dateAndTime)
        val meridiem = meridiemFormat.format(dateAndTime)

        updateHourOfTimePicker = if (meridiem == "PM" && hour < 12) hour + 12 else hour
        updateMinuteOfTimePicker = minuteOfTimePicker.toInt()

        dateTextLabel.set(String.format("%d월 %d일", monthOfDatePicker, dayOfDatePicker))
        timeTextLabel.set(String.format("%d:%s%s", hour, minuteOfTimePicker, meridiem))

        initDatePickerAndTimePickerListener(yearOfDatePicker, monthOfDatePicker, dayOfDatePicker,
                updateHourOfTimePicker, updateMinuteOfTimePicker)

    }

    fun setDateTextLabel(setMonth: Int, setDay: Int) {
        monthOfDatePicker = setMonth
        dayOfDatePicker = setDay

        dateTextLabel.set(String.format("%d월 %d일", monthOfDatePicker, dayOfDatePicker))
    }

    fun setTimeTextLabel(setHour: Int, setMinute: Int) {

        val meridiem = if (setHour >= 12) "PM" else "AM"
        val minuteStr = if (setMinute < 10) {
            "0$setMinute"
        } else {
            "" + setMinute
        }

        updateHourOfTimePicker = setHour
        updateMinuteOfTimePicker = setMinute

        timeTextLabel.set(String.format(Locale.getDefault(), "%d:%s%s",
                if (setHour > 12) setHour - 12 else setHour, minuteStr, meridiem))
    }

    private fun getImageSaveDate(): Date {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.set(yearOfDatePicker, monthOfDatePicker - 1, dayOfDatePicker, updateHourOfTimePicker, updateMinuteOfTimePicker)
        return currentCalendar.time
    }

    fun loadDiaryViewData(diaryIndex: Int) {
        compositeDisposable.add(diaryRepository.findDiaryById(diaryIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { diary ->
                    initDateAndTime(diary.date)
                    loadDiaryImageComplete(diary.image!!)
                    loadImageFileNameForCheckUpdate = diary.image
                    descriptionTextLabel.set(diary.description)
                })
    }

    fun insertDiaryViewData() {

        val imageSaveDate: Date = getImageSaveDate()
        imageFileName = imageSaveDate.time
        compositeDisposable.add(diaryRepository.insertDiary(imageSaveDate, imageFileName.toString() + ".jpg", descriptionTextLabel.get().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    diaryInsertComplete()
                }, {
                    diaryInsertError()
                }))
    }

    fun updateDiaryViewData(diaryIndex: Int) {

        val imageSaveDate: Date = getImageSaveDate()
        imageFileName = imageSaveDate.time
        var image: String? = loadImageFileNameForCheckUpdate
        compositeDisposable.add(diaryRepository.findDiaryById(diaryIndex)
                .subscribeOn(Schedulers.single())
                .flatMapCompletable {
                    if (isPhotoUpdate) {
                        image = imageFileName.toString() + ".jpg"
                    }
                    diaryRepository.updateDiary(it, imageSaveDate, image!!, descriptionTextLabel.get().toString())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    diaryUpdateComplete()
                }, {
                    diaryUpdateError()
                }))
    }

    companion object {
        var imageFileName: Long? = null
        var isPhotoUpdate = false
    }

}