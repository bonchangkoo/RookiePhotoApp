package kr.co.yogiyo.rookiephotoapp.diary.main

import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_diaries.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityDiariesBinding
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity
import kr.co.yogiyo.rookiephotoapp.settings.SettingsActivity
import java.util.Date

class DiariesActivity : BaseActivity() {

    private val calendarPickerDialog by lazy {
        YearMonthPickerDialog.newInstance(BASE_DATE.time).apply {
            setListener(DatePickerDialog.OnDateSetListener { _, year, month, _ ->
                this@DiariesActivity.viewpager.currentItem = diariesViewModel.getPositionFromYearMonth(year, month)
            })
        }
    }

    private lateinit var diariesViewModel: DiariesViewModel
    private lateinit var activityDiariesBinding: ActivityDiariesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialize()

        initView()
    }

    private fun initialize() {
        diariesViewModel = ViewModelProviders.of(this, DiariesViewModelFactory.getInstance(GlobalApplication.globalApplicationContext))
                .get(DiariesViewModel::class.java).apply {
                    showLoadingView = { showLoading(true) }
                    hideLoadingView = { showLoading(false) }
                }

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
            calendarPickerDialog.show(supportFragmentManager, "show_calendar_dialog")
        }

        btn_add_diary.setOnClickListener {
            val intent = Intent(this@DiariesActivity, DiaryEditActivity::class.java).apply {
                putExtra("DIARY_IDX", -1)
            }
            startActivity(intent)
        }

        btn_start_settings.setOnClickListener { startActivity(Intent(this@DiariesActivity, SettingsActivity::class.java)) }

        btn_show_pre_page.setOnClickListener { viewpager.currentItem -= 1 }

        btn_show_next_page.setOnClickListener { viewpager.currentItem += 1 }
    }

    companion object {
        const val PAGES = 5
        const val LOOPS = 1000
        const val FIRST_PAGE = PAGES * LOOPS / 2
        var BASE_DATE = Date()
    }
}