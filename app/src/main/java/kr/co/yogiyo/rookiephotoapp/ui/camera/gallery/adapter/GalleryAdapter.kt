package kr.co.yogiyo.rookiephotoapp.ui.camera.gallery.adapter

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_gallery_image.view.*
import kr.co.yogiyo.rookiephotoapp.GlobalApplication

import java.io.File

import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.ui.camera.gallery.GalleryActivity
import kr.co.yogiyo.rookiephotoapp.ui.camera.gallery.LoadImage

// TODO : ViewModel 갖는 형태로 수정
class GalleryAdapter(private val context: Context?, private var loadImages: List<LoadImage>) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private var selectedViewHolder: GalleryViewHolder? = null

    val selectedImageUri: Uri?
        get() = selectedViewHolder?.run {
            Uri.fromFile(File(loadImages[adapterPosition].pathOfImage))
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): GalleryViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_gallery_image, viewGroup, false)
        return GalleryViewHolder(context ?: GlobalApplication.globalApplicationContext, view)
    }

    override fun onBindViewHolder(galleryViewHolder: GalleryViewHolder, i: Int) {
        Glide.with(context ?: GlobalApplication.globalApplicationContext)
                .load(loadImages[i].pathOfImage)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(galleryViewHolder.imageView)
        val selectedFrame = galleryViewHolder.selectedFrame

        selectedFrame.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return loadImages.size
    }

    fun setImages(images: List<LoadImage>) {
        this.loadImages = images
        if (context is GalleryActivity){
            context.setControlButtonEnabled(false)
        }
    }

    inner class GalleryViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.image_view
        val selectedFrame: FrameLayout = itemView.frame_selected

        init {
            itemView.setOnClickListener {
                if (context is GalleryActivity) {
                    when (selectedViewHolder) {
                        null -> {
                            selectedViewHolder = this@GalleryViewHolder
                            selectedFrame.visibility = View.VISIBLE
                            context.setControlButtonEnabled(true)
                        }
                        this@GalleryViewHolder -> {
                            selectedFrame.visibility = View.GONE
                            selectedViewHolder = null
                            context.setControlButtonEnabled(false)
                        }
                        else -> {
                            selectedViewHolder!!.selectedFrame.visibility = View.GONE
                            selectedViewHolder = this@GalleryViewHolder
                            selectedFrame.visibility = View.VISIBLE
                            context.setControlButtonEnabled(true)
                        }
                    }
                }
            }
        }
    }
}
