package kr.co.yogiyo.rookiephotoapp.gallery;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.R;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private List<String> imagePaths;

    private Context context;

    private GalleryViewHolder selectedViewHolder;

    public GalleryAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gallery_image, viewGroup, false);
        return new GalleryViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder galleryViewHolder, int i) {
        Glide.with(context)
                .load(imagePaths.get(i))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(galleryViewHolder.imageView);
        FrameLayout selectedFrame = galleryViewHolder.selectedFrame;

        selectedFrame.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public void setImages(List<String> images) {
        this.imagePaths = images;
    }

    public Uri getSelectedImageUri() {
        if (selectedViewHolder != null) {
            return Uri.fromFile(new File(imagePaths.get(selectedViewHolder.getAdapterPosition())));
        }
        return null;
    }

    public GalleryViewHolder getSelectedViewHolder() {
        return selectedViewHolder;
    }

    public void setSelectedViewHolder(GalleryViewHolder selectedViewHolder) {
        this.selectedViewHolder = selectedViewHolder;
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private FrameLayout selectedFrame;

        // TODO : 카메라에서 갤러리를 열었을 때 UI 수정 필요
        public GalleryViewHolder(final Context context, @NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            selectedFrame = itemView.findViewById(R.id.frame_selected);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof GalleryActivity) {
                        if (getSelectedViewHolder() == null) {
                            setSelectedViewHolder(GalleryViewHolder.this);
                            selectedFrame.setVisibility(View.VISIBLE);
                            ((GalleryActivity) context).setControlButtonEnabled(true);
                        } else if (getSelectedViewHolder() == GalleryViewHolder.this) {
                            selectedFrame.setVisibility(View.GONE);
                            setSelectedViewHolder(null);
                            ((GalleryActivity) context).setControlButtonEnabled(false);
                        } else {
                            getSelectedViewHolder().selectedFrame.setVisibility(View.GONE);
                            setSelectedViewHolder(GalleryViewHolder.this);
                            selectedFrame.setVisibility(View.VISIBLE);
                            ((GalleryActivity) context).setControlButtonEnabled(true);
                        }
                    }
                }
            });
        }
    }
}
