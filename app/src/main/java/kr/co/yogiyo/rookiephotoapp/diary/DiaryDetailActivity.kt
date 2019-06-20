package kr.co.yogiyo.rookiephotoapp.diary


import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.activity_diary_detail.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.databinding.ActivityDiaryDetailBinding
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryRepository

class DiaryDetailActivity : BaseActivity() {

    private lateinit var activityDiaryDetailBinding: ActivityDiaryDetailBinding
    private lateinit var diaryDetailViewModel: DiaryDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        diaryIdx = intent.getIntExtra("DIARY_IDX", 0)

        initialize()
        initView()

        diaryDetailViewModel.loadViewData(diaryIdx)
        diaryDetailViewModel.initViewModel()

    }

    private fun initialize() {
        val dao = DiaryDatabase.getDatabase(this)!!.diaryDao()
        val repository = DiaryRepository.getInstance(dao)
        val viewModelFactory = DiaryDetailViewModelFactory(repository)

        activityDiaryDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_diary_detail)
        diaryDetailViewModel = ViewModelProviders.of(this, viewModelFactory).get(DiaryDetailViewModel::class.java)
        activityDiaryDetailBinding.viewModel = diaryDetailViewModel
    }

    private fun DiaryDetailViewModel.initViewModel() {
        diaryDeleteComplete = {
            showToast(R.string.text_diary_detail_deleted)
            finish()
        }
        diaryDeleteError = {
            showToast(getString(R.string.text_cant_delete_diary))
        }
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        ib_back.setOnClickListener {
            onBackPressed()
        }

        ib_diary_delete.setOnClickListener {
            val builder = AlertDialog.Builder(this@DiaryDetailActivity).apply {
                setPositiveButton(getString(R.string.text_dialog_ok)) { dialog, id -> diaryDetailViewModel.deleteDiary(diaryIdx) }
                setNegativeButton(getString(R.string.text_dialog_no)) { dialog, id -> dialog.dismiss() }
            }
            val dialog = builder.create().apply {
                setTitle(getString(R.string.text_diary_delete))
                setMessage(getString(R.string.test_diary_delete_question))
            }
            dialog.show()
        }

        ib_diary_edit.setOnClickListener {
            val diaryEditActivityIntent = Intent(this, DiaryEditActivity::class.java).apply {
                putExtra("DIARY_IDX", diaryIdx)
            }
            startActivity(diaryEditActivityIntent)
            finish()
        }

    }

    companion object {
        private var diaryIdx: Int = 0
    }
}
