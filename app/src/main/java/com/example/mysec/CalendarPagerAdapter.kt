package com.example.mysec

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

// 로그 태그
private const val TAG = "CalendarPagerAdapter"

// ViewPager2에 사용할 어댑터
class CalendarPageAdapter(
    fragment: Fragment,
    private val userId: String // 사용자 아이디
) : FragmentStateAdapter(fragment) {

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, position - START_POSITION)
        }
        Log.d("CalendarPageAdapter", "Creating CalendarFragment for position: $position with date: ${calendar.time} and userId: $userId")
        return CalendarFragment.newInstance(calendar.time, userId)
    }
}
