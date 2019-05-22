package kr.co.yogiyo.rookiephotoapp.diary.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.diary.db.Diary;
import kr.co.yogiyo.rookiephotoapp.diary.db.DiaryDatabase;

public class DiariesFragment extends Fragment {

    private boolean onResuming;

    private Context context;

    private LinearLayoutManager linearLayoutManager;
    private DiariesAdapter diariesAdapter;

    private RecyclerView diariesRecyclerView;
    private ProgressBar loadDiariesProgressBar;

    public static Fragment newInstance(DiariesActivity context, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);

        return Fragment.instantiate(context, DiariesFragment.class.getName(), bundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (onResuming && isVisibleToUser) {
            loadDiaries(false);
        }
        // TODO: 이전에 데이터베이스 접근 또는 API 호출 이력이 있으면 무시하도록 구현
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View root = inflater.inflate(R.layout.fragment_diaries, container, false);

        diariesRecyclerView = root.findViewById(R.id.recycler_diaries);
        linearLayoutManager = new LinearLayoutManager(container.getContext());
        diariesRecyclerView.setLayoutManager(linearLayoutManager);
        diariesAdapter = new DiariesAdapter(context, Glide.with(this), new ArrayList<Diary>());
        diariesRecyclerView.setAdapter(diariesAdapter);
        diariesRecyclerView.addItemDecoration(new DividerItemDecoration(context, linearLayoutManager.getOrientation()));
        loadDiariesProgressBar = root.findViewById(R.id.progressbar_load_diaries);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        onResuming = true;
        if (getUserVisibleHint()) {
            loadDiaries(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        onResuming = false;
    }

    public void loadDiaries(boolean forceUpdate) {
        loadDiaries(forceUpdate, true);
    }

    private void loadDiaries(boolean forceUpdate, boolean showLoadingUI) {
        if (this.getArguments() == null) {
            throw new NullPointerException();
        }
        int position = this.getArguments().getInt("position");
        Calendar calendar = DiariesActivity.getCalendar(position);

        if (calendar == null) {
            return;
        }

        Calendar fromCalendar = new GregorianCalendar();
        fromCalendar.setTime(calendar.getTime());
        Calendar toCalendar = new GregorianCalendar();
        toCalendar.setTime(calendar.getTime());

        fromCalendar.set(Calendar.DAY_OF_MONTH, 1);
        toCalendar.set(Calendar.DAY_OF_MONTH, 1);

        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        toCalendar.add(Calendar.MONTH, 1);
        toCalendar.add(Calendar.MILLISECOND, -1);

        if (showLoadingUI) {
            loadDiariesProgressBar.setVisibility(View.VISIBLE);
        }

        DiaryDatabase.getInstance(context).diaryDao().findDiariesCreatedBetweenDates(fromCalendar.getTime(), toCalendar.getTime())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<Diary>>() {
            @Override
            public void accept(List<Diary> diaries) throws Exception {
                diariesAdapter.setItems(diaries);
                diariesAdapter.notifyDataSetChanged();
                loadDiariesProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
