package kr.co.yogiyo.rookiephotoapp.diary.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_diaries.view.*
import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R

class DiariesFragment : Fragment() {

    private val compositeDisposable by lazy { CompositeDisposable() }

    private lateinit var diariesViewModel: DiariesViewModel

    private lateinit var diariesAdapter: DiariesAdapter

    private val diariesObservable by lazy { diariesViewModel.diariesObservable }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diariesViewModel = ViewModelProviders.of(activity!!, DiariesViewModelFactory.getInstance(GlobalApplication.globalApplicationContext))
                .get(DiariesViewModel::class.java)
        compositeDisposable.add(
                diariesObservable
                        .filter { it.first == arguments!!.getInt(POSITION) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            diariesAdapter.run {
                                setItems(it.second)
                                notifyDataSetChanged()
                            }
                        }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (container == null) {
            return null
        }

        val view = inflater.inflate(R.layout.fragment_diaries, container, false)

        view.recycler_diaries.run {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            diariesAdapter = DiariesAdapter(context, ArrayList())
            adapter = diariesAdapter
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
        }

        return view
    }

    // TODO: 이전에 데이터베이스 접근 또는 API 호출 이력이 있으면 무시하도록 구현 -> Fragment마다 호출하던 data load를 activity에서 한 번만 호출하도록 수정해야 합니다.
    override fun onResume() {
        super.onResume()
        if (userVisibleHint) {
            diariesViewModel.run {
                updateNowPageYearMonth(arguments!!.getInt(POSITION))
                loadNowPageDiaries(arguments!!.getInt(POSITION))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isResumed && isVisibleToUser) {
            diariesViewModel.run {
                updateNowPageYearMonth(arguments!!.getInt(POSITION))
                loadNowPageDiaries(arguments!!.getInt(POSITION))
            }
        }
    }

    companion object {

        private const val POSITION = "position"

        fun newInstance(context: DiariesActivity, position: Int): Fragment {
            val bundle = Bundle().apply {
                putInt(POSITION, position)
            }

            return instantiate(context, DiariesFragment::class.java.name, bundle)
        }
    }
}