package kr.co.yogiyo.rookiephotoapp.diary.main

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
import kr.co.yogiyo.rookiephotoapp.Constants
import kr.co.yogiyo.rookiephotoapp.R
import kr.co.yogiyo.rookiephotoapp.diary.DiaryDetailActivity
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary
import java.text.SimpleDateFormat
import java.util.*

class DiariesAdapter(val context: Context, var diaries: List<Diary>) : RecyclerView.Adapter<DiariesAdapter.DiariesViewHolder>() {

    private var hourMinuteFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
    private var dayFormat = SimpleDateFormat("dd일", Locale.getDefault())

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

        diariesViewHolder.dayText.text = dayFormat.format(calendar.time)
        diariesViewHolder.timeText.text = hourMinuteFormat.format(calendar.time)
        diariesViewHolder.descriptionText.text = diary.description

        val yogiDiaryStorageDir = Constants.YOGIDIARY_PATH

        val imageAbsolutePath = "${yogiDiaryStorageDir.absolutePath}/${diary.image}"
        Glide.with(context)
                .load(imageAbsolutePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.baseline_not_interested_black_36)
                .into(diariesViewHolder.imageView)
    }

    override fun getItemCount(): Int {
        return diaries.size
    }

    fun setItems(diaries: List<Diary>) {
        this.diaries = diaries
    }

    inner class DiariesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val dayText: TextView = itemView.findViewById(R.id.text_day)
        val timeText: TextView = itemView.findViewById(R.id.text_time)
        val descriptionText: TextView = itemView.findViewById(R.id.text_description)
        val imageView: ImageView = itemView.findViewById(R.id.image_view)

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, DiaryDetailActivity::class.java)
                intent.putExtra("DIARY_IDX", diaries[adapterPosition].idx)
                context.startActivity(intent)
            }
        }
    }
}
