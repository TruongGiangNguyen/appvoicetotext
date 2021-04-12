package com.rabiloo.appnote.adapter


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
 */
class FragmentSlidePagerAdapter(private val fragmentList: ArrayList<Fragment>, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): Fragment {
            return fragmentList[position]
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}