package kr.co.yogiyo.rookiephotoapp.ui.diary.main

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
import kr.co.yogiyo.rookiephotoapp.ui.diary.main.adapter.DiariesAdapter

class DiariesFragment : Fragment() {

    private val compositeDisposable by lazy { CompositeDisposable() }

    private lateinit var diariesViewModel: DiariesViewModel

    private lateinit var diariesAdapter: DiariesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diariesViewModel = ViewModelProviders.of(activity!!, DiariesViewModelFactory.getInstance(GlobalApplication.globalApplicationContext))
                .get(DiariesViewModel::class.java)
        compositeDisposable.add(
                diariesViewModel.diariesObservable
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        diariesViewModel.loadNowPageDiaries(arguments!!.getInt(POSITION))
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
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