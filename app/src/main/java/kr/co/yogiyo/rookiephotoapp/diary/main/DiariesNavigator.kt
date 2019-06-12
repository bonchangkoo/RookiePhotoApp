package kr.co.yogiyo.rookiephotoapp.diary.main

import kr.co.yogiyo.rookiephotoapp.diary.db.Diary

interface DiariesNavigator {
    fun loadThisMonthDiaries(diaries: List<Diary>)

    fun showLoading()

    fun hideLoading()
}
