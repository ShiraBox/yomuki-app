package me.urfate.yomuki.ui.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import me.urfate.yomuki.R
import me.urfate.yomuki.adapter.SetupPagerAdapter

class SetupActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        val viewPager: ViewPager2 = findViewById(R.id.setup_view_pager)

        viewPager.adapter = SetupPagerAdapter(supportFragmentManager, lifecycle, viewPager)
    }
}