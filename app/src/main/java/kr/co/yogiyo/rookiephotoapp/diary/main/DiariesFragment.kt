package kr.co.yogiyo.rookiephotoapp.diary.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import java.util.*

class DiariesFragment : Fragment(), DiariesNavigator {

    private lateinit var diariesViewModel: DiariesViewModel

    private lateinit var diariesAdapter: DiariesAdapter

    private lateinit var diariesRecyclerView: RecyclerView
    private lateinit var loadDiariesProgressbar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diariesViewModel = ViewModelProviders.of((context as DiariesActivity)).get(DiariesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (container == null) {
            return null
        }

        val root = inflater.inflate(R.layout.fragment_diaries, container, false)

        diariesRecyclerView = root.findViewById<RecyclerView>(R.id.recycler_diaries).apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            diariesAdapter = DiariesAdapter(context, ArrayList())
            adapter = diariesAdapter
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
        }

        loadDiariesProgressbar = root.findViewById(R.id.progressbar_load_diaries)

        return root
    }

    // TODO: 이전에 데이터베이스 접근 또는 API 호출 이력이 있으면 무시하도록 구현 -> Fragment마다 호출하던 data load를 activity에서 한 번만 호출하도록 수정해야 합니다.
    override fun onResume() {
        super.onResume()
        if (userVisibleHint) {
            diariesViewModel.run {
                setNavigator(this@DiariesFragment)
                updateNowPageByPosition(arguments!!.getInt(POSITION))
                loadThisMonthDiaries()
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isResumed && isVisibleToUser) {
            diariesViewModel.run {
                setNavigator(this@DiariesFragment)
                updateNowPageByPosition(arguments!!.getInt(POSITION))
                loadThisMonthDiaries()
            }
        }
    }

    override fun loadThisMonthDiaries(diaries: List<Diary>) {
        diariesAdapter.setItems(diaries)
        diariesAdapter.notifyDataSetChanged()
    }

    override fun showLoading() {
        loadDiariesProgressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        loadDiariesProgressbar.visibility = View.GONE
    }

    companion object {

        private const val POSITION = "position"

        fun newInstance(context: DiariesActivity, position: Int): Fragment {
            val bundle = Bundle()
            bundle.putInt(POSITION, position)

            return instantiate(context, DiariesFragment::class.java.name, bundle)
        }
    }
}