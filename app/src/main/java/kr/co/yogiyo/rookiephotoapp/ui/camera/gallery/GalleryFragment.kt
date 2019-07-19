package kr.co.yogiyo.rookiephotoapp.ui.camera.gallery

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_gallery.*

import java.util.ArrayList

import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.ui.camera.gallery.adapter.GalleryAdapter

class GalleryFragment : Fragment() {

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val galleryViewModel by lazy {
        ViewModelProviders.of(activity!!).get(GalleryViewModel::class.java)
    }

    private val galleryAdapter by lazy {
        GalleryAdapter(ArrayList(), galleryViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable.add(
                galleryViewModel.loadImagesPublishSubject
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            galleryAdapter.run {
                                setImages(it)
                                notifyDataSetChanged()
                            }
                        }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_gallery.run {
            setHasFixedSize(true)
            adapter = galleryAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    companion object {

        fun newInstance(): GalleryFragment {
            return GalleryFragment()
        }
    }
}
