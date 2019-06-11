package kr.co.yogiyo.rookiephotoapp.diary


import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_diary_detail.*
import kr.co.yogiyo.rookiephotoapp.BaseActivity
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import kr.co.yogiyo.rookiephotoapp.diary.db.LocalDiaryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DiaryDetailActivity : BaseActivity() {

    // TODO : ViewModel 개선을 어떻게 할지 고민하기
    private var localDiaryViewModel: LocalDiaryViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_detail)

        localDiaryViewModel = ViewModelProviders.of(this).get(LocalDiaryViewModel::class.java)

        diaryIdx = intent.getIntExtra("DIARY_IDX", 0)

        initView()
        setViewData(diaryIdx)
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        ib_back.setOnClickListener {
            onBackPressed()
        }

        ib_diary_delete.setOnClickListener {
            val builder = AlertDialog.Builder(this@DiaryDetailActivity)
            builder.setPositiveButton(getString(R.string.text_dialog_ok)) { dialog, id -> deleteDiary(diaryIdx) }
            builder.setNegativeButton(getString(R.string.text_dialog_no)) { dialog, id -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.setTitle("다이어리 삭제")
            dialog.setMessage("정말로 삭제하시겠습니까?")
            dialog.show()
        }

        ib_diary_edit.setOnClickListener {
            val diaryEditActivityIntent = Intent(this, DiaryEditActivity::class.java)
            diaryEditActivityIntent.putExtra("DIARY_IDX", diaryIdx)
            startActivity(diaryEditActivityIntent)
            finish()
        }

    }

    private fun setViewData(diaryIndex: Int) {
        compositeDisposable.add(localDiaryViewModel?.findDiaryById(diaryIndex)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { diary ->
                    setDateAndTime(diary.date)
                    iv_diary_detail_photo.setImageURI(Uri.fromFile(File(Constants.YOGIDIARY_PATH, diary.image)))
                    tv_diary_detail_description.text = diary.description
                })
    }

    private fun setDateAndTime(dateAndTime: Date) {

        val monthFormat = SimpleDateFormat("M", Locale.getDefault())
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

        val month = monthFormat.format(dateAndTime)
        val day = dayFormat.format(dateAndTime)

        tv_diary_detail_date.text = String.format("%s월 %s일", month, day)

        val hourFormat = SimpleDateFormat("hh", Locale.getDefault())
        val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
        val meridiemFormat = SimpleDateFormat("aa", Locale.ENGLISH)

        val hour = hourFormat.format(dateAndTime)
        val minute = minuteFormat.format(dateAndTime)
        val meridiem = meridiemFormat.format(dateAndTime)

        tv_diary_detail_time.text = String.format("%s:%s%s", hour, minute, meridiem)
    }

    private fun deleteDiary(idx: Int) {
        compositeDisposable.add(localDiaryViewModel?.findDiaryById(idx)
                ?.subscribeOn(Schedulers.single())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Consumer<Diary> {
                    override fun accept(diary: Diary) {
                        diary?.run {
                            localDiaryViewModel!!.deleteDiary(this)
                                    .subscribeOn(Schedulers.single())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : CompletableObserver {
                                        override fun onSubscribe(d: Disposable) {
                                            //Do noting
                                        }

                                        override fun onComplete() {
                                            showToast(R.string.text_diary_detail_deleted)
                                            finish()
                                        }

                                        override fun onError(e: Throwable) {
                                            showToast(getString(R.string.text_cant_delete_diary))
                                        }
                                    })
                        }
                    }
                }))
    }

    companion object {

        private var diaryIdx: Int = 0
    }
}
