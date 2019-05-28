package kr.co.yogiyo.rookiephotoapp.diary.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DiariesFragmentPagerAdapter extends FragmentPagerAdapter {

    private DiariesActivity context;

    public DiariesFragmentPagerAdapter(DiariesActivity context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return DiariesFragment.newInstance(context, position);
    }

    @Override
    public int getCount() {
        return DiariesActivity.PAGES * DiariesActivity.LOOPS;
    }
}