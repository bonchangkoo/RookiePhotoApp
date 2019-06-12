package kr.co.yogiyo.rookiephotoapp.diary.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase
import java.text.SimpleDateFormat
import java.util.*

class DiariesViewModel(application: Application) : AndroidViewModel(application) {

    private var yearMonthFormat = SimpleDateFormat("yyyy.M", Locale.getDefault())

    val nowPageYearMonth: ObservableField<String> = ObservableField(yearMonthFormat.format(Date()))

    private val compositeDisposable = CompositeDisposable()
    private val diaryDatabase = DiaryDatabase.getDatabase(application)

    private lateinit var diariesNavigator: DiariesNavigator

    private lateinit var nowPageCalendar: Calendar

    override fun onCleared() {
        super.onCleared()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    fun setNavigator(diariesNavigator: DiariesNavigator) {
        this.diariesNavigator = diariesNavigator
    }

    fun updateNowPageByPosition(position: Int) {
        nowPageCalendar = getCalendarFromPosition(position)
        nowPageYearMonth.set(yearMonthFormat.format(nowPageCalendar.time))
    }

    fun updateNowPageByYearMon(year: Int, month: Int) {
        // TODO: 포지션 반환
        nowPageCalendar = Calendar.getInstance().apply {
            set(year, month, 1)
            time
        }
        nowPageYearMonth.set(yearMonthFormat.format(nowPageCalendar.time))
    }

    fun getPositionFromYearMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1)
        }

        return DiariesActivity.FIRST_PAGE + howFarFromBase(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }

    private fun getCalendarFromPosition(position: Int): Calendar {
        val calendar = GregorianCalendar()
        calendar.time = DiariesActivity.BASE_DATE
        calendar.add(Calendar.MONTH, position - DiariesActivity.FIRST_PAGE)
        return calendar
    }

    private fun howFarFromBase(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.time = DiariesActivity.BASE_DATE
        val distanceYear = (year - calendar.get(Calendar.YEAR)) * 12
        val distanceMonth = month - calendar.get(Calendar.MONTH)
        return distanceYear + distanceMonth
    }

    fun loadThisMonthDiaries() {
        val fromCalendar = GregorianCalendar().apply {
            time = nowPageCalendar.time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val toCalendar = GregorianCalendar().apply {
            time = nowPageCalendar.time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        loadDiariesBetweenDates(fromCalendar.time, toCalendar.time)
    }

    private fun loadDiariesBetweenDates(from: Date, to: Date) {
        diariesNavigator.showLoading()
        compositeDisposable.add(diaryDatabase.diaryDao().findDiariesBetweenDates(from, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    diariesNavigator.loadThisMonthDiaries(sortDiaries(it, true))
                    diariesNavigator.hideLoading()
                }, {
                    diariesNavigator.hideLoading()
                })
        )
    }

    private fun sortDiaries(diaries: List<Diary>, reverse: Boolean): List<Diary> {
        return if (reverse) {
            diaries.sortedByDescending { diary -> diary.date }
        } else {
            diaries.sortedBy { diary -> diary.date }
        }
    }
}