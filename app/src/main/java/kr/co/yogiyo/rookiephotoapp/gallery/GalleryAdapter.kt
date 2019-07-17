package kr.co.yogiyo.rookiephotoapp.gallery

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_gallery_image.view.*
import kr.co.yogiyo.rookiephotoapp.GlobalApplication

import kr.co.yogiyo.rookiephotoapp.R

class GalleryAdapter(
        private var loadImages: List<LoadImage>,
        private val galleryViewModel: GalleryViewModel
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    init {
        galleryViewModel.run {
            setSelection = { position, selected ->
                loadImages[position].selected = selected
                notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): GalleryViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_gallery_image, viewGroup, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(galleryViewHolder: GalleryViewHolder, position: Int) {
        galleryViewHolder.itemView.run {
            Glide.with(context ?: GlobalApplication.globalApplicationContext)
                    .load(loadImages[position].pathOfImage)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(image_view)

            frame_selected.visibility = if (loadImages[position].selected) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int {
        return loadImages.size
    }

    fun setImages(images: List<LoadImage>) {
        this.loadImages = images
    }

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                galleryViewModel.onImageSelected(
                        adapterPosition,
                        loadImages[adapterPosition].pathOfImage
                )
            }
        }
    }
}
