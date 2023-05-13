package me.urfate.yomuki.ui.fragment.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import me.urfate.yomuki.R

class WelcomeFragment    // Required empty public constructor
    (private var viewPager2: ViewPager2) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.fragment_welcome, container, false)

        root.findViewById<View>(R.id.continue_button)
            .setOnClickListener { viewPager2.currentItem = 1 }
        return root
    }
}