package kr.co.yogiyo.rookiephotoapp.diary.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import kr.co.yogiyo.rookiephotoapp.Constants;
import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;
import kr.co.yogiyo.rookiephotoapp.diary.DiaryEditActivity;
import kr.co.yogiyo.rookiephotoapp.settings.SettingsActivity;

public class DiariesActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: PAGES의 의미 조사 필요
    public final static int PAGES = 5;
    public final static int LOOPS = 1000;
    public final static int FIRST_PAGE = PAGES * LOOPS / 2;
    public final static Date BASE_DATE = new Date();

    private Calendar calendar;

    private ViewPager viewPager;
    private TextView dateNowPageText;

    public static Calendar getCalendar(int position) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DiariesActivity.BASE_DATE);
        calendar.add(Calendar.MONTH, position - DiariesActivity.FIRST_PAGE);
        return calendar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diaries);

        initialize();

        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_camera:
                Intent cameraIntent = new Intent(DiariesActivity.this, CameraActivity.class);
                cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cameraIntent);
                break;
            case R.id.btn_add_diary:
                // TODO: 다이어리 글 추가 화면으로 이동
                Intent intent = new Intent(this, DiaryEditActivity.class);
                intent.putExtra(Constants.DIARY_IDX, -1);
                startActivity(intent);
                break;
            case R.id.btn_convert_layout:
                // TODO: 달력 형태로 변경
                break;
            case R.id.btn_start_settings:
                startActivity(new Intent(DiariesActivity.this, SettingsActivity.class));
                break;
            case R.id.image_show_pre_page:
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                break;
            case R.id.image_show_next_page:
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                break;
        }
    }

    private void initialize() {
        calendar = new GregorianCalendar();
        calendar.setTime(new Date());
    }

    private void initView() {
        ImageButton startCameraButton = findViewById(R.id.btn_start_camera);
        ImageButton addButton = findViewById(R.id.btn_add_diary);
        ImageButton convertLayoutButton = findViewById(R.id.btn_convert_layout);
        ImageButton settingsButton = findViewById(R.id.btn_start_settings);
        ImageView showPrePageImage = findViewById(R.id.image_show_pre_page);
        ImageView showNextPageImage = findViewById(R.id.image_show_next_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewpager);
        dateNowPageText = findViewById(R.id.text_date_now_page);

        startCameraButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        convertLayoutButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

        showPrePageImage.setOnClickListener(this);
        showNextPageImage.setOnClickListener(this);

        setDateNowPageText(calendar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException();
        }

        DiariesFragmentPagerAdapter diariesFragmentPagerAdapter = new DiariesFragmentPagerAdapter(this, this.getSupportFragmentManager());
        viewPager.setAdapter(diariesFragmentPagerAdapter);
        viewPager.setCurrentItem(FIRST_PAGE);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                calendar = getCalendar(position);
                setDateNowPageText(calendar);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // Do nothing
            }
        });
    }

    public void setDateNowPageText(Calendar calendar) {
        dateNowPageText.setText(String.format(getString(R.string.text_date_now_page),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1));
    }
}