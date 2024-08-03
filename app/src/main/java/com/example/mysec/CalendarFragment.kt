package com.example.mysec

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import java.util.*

class CalendarFragment : Fragment(), EventDialogFragment.OnEventAddedListener {

    interface OnMonthChangeListener {
        fun onMonthChanged(newDate: Date)
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private var calendar: Calendar = Calendar.getInstance()
    private var monthChangeListener: OnMonthChangeListener? = null
    internal var userId: String? = null
    private var dbHelper: DBHelper? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        monthChangeListener = when {
            context is OnMonthChangeListener -> context
            parentFragment is OnMonthChangeListener -> parentFragment as OnMonthChangeListener
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        arguments?.let {
            userId = it.getString("user_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.calendar_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        val date = Date(arguments?.getLong("date") ?: System.currentTimeMillis())
        calendar.time = date

        val events = userId?.let { dbHelper?.getAllEvents(it) } ?: emptyList()
        userId?.let {
            calendarAdapter = CalendarAdapter(requireContext(), date, events.toMutableList())
        }

        recyclerView.adapter = calendarAdapter

        calendarAdapter.itemClick = object : CalendarAdapter.ItemClick {
            override fun onClick(view: View, position: Int, day: Int) {
                showEventDialog(day)
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapView = snapHelper.findSnapView(recyclerView.layoutManager)
                    val position = recyclerView.layoutManager?.getPosition(snapView!!)

                    if (position != null) {
                        val currentMonthOffset = position / 7
                        val previousMonthOffset = (position - 1) / 7
                        val newMonth = if (currentMonthOffset > previousMonthOffset) {
                            calendar.get(Calendar.MONTH) + 1
                        } else {
                            calendar.get(Calendar.MONTH) - 1
                        }
                        calendar.set(Calendar.MONTH, newMonth)
                        calendar.set(Calendar.DATE, 1)
                        calendarAdapter.updateDate(calendar.time)
                        monthChangeListener?.onMonthChanged(calendar.time)
                        updateEventList()
                    }
                }
            }
        })

        updateEventList()
    }

    companion object {
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

    private fun showEventDialog(day: Int) {
        // 현재 Calendar 객체에서 날짜를 계산하여 올바른 월을 설정합니다.
        val selectedDate = Calendar.getInstance().apply {
            time = calendar.time
            set(Calendar.DAY_OF_MONTH, day)
        }.time

        // EventDialogFragment에 선택된 날짜와 일자를 전달합니다.
        val eventDialogFragment = EventDialogFragment.newInstance(selectedDate, day, userId!!).apply {
            setOnEventAddedListener(this@CalendarFragment)
        }
        eventDialogFragment.show(parentFragmentManager, eventDialogFragment.tag)
    }

    private fun updateEventList() {
        userId?.let {
            val events = dbHelper?.getAllEvents(it) ?: emptyList()
            calendarAdapter.updateEvents(events)
            recyclerView.post {
                recyclerView.adapter?.notifyDataSetChanged()
                recyclerView.requestLayout() // 레이아웃 요청
            }
        }
    }

    override fun onEventAdded() {
        // Handle any additional logic if needed after an event is added
        updateEventList()
    }
}
