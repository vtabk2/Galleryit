package com.codebasetemplate.features.app.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codebasetemplate.features.app.main.fragment.MainFragment

class MainCategoryAdapter(
    private val list: MutableList<Pair<String, Int>>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return MainFragment.newInstance(list[position].second)
    }

    override fun getItemCount(): Int {
        return 3
    }
}