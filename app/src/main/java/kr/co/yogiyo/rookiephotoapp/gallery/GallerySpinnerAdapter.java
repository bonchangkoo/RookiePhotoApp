package kr.co.yogiyo.rookiephotoapp.gallery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import kr.co.yogiyo.rookiephotoapp.R;

/**
 * GallerySpinnerAdapter
 *   : spinner에 폴더명들을 나열해서 보여줌
 */
public class GallerySpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    // List<폴더명 (이미지 수)> 리스트 저장할 변수
    private List<String> folderNames;

    public GallerySpinnerAdapter(Context context, List<String> folderNames) {
        super(context, R.layout.spinner_gallery, folderNames);
        this.context = context;
        this.folderNames = folderNames;
    }

    // spinner 목록 보여줄 때마다 호출됨, spinner 목록 보여줌
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // 첫 시작시에만 호출됨, spinner 목록 보여줌
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        // spinner root View 객체 가져옴
        View view = LayoutInflater.from(context).inflate(R.layout.spinner_gallery, parent, false);
        TextView categoryText = view.findViewById(R.id.text_category);
        // spinner 목록의 TextView에 폴더명 (이미지 수) 저장
        categoryText.setText(folderNames.get(position));

        return view;
    }
}
