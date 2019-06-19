package kr.co.yogiyo.rookiephotoapp.diary.main;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.DiaryDetailActivity;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;

public class DiariesAdapter extends RecyclerView.Adapter<DiariesAdapter.DiariesViewHolder> {

    private Context context;
    private List<Diary> diaries;

    public DiariesAdapter(Context context, List<Diary> diaries) {
        this.context = context;
        this.diaries = diaries;
    }

    @NonNull
    @Override
    public DiariesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_diary, viewGroup, false);

        return new DiariesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiariesViewHolder diariesViewHolder, int i) {
        Diary diary = diaries.get(i);
        Date date = diary.getDate();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        diariesViewHolder.dayText.setText(String.format(context.getString(R.string.text_day_now_diary), calendar.get(Calendar.DAY_OF_MONTH)));
        diariesViewHolder.timeText.setText(String.format(context.getString(R.string.text_time_now_diary),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE) < 10 ?
                        "0" + calendar.get(Calendar.MINUTE) : String.valueOf(calendar.get(Calendar.MINUTE))));
        diariesViewHolder.descriptionText.setText(diary.getDescription());

        File yogiDiaryStorageDir = Environment.getExternalStoragePublicDirectory(String.format(context.getString(R.string.text_yogidiary_path),
                Environment.DIRECTORY_PICTURES, "YogiDiary"));

        String imageAbsolutePath = String.format("%s/%s", yogiDiaryStorageDir.getAbsolutePath(), diary.getImage());
        Glide.with(context)
                .load(imageAbsolutePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(diariesViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return diaries.size();
    }

    public void setItems(List<Diary> diaries) {
        this.diaries = diaries;
    }

    public class DiariesViewHolder extends RecyclerView.ViewHolder {

        private TextView dayText;
        private TextView timeText;
        private TextView descriptionText;
        private ImageView imageView;

        // TODO: View 클릭시 해당 다이어리의 상세 화면으로 이동
        public DiariesViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.text_day);
            timeText = itemView.findViewById(R.id.text_time);
            descriptionText = itemView.findViewById(R.id.text_description);
            imageView = itemView.findViewById(R.id.image_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(DiariesAdapter.class.getSimpleName(), diaries.get(getAdapterPosition()).getIdx() + "");
                    Intent intent = new Intent(context, DiaryDetailActivity.class);
                    intent.putExtra("DIARY_IDX", diaries.get(getAdapterPosition()).getIdx());
                    context.startActivity(intent);
                }
            });
        }
    }
}
