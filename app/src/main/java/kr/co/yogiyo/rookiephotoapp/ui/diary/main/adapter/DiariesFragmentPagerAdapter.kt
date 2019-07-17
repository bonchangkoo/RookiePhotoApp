package kr.co.yogiyo.rookiephotoapp.ui.diary.main.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import kr.co.yogiyo.rookiephotoapp.ui.diary.main.DiariesActivity
import kr.co.yogiyo.rookiephotoapp.ui.diary.main.DiariesFragment

class DiariesFragmentPagerAdapter(private val context: DiariesActivity, fragmentManager: FragmentManager)
    : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return DiariesFragment.newInstance(context, position)
    }

    override fun getCount(): Int {
        return DiariesActivity.PAGES * DiariesActivity.LOOPS
    }
}