package me.urfate.yomuki.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import me.urfate.yomuki.ui.fragment.setup.SourcesFragment
import me.urfate.yomuki.ui.fragment.setup.WelcomeFragment

class SetupPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private var viewPager: ViewPager2
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return if (position == 1) {
            SourcesFragment()
        } else WelcomeFragment(viewPager)
    }

    override fun getItemCount(): Int {
        return 2
    }
}