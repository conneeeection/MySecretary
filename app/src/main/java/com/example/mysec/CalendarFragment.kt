package com.example.mysec

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import java.util.*

// 달력을 표시하고 이벤트를 처리하는 Fragment 클래스
class CalendarFragment : Fragment(), EventDialogFragment.OnEventAddedListener {

    interface OnMonthChangeListener {
        fun onMonthChanged(newDate: Date) // 월이 변경되었을 때 호출되는 콜백
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private var calendar: Calendar = Calendar.getInstance() // 현재 달력 날짜
    private var monthChangeListener: OnMonthChangeListener? = null // 월 변경 리스너
    private var userId: String? = null // 사용자 아이디
    private var dbHelper: DBHelper? = null // DB 헬퍼

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("CalendarFragment", "onAttach called")
        // OnMonthChangeListener를 찾아 설정
        monthChangeListener = when {
            context is OnMonthChangeListener -> context
            parentFragment is OnMonthChangeListener -> parentFragment as OnMonthChangeListener
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CalendarFragment", "onCreate called")
        dbHelper = DBHelper(requireContext()) // DB 헬퍼 초기화
        arguments?.let {
            userId = it.getString("user_id") // 사용자 아이디 가져오기
            Log.d("CalendarFragment", "userId: $userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CalendarFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_calendar, container, false) // 레이아웃 설정
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CalendarFragment", "onViewCreated called")

        recyclerView = view.findViewById(R.id.calendar_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7) // 7열 그리드 레이아웃 설정

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView) // 스크롤 스냅 설정

        // 전달받은 날짜 또는 현재 날짜로 달력 설정
        val date = Date(arguments?.getLong("date") ?: System.currentTimeMillis())
        calendar.time = date
        Log.d("CalendarFragment", "Calendar date set to: ${calendar.time}")

        // 이벤트 목록 가져오기
        val events = userId?.let { dbHelper?.getAllEvents(it) } ?: emptyList()
        Log.d("CalendarFragment", "Fetched events: $events")
        calendarAdapter = CalendarAdapter(requireContext(), date, events.toMutableList())
        recyclerView.adapter = calendarAdapter

        // 날짜 항목 클릭 리스너 설정
        calendarAdapter.itemClick = object : CalendarAdapter.ItemClick {
            override fun onClick(view: View, position: Int, day: Int) {
                Log.d("CalendarFragment", "Date clicked: $day")
                showEventDialog(day)
            }
        }

        // 스크롤 상태 변경 리스너 설정
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapView = snapHelper.findSnapView(recyclerView.layoutManager)
                    val position = recyclerView.layoutManager?.getPosition(snapView!!)

                    if (position != null) {
                        // 현재 월과 이전 월을 비교하여 월 변경 결정
                        val currentMonthOffset = position / 7
                        val previousMonthOffset = (position - 1) / 7
                        val newMonth = if (currentMonthOffset > previousMonthOffset) {
                            calendar.get(Calendar.MONTH) + 1
                        } else {
                            calendar.get(Calendar.MONTH) - 1
                        }
                        calendar.set(Calendar.MONTH, newMonth)
                        calendar.set(Calendar.DATE, 1)
                        Log.d("CalendarFragment", "Month changed to: ${calendar.time}")
                        calendarAdapter.updateDate(calendar.time)
                        monthChangeListener?.onMonthChanged(calendar.time) // 월 변경 리스너 호출
                        updateEventList() // 이벤트 목록 업데이트
                    }
                }
            }
        })

        updateEventList() // 초기 이벤트 목록 업데이트
    }

    companion object {
        // 새로운 인스턴스를 생성하는 메서드
        fun newInstance(date: Date, userId: String): CalendarFragment {
            val fragment = CalendarFragment()
            val args = Bundle().apply {
                putLong("date", date.time)
                putString("user_id", userId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    // 선택한 날짜의 이벤트 다이얼로그를 표시하는 메서드
    private fun showEventDialog(day: Int) {
        Log.d("CalendarFragment", "Showing event dialog for day: $day with userId: $userId")
        val selectedDate = Calendar.getInstance().apply {
            time = calendar.time
            set(Calendar.DAY_OF_MONTH, day)
        }.time

        val eventDialogFragment = EventDialogFragment.newInstance(userId ?: "", selectedDate, day).apply {
            setOnEventAddedListener(this@CalendarFragment)
        }
        eventDialogFragment.show(parentFragmentManager, eventDialogFragment.tag)
    }

    // 이벤트 목록을 업데이트하는 메서드
    private fun updateEventList() {
        Log.d("CalendarFragment", "Updating event list")
        userId?.let {
            val events = dbHelper?.getAllEvents(it) ?: emptyList()
            Log.d("CalendarFragment", "Events to update: $events")
            calendarAdapter.updateEvents(events)
            recyclerView.post {
                recyclerView.adapter?.notifyDataSetChanged()
                recyclerView.requestLayout() // 레이아웃 요청
            }
        }
    }

    // 이벤트 추가 후 호출되는 메서드
    override fun onEventAdded() {
        Log.d("CalendarFragment", "Event added, updating list")
        updateEventList() // 이벤트 목록 갱신
    }
}