package kr.co.yogiyo.rookiephotoapp.gallery

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableInt
import io.reactivex.subjects.PublishSubject
import kr.co.yogiyo.rookiephotoapp.loadImages

class GalleryViewModel : ViewModel() {

    val recentPosition = ObservableInt(-1)
    val loadImagesPublishSubject: PublishSubject<List<LoadImage>> by lazy {
        PublishSubject.create<List<LoadImage>>()
    }

    lateinit var setSelection: (Int, Boolean) -> Unit
    lateinit var returnResult: (String?) -> Unit
    lateinit var sendResult: (String?) -> Unit

    private var recentImagePath: String? = null

    fun startDoneButtonAction() {
        returnResult(recentImagePath)
    }

    fun startEditButtonAction() {
        sendResult(recentImagePath)
    }

    fun onItemSelected(itemName: String) {
        recentPosition.set(-1)
        loadImagesPublishSubject.onNext(loadImages(itemName))
    }

    fun onImageSelected(position: Int, imagePath: String) {
        when (recentPosition.get()) {
            -1 -> {
                recentPosition.set(position)
                setSelection(position, true)
                recentImagePath = imagePath
            }
            position -> {
                recentPosition.set(-1)
                setSelection(position, false)
                recentImagePath = null
            }
            else -> {
                setSelection(recentPosition.get(), false)
                recentPosition.set(position)
                setSelection(position, true)
                recentImagePath = imagePath
            }
        }
    }
}