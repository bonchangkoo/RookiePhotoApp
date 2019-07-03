package kr.co.yogiyo.rookiephotoapp.diary.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.GregorianCalendar
import java.util.Calendar
import java.util.Date

class DiariesViewModel(
        private val diaryRepository: DiaryRepository,
        application: Application
) : AndroidViewModel(application) {

    private var yearMonthFormat = SimpleDateFormat("yyyy.M", Locale.getDefault())
    private val diariesPublishSubject by lazy {
        PublishSubject.create<Pair<Int, List<Diary>>>()
    }
    val diariesObservable: Observable<Pair<Int, List<Diary>>> by lazy {
        diariesPublishSubject.hide()
    }

    val nowPageYearMonth: ObservableField<String> = ObservableField(yearMonthFormat.format(Date()))

    private val compositeDisposable = CompositeDisposable()

    lateinit var showLoadingView: () -> Unit
    lateinit var hideLoadingView: () -> Unit

    override fun onCleared() {
        super.onCleared()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    fun loadNowPageDiaries(position: Int) {
        val fromCalendar = GregorianCalendar().apply {
            time = getCalendarFromPosition(position).time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val toCalendar = GregorianCalendar().apply {
            time = getCalendarFromPosition(position).time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        showLoadingView
        compositeDisposable.add(
                diaryRepository.findDiariesBetweenDates(fromCalendar.time, toCalendar.time)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe({ diaries ->
                            diariesPublishSubject.onNext(Pair(position, sortDiaries(diaries, false)))
                            hideLoadingView
                        }, {
                            hideLoadingView
                        })
        )
    }

    fun updateNowPageYearMonth(position: Int) {
        getCalendarFromPosition(position).run {
            nowPageYearMonth.set(yearMonthFormat.format(time))
        }
    }

    fun getPositionFromYearMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1)
        }

        return DiariesActivity.FIRST_PAGE + howFarFromBase(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }

    private fun getCalendarFromPosition(position: Int): Calendar {
        return GregorianCalendar().apply {
            time = DiariesActivity.BASE_DATE
            add(Calendar.MONTH, position - DiariesActivity.FIRST_PAGE)
        }
    }

    private fun howFarFromBase(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance().apply {
            time = DiariesActivity.BASE_DATE
        }
        val distanceYear = (year - calendar.get(Calendar.YEAR)) * 12
        val distanceMonth = month - calendar.get(Calendar.MONTH)
        return distanceYear + distanceMonth
    }

    private fun sortDiaries(diaries: List<Diary>, reverse: Boolean): List<Diary> {
        return if (reverse) {
            diaries.sortedByDescending { diary -> diary.date }
        } else {
            diaries.sortedBy { diary -> diary.date }
        }
    }
}