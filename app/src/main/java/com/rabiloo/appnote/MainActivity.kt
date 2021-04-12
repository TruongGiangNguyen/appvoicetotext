package com.io.note

import android.graphics.Typeface
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView
import com.io.note.fragment.HomeFragment
import com.io.note.fragment.NotiFragment
import com.io.note.fragment.ProfileFragment
import com.rabiloo.appnote.R
import com.rabiloo.appnote.adapter.FragmentSlidePagerAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var floating_top_bar_navigation: BubbleNavigationConstraintView
    private lateinit var view_pager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        //No action bar
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        initView()
        configViewPager()
    }

    fun initView(){
        floating_top_bar_navigation = findViewById(R.id.floating_top_bar_navigation)
        view_pager = findViewById(R.id.view_pager)
    }

    fun configViewPager(){
        floating_top_bar_navigation.setTypeface(Typeface.createFromAsset(assets, "rubik.ttf"))
        /*floating_top_bar_navigation.setBadgeValue(0, "")
        floating_top_bar_navigation.setBadgeValue(1, "") //invisible badge*/

        val fragList = ArrayList<Fragment>()
        fragList.add(HomeFragment())
        fragList.add(ProfileFragment())
        fragList.add(NotiFragment())

        val pagerAdapter = FragmentSlidePagerAdapter(fragList, supportFragmentManager)
        view_pager.adapter = pagerAdapter
        //disble swipe
        /*view_pager.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }*/

        //swipe
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                floating_top_bar_navigation.setCurrentActiveItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

        floating_top_bar_navigation.setNavigationChangeListener { _, position ->
            view_pager.setCurrentItem(position, true)
        }
    }


}