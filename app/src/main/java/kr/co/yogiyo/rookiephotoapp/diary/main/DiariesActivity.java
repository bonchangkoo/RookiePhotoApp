package kr.co.yogiyo.rookiephotoapp.diary.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import kr.co.yogiyo.rookiephotoapp.R;
import kr.co.yogiyo.rookiephotoapp.camera.CameraActivity;

public class DiariesActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: PAGES의 의미 조사 필요
    public final static int PAGES = 5;
    public final static int LOOPS = 1000;
    public final static int FIRST_PAGE = PAGES * LOOPS / 2;
    public final static Date BASE_DATE = new Date();

    private DiariesFragmentPagerAdapter diariesFragmentPagerAdapter;

    private Calendar calendar;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private ImageView showPrePageImage;
    private ImageView showNextPageImage;
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

        setupActionBar();
        setupPager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_diary_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(DiariesActivity.this, CameraActivity.class));
                break;
            case R.id.menu_create:
                // TODO: 다이어리 글 추가 화면으로 이동
                break;
            case R.id.menu_convert_layout:
                // TODO: 달력 형태로 변경
                break;
            case R.id.menu_settings:
                // TODO: 설정 화면으로 이동
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewpager);
        showPrePageImage = findViewById(R.id.image_show_pre_page);
        showPrePageImage.setOnClickListener(this);
        showNextPageImage = findViewById(R.id.image_show_next_page);
        showNextPageImage.setOnClickListener(this);
        dateNowPageText = findViewById(R.id.text_date_now_page);

        setDateNowPageText(calendar);

    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException();
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.baseline_photo_camera_white_36);
    }

    private void setupPager() {
        diariesFragmentPagerAdapter = new DiariesFragmentPagerAdapter(this, this.getSupportFragmentManager());
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