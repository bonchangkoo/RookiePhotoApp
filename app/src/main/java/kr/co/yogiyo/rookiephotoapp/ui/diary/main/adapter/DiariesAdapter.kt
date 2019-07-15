package kr.co.yogiyo.rookiephotoapp.ui.diary.main.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_diary.view.*
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.ui.diary.detail.DiaryDetailActivity
import kr.co.yogiyo.rookiephotoapp.data.model.Diary
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.GregorianCalendar

class DiariesAdapter(val context: Context, var diaries: List<Diary>) : RecyclerView.Adapter<DiariesAdapter.DiariesViewHolder>() {

    private val hourMinuteFormat by lazy { SimpleDateFormat("hh:mma", Locale.getDefault()) }
    private val dayFormat by lazy { SimpleDateFormat("dd일", Locale.getDefault()) }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DiariesViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_diary, viewGroup, false)

        return DiariesViewHolder(view)
    }

    override fun onBindViewHolder(diariesViewHolder: DiariesViewHolder, i: Int) {

        val diary = diaries[i]
        val date = diary.date
        val calendar = GregorianCalendar().apply {
            time = date
        }

        diariesViewHolder.run {
            dayText.text = dayFormat.format(calendar.time)
            timeText.text = hourMinuteFormat.format(calendar.time)
                    .replace("오전", "AM")
                    .replace("오후", "PM")
            descriptionText.text = diary.description
        }

        val imageAbsolutePath = "${Constants.FOONCARE_PATH.absolutePath}/${diary.image}"
        Glide.with(context)
                .load(imageAbsolutePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(diariesViewHolder.imageView)
    }

    override fun getItemCount(): Int {
        return diaries.size
    }

    fun setItems(diaries: List<Diary>) {
        this.diaries = diaries
    }

    inner class DiariesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val dayText: TextView = itemView.text_day
        val timeText: TextView = itemView.text_time
        val descriptionText: TextView = itemView.text_description
        val imageView: ImageView = itemView.image_view

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, DiaryDetailActivity::class.java).apply {
                    putExtra("DIARY_IDX", diaries[adapterPosition].idx)
                }
                context.startActivity(intent)
            }
        }
    }
}
