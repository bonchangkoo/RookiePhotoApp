package kr.co.yogiyo.rookiephotoapp.gallery;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;

import java.io.File;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.R;

/**
 * GalleryAdapter
 *   : GalleryFragment의 RecyclerView에 GridLayout(3) 방식으로 이미지를 보여줌
 *
 * GalleryAdapter 역할
 *   : ViewHolder 생성
 *   : 선택한 이미지의 ViewHolder, imagePath 임시 저장
 *   : 사용자가 선택한 폴더명의 이미지 파일 경로 list를 받아 화면에 반영
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    // 갤러리 화면에에 보여줄 이미지들의 경로 list
    private List<String> imagePaths;

    // galleryActivity의 메서드 (setControlButton)를 호출하기 위한 변수
    // 사진 선택시 toolbar의 edit/done 옵션 메뉴 버튼 활성화/비활성화
    private GalleryActivity galleryActivity;
    // Glide library
    // requestManager = Glide.with(this), this = GalleryFragment
    private RequestManager requestManager;

    // selectedViewHolder : 선택된 사진의 ViewHolder (다른 사진 선택했을 때, 이전 사진 선택 효과 INVISIBLE 할 때 사용)
    private GalleryViewHolder selectedViewHolder;
    // selectedImagePath : 선택된 사진의 절대 경로 (선택한 사진의 경로를 Uri로 변환 후 GalleryActivity에 반환하여 이전 액티비티에 돌려주기 위해 사용)
    private String selectedImagePath;

    public GalleryAdapter(GalleryActivity galleryActivity, RequestManager requestManager, List<String> imagePaths) {
        this.galleryActivity = galleryActivity;
        this.requestManager = requestManager;
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gallery_image, viewGroup, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder galleryViewHolder, int i) {
        final String imagePath = imagePaths.get(i);
        requestManager.load(imagePath)
                .into(galleryViewHolder.imageView);
        FrameLayout selectedFrame = galleryViewHolder.selectedFrame;

        // 첫 실행시 사진이 선택됐을 때 사진 어두워지는 효과 설정
        // GONE = 선택 안됐을 때
        // VISIBLE = 선택 됐을 때
        selectedFrame.setVisibility(View.GONE);

        /*
         * setOnClickListener : 사진이 선택됐을 때, 선택 해제했을 때 설정
         * @selectedViewHolder : 사진 선택 후 다른 사진 선택할 때 이전 사진의 선택 효과를 GONE 하기 위해 임시 저장
         * @selectedImagePath : 선택된 사진의 경로를 uri로 변환해서 GalleryActivity에 반환하기 위해 selectedImagePath에 임시 저장
         * @selectedFrame : 사진 선택됐을 때 사진에 겹쳐서 효과를 나타낼 frame
         * @galleryActivity.setControlButton : 사진 선택됐을 때 toolbar의 edit/done 옵션 메뉴 버튼 활성화
         */
        galleryViewHolder.galleryItemConstraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedViewHolder == null && selectedImagePath == null) {
                    // 이전에 사진 선택된 적이 없을 때, 폴더명이 바뀌어서 선택된 사진이 없을 때

                    selectedViewHolder = galleryViewHolder;
                    selectedImagePath = imagePath;
                    galleryViewHolder.selectedFrame.setVisibility(View.VISIBLE);
                    galleryActivity.setControlButton(true);

                } else if (selectedImagePath.equals(imagePath)) {
                    // 선택됐던 사진을 선택해제할 때

                    galleryViewHolder.selectedFrame.setVisibility(View.GONE);
                    selectedViewHolder = null;
                    selectedImagePath = null;
                    galleryActivity.setControlButton(false);

                } else {
                    // 선택됐던 사진이 아닌 다른 사진을 선택할 때

                    selectedViewHolder.selectedFrame.setVisibility(View.GONE);
                    selectedViewHolder = galleryViewHolder;
                    selectedImagePath = imagePath;
                    galleryViewHolder.selectedFrame.setVisibility(View.VISIBLE);
                    galleryActivity.setControlButton(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    // 갤러리 Fragment가 처음 시작됐을 때 또는 폴더명이 변경됐을 때 image path list를 갱신하기 위해 선언
    public void setImages(List<String> images) {
        this.imagePaths = images;
    }

    // 경로를 uri로 변환해서 GalleryActivity에 반환하기 위해 선언
    public Uri getSelectedImageUri() {
        if (selectedImagePath != null) {
            return Uri.fromFile(new File(selectedImagePath));
        }
        return null;
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout galleryItemConstraint;
        private ImageView imageView;
        private FrameLayout selectedFrame;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            galleryItemConstraint = itemView.findViewById(R.id.constraint_gallery_item);
            imageView = itemView.findViewById(R.id.image_view);
            selectedFrame = itemView.findViewById(R.id.frame_selected);
        }
    }
}
