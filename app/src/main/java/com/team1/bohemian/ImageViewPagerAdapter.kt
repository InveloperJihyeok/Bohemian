package com.team1.bohemian

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.ArrayList

class ImageViewPagerAdapter(
    private val imageList: ArrayList<String>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        // 각 이미지에 대한 Fragment를 생성하여 반환
        return ImageViewFragment.newInstance(imageList[position])
    }
}
