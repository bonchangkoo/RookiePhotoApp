package kr.co.yogiyo.rookiephotoapp.gallery

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_gallery.*

import java.util.ArrayList

import kr.co.yogiyo.rookiephotoapp.GlobalApplication
import kr.co.yogiyo.rookiephotoapp.R

class GalleryFragment : Fragment() {

    private val galleryAdapter by lazy { GalleryAdapter(context, ArrayList()) }
    private val gridLayoutManager by lazy { GridLayoutManager(context, 3) }

    val selectedImageUri: Uri?
        get() = galleryAdapter.selectedImageUri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_gallery.run {
            setHasFixedSize(true)
            adapter = galleryAdapter
            layoutManager = gridLayoutManager
        }
    }

    override fun onResume() {
        super.onResume()
        arguments?.let {
            loadGallery(it.getString(FOLDER_NAME))
        }
    }

    private fun loadGallery(folderName: String?) {
        galleryAdapter.run {
            setImages(loadImages(folderName))
            notifyDataSetChanged()
        }
    }

    companion object {

        private const val FOLDER_NAME = "folderName"

        fun newInstance(folderName: String): GalleryFragment {
            return GalleryFragment().apply {
                arguments = Bundle().apply {
                    putString(FOLDER_NAME, folderName)
                }
            }
        }

        fun loadImages(folderName: String?): List<LoadImage> {
            val listOfAllImages = ArrayList<LoadImage>()
            GlobalApplication.globalApplicationContext.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.BUCKET_DISPLAY_NAME),
                    null, null, null)?.run {
                val columnIndexData = getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val columnIndexDateModified = getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val columnIndexFolderName = getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                while (moveToNext()) {
                    if (getString(columnIndexFolderName) == folderName || folderName == "All") {
                        listOfAllImages.add(LoadImage(getString(columnIndexData), getLong(columnIndexDateModified)))
                    }
                }

                close()

            } ?: return listOfAllImages

            return sortImages(listOfAllImages, true)
        }

        private fun sortImages(images: List<LoadImage>, reverse: Boolean): List<LoadImage> {
            return if (reverse) {
                images.sortedByDescending { loadImage -> loadImage.modifiedDateOfImage }
            } else {
                images.sortedBy { loadImage -> loadImage.modifiedDateOfImage }
            }
        }
    }
}
