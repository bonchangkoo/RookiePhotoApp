package kr.co.yogiyo.rookiephotoapp.diary.main

import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.widget.DatePicker
import kotlinx.android.synthetic.main.activity_diaries.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityDiariesBinding
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.settings.SettingsActivity
import java.util.*

class DiariesActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var diariesViewModel: DiariesViewModel
    private lateinit var activityDiariesBinding: ActivityDiariesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diaries)

        initialize()

        initView()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        viewpager.currentItem = diariesViewModel.getPositionFromYearMonth(year, month)

    }

    private fun initialize() {

        diariesViewModel = ViewModelProviders.of(this).get(DiariesViewModel::class.java)

        activityDiariesBinding = DataBindingUtil.setContentView(this, R.layout.activity_diaries)
        activityDiariesBinding.viewModel = diariesViewModel
    }

    private fun initView() {

        setSupportActionBar(toolbar)

        viewpager.run {
            adapter = DiariesFragmentPagerAdapter(this@DiariesActivity, this@DiariesActivity.supportFragmentManager)
            currentItem = FIRST_PAGE
            offscreenPageLimit = 3
        }

        btn_start_camera.setOnClickListener {
            val cameraIntent = Intent(this@DiariesActivity, CameraActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(cameraIntent)
        }

        btn_date_now_page.setOnClickListener {
            buildCalendarDialog(Calendar.getInstance(), this@DiariesActivity).show(supportFragmentManager, "test")
        }

        btn_add_diary.setOnClickListener {
            val intent = Intent(this@DiariesActivity, DiaryEditActivity::class.java).apply {
                putExtra("DIARY_IDX", -1)
            }
            startActivity(intent)
        }

        btn_start_settings.setOnClickListener { startActivity(Intent(this@DiariesActivity, SettingsActivity::class.java)) }

        btn_show_pre_page.setOnClickListener { viewpager.run { currentItem -= 1 } }

        btn_show_next_page.setOnClickListener { viewpager.run { currentItem += 1 } }
    }

    companion object {

        const val PAGES = 5
        const val LOOPS = 1000
        const val FIRST_PAGE = PAGES * LOOPS / 2
        var BASE_DATE = Date()
    }
}