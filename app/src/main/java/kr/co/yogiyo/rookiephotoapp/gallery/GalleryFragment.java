package kr.co.yogiyo.rookiephotoapp.gallery;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import kr.co.yogiyo.rookiephotoapp.R;


public class GalleryFragment extends Fragment {

    private static final String FOLDER_NAME = "folderName";

    private Context context;

    private RecyclerView galleryRecycler;
    private GalleryAdapter galleryAdapter;
    private GridLayoutManager gridLayoutManager;

    public static GalleryFragment newInstance(String folderName) {
        GalleryFragment galleryFragment = new GalleryFragment();

        Bundle bundle = new Bundle();
        bundle.putString(FOLDER_NAME, folderName);
        galleryFragment.setArguments(bundle);

        return galleryFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GalleryActivity) {
            this.context = context;
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

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            loadGallery(getArguments().getString(FOLDER_NAME));
        }
    }

    public List<String> getImagesPath(String folderName) {
        List<String> listOfAllImages = new ArrayList<>();
        String pathOfImage;

        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return listOfAllImages;
        }

        int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            if (!cursor.getString(columnIndexFolderName).equals(folderName) && !folderName.equals("All")) {
                continue;
            }
            pathOfImage = cursor.getString(columnIndexData);

            listOfAllImages.add(pathOfImage);
        }

        cursor.close();

        return listOfAllImages;
    }

    public Uri getSelectedImageUri() {
        return galleryAdapter.getSelectedImageUri();
    }

    private void setupRecyclerView() {
        galleryAdapter = new GalleryAdapter(context, new ArrayList<String>());
        gridLayoutManager = new GridLayoutManager(context, 3);
        galleryRecycler.setHasFixedSize(true);
        galleryRecycler.setAdapter(galleryAdapter);
        galleryRecycler.setLayoutManager(gridLayoutManager);
    }

    private void loadGallery(String folderName) {
        List<String> images = getImagesPath(folderName);
        galleryAdapter.setImages(images);
        galleryAdapter.notifyDataSetChanged();
    }
}
