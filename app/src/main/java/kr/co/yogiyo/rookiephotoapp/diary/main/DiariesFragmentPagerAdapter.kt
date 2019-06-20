package kr.co.yogiyo.rookiephotoapp.diary.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class DiariesFragmentPagerAdapter(private val context: DiariesActivity, fragmentManager: FragmentManager)
    : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return DiariesFragment.newInstance(context, position)
    }

    override fun getCount(): Int {
        return DiariesActivity.PAGES * DiariesActivity.LOOPS
    }
}