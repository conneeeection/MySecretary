package com.example.mysec

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

class CalendarPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, position - START_POSITION)
        }
        return CalendarFragment.newInstance(calendar.time)
    }
}