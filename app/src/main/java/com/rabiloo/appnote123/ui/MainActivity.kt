package com.rabiloo.appnote123.ui

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView
import com.io.note.fragment.ProfileFragment
import com.rabiloo.appnote123.R
import com.rabiloo.appnote123.adapter.FragmentSlidePagerAdapter
import com.rabiloo.appnote123.fragment.HomeFragment
import java.util.*
import kotlin.collections.ArrayList

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

    fun initView() {
        floating_top_bar_navigation = findViewById(R.id.floating_top_bar_navigation)
        view_pager = findViewById(R.id.view_pager)
    }

    fun configViewPager() {
        floating_top_bar_navigation.setTypeface(Typeface.createFromAsset(assets, "rubik.ttf"))

        val fragList = ArrayList<Fragment>()
        fragList.add(HomeFragment())
        fragList.add(ProfileFragment())
//        fragList.add(NotiFragment())

        val pagerAdapter = FragmentSlidePagerAdapter(fragList, supportFragmentManager)
        view_pager.adapter = pagerAdapter

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