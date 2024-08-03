package com.example.mysec

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

// ViewPager2에 사용할 어댑터
class CalendarPageAdapter(
    fragment: Fragment,
    private val userId: String // 사용자 아이디
) : FragmentStateAdapter(fragment) {

    companion object {
        // ViewPager의 중앙 위치를 시작 위치로 설정
        const val START_POSITION = Int.MAX_VALUE / 2
    }

    // 전체 아이템 개수 반환 (무한 스크롤을 위해 매우 큰 값으로 설정)
    override fun getItemCount(): Int = Int.MAX_VALUE

    // 현재 위치에 해당하는 CalendarFragment 생성
    override fun createFragment(position: Int): Fragment {
        // 현재 시간에서 위치를 기준으로 월을 계산
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, position - START_POSITION)
        }
        // CalendarFragment를 생성하고 userId를 전달
        return CalendarFragment.newInstance(calendar.time, userId)
    }
}