package kr.co.yogiyo.rookiephotoapp.gallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.R;

/**
 * GalleryFragment
 *   : GalleryActivity로부터 폴더명 파라미터를 받고 화면에 보여지라는 요청을 받아
 *     폴더명 안의 사진들을 검색하고 RecyclerView - GridLayout으로 보여주는 GalleryFragment
 *
 * GalleryFragment 역할
 *   : toolbar 설정
 *   : Fragment에서 사진 선택시 toolbar의 옵션 메뉴 버튼 edit/done 활성화 기능 제공
 *   : 사진 파일을 가진 폴더 검색해서 spinner 설정
 *   : spinner에서 폴더 선택시 새 Fragment에 폴더명 목록을 전달하고 Fragment를 시작함
 */
public class GalleryFragment extends Fragment {

    private GalleryActivity context;

    // 화면에 사진들을 보여주기 위한 recyclerView 관련 변수
    private RecyclerView galleryRecycler;
    private GalleryAdapter galleryAdapter;
    private GridLayoutManager gridLayoutManager;

    // 현재 보여지는 Fragment인지 판단하기 위한 변수
    // onResuming : onResume() 라이프사이클 메서드 호출시 true, onPause() 호출시 false
    // userVisibleHint : 화면에 보여지라고 setUserVisibleHint()가 호출되면 true, 보여지지 말라고 호출되면 false
    private boolean onResuming;
    private boolean userVisibleHint;

    // 어떤 폴더의 사진을 검색할지 알기 위해 folderName을 파라미터로 전달받음
    public static GalleryFragment newInstance(String folderName) {

        GalleryFragment galleryFragment = new GalleryFragment();

        Bundle bundle = new Bundle();
        bundle.putString("folderName", folderName);
        galleryFragment.setArguments(bundle);

        return galleryFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GalleryActivity) {
            this.context = (GalleryActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        galleryRecycler = view.findViewById(R.id.recycler_gallery);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupRecyclerView();
    }

    // 현재 화면에 보여지는 Fragment인지 판단하고 loadGallery() 호출
    @Override
    public void onResume() {
        super.onResume();
        onResuming = true;
        if (getUserVisibleHint()) {
            if (getArguments() != null) {
                loadGallery(getArguments().getString("folderName"));
            }
        }
    }

    // 현재 Fragment의 라이프사이클 중 보여지지 말라고 요청될 때 호출됨
    @Override
    public void onPause() {
        super.onPause();
        onResuming = false;
    }

    // Fragment가 보여지라고 또는 보여지지 말라고 요청될 때 호출됨
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        userVisibleHint = isVisibleToUser;
        if (onResuming && userVisibleHint) {
            if (getArguments() != null) {
                loadGallery(getArguments().getString("folderName"));
            }
        }
    }

    // 단말기 내 전체 이미지 불러오기
    public List<String> getImagesPath(Activity activity, String folderName) {
        // 이미지 경로들을 저장할 list
        List<String> listOfAllImages = new ArrayList<>();
        String pathOfImage;

        // External volume(저장소)에 접근하기 위한 Uri 스타일 옵션 변수
        // content://media/external/images/media
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // 이미지 파일 경로와 이미지 파일을 가진 폴더명을 검색하기 위해 정의
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        // 이미지 파일 경로와 이미지 파일을 가진 폴더명을 필드로 가진 set에서
        // 현재 몇 번째 데이터를 가리키는지 나타냄
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return listOfAllImages;
        }

        // 해당하는 칼럼 인덱스를 가져옴
        // column_index_data = 이미지 파일 경로 카테고리 인덱스를 가져옴
        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        // column_index_folder_name = 폴더명 카테고리 인덱스를 가져옴
        int column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        // 데이터를 가리키는 커서를 다음 순서로 이동
        while (cursor.moveToNext()) {
            if (!cursor.getString(column_index_folder_name).equals(folderName) && !folderName.equals("All")) {
                // 사용자가 선택한 폴더명의 사진들을 검색할 때, 전체 폴더의 사진들을 검색할 때
                continue;
            }
            // 이미지 파일 경로 카테고리 인덱스의 값(=이미지 파일 경로)을 가져옴
            pathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(pathOfImage);
        }

        cursor.close();

        // 이미지 파일 경로 리스트 반환
        return listOfAllImages;
    }

    // GalleryActivity에 선택한 이미지의 Uri 반환
    public Uri getSelectedImageUri() {
        return galleryAdapter.getSelectedImageUri();
    }

    private void setupRecyclerView() {
        galleryAdapter = new GalleryAdapter(context, Glide.with(this), new ArrayList<String>());
        gridLayoutManager = new GridLayoutManager(context, 3);
        galleryRecycler.setHasFixedSize(true);
        galleryRecycler.setAdapter(galleryAdapter);
        galleryRecycler.setLayoutManager(gridLayoutManager);
    }

    private void loadGallery(String folderName) {
        List<String> images = getImagesPath(context, folderName);
        galleryAdapter.setImages(images);
        galleryAdapter.notifyDataSetChanged();
    }
}
