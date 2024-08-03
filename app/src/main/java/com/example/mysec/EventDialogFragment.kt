package com.example.mysec

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventDialogFragment : DialogFragment() {

    companion object {
        // Fragment 인스턴스를 생성하기 위한 상수
        private const val ARG_DATE = "date"
        private const val ARG_DAY = "day"
        private const val ARG_USER_ID = "user_id"

        // Fragment 인스턴스를 생성하고 인자를 전달하는 메소드
        fun newInstance(date: Date, day: Int, userId: String): EventDialogFragment {
            val fragment = EventDialogFragment()
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
                putInt(ARG_DAY, day)
                putString(ARG_USER_ID, userId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var onEventAddedListener: OnEventAddedListener? = null
    private lateinit var eventAdapter: EventAdapter
    private val eventList = mutableListOf<Event>()
    private var userId: String? = null

    interface OnEventAddedListener {
        fun onEventAdded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 부모 Fragment가 OnEventAddedListener를 구현하고 있는지 확인
        onEventAddedListener = parentFragment as? OnEventAddedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // 다이얼로그 배경 설정
        dialog.window?.setBackgroundDrawableResource(R.drawable.background_shape)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 다이얼로그의 레이아웃을 설정
        return inflater.inflate(R.layout.dialog_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        val eventListView = view.findViewById<ListView>(R.id.event_list_view)
        val addEventButton = view.findViewById<Button>(R.id.add_event_button)
        val dateTextView = view.findViewById<TextView>(R.id.date_text_view)

        // Fragment 인자에서 날짜와 일자, 사용자 ID를 가져옴
        val date = arguments?.getSerializable(ARG_DATE) as Date
        val day = arguments?.getInt(ARG_DAY) ?: 1
        userId = arguments?.getString(ARG_USER_ID)

        // Calendar 객체를 사용하여 날짜 설정
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, day)
        }

        // 날짜 포맷 설정 및 표시
        val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        dateTextView.text = formattedDate

        // EventAdapter 설정 및 삭제 버튼 클릭 리스너 설정
        eventAdapter = EventAdapter(requireContext(), eventList) { event ->
            deleteEvent(event)
        }
        eventListView.adapter = eventAdapter

        // 이벤트 로드
        loadEvents(calendar.time)

        // 이벤트 추가 버튼 클릭 리스너 설정
        addEventButton.setOnClickListener {
            showAddEventBottomSheet(calendar.time)
        }
    }

    // 지정된 날짜에 대한 이벤트를 로드
    private fun loadEvents(date: Date) {
        userId?.let {
            val dbHelper = DBHelper(requireContext())
            val events = dbHelper.getEventsForDate(it, date.time)
            Log.d("EventDialogFragment", "Loaded events: $events")
            eventList.clear()
            eventList.addAll(events)
            eventAdapter.updateEvents(eventList) // 어댑터의 데이터 갱신
        } ?: Log.e("EventDialogFragment", "User ID is null")
    }

    // 이벤트 추가 다이얼로그를 표시
    private fun showAddEventBottomSheet(date: Date) {
        val bottomSheetFragment = AddEventBottomSheetFragment().apply {
            setOnEventAddedListener(object : AddEventBottomSheetFragment.OnEventAddedListener {
                override fun onEventAdded(event: String) {
                    userId?.let {
                        val dbHelper = DBHelper(requireContext())
                        val success = dbHelper.addEvent(it, date, event)
                        if (success) {
                            Log.d("EventDialogFragment", "Event added: $event")
                            loadEvents(date) // 새로 추가된 일정을 로드
                            onEventAddedListener?.onEventAdded() // 부모 Fragment에게 알림
                        } else {
                            Log.e("EventDialogFragment", "Failed to add event: $event")
                        }
                    }
                }
            })
        }
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    // 이벤트 삭제
    private fun deleteEvent(event: Event) {
        userId?.let {
            val dbHelper = DBHelper(requireContext())
            val success = dbHelper.deleteEvent(it, event) // Event 객체 전달
            if (success) {
                Log.d("EventDialogFragment", "Event deleted: ${event.event}") // 디버깅 로그 추가
                loadEvents(arguments?.getSerializable(ARG_DATE) as Date) // 삭제 후 일정을 새로 로드
            } else {
                Log.e("EventDialogFragment", "Failed to delete event: ${event.event}") // 실패 로그 추가
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그의 크기 설정
        dialog?.window?.setLayout(850, 1200)
    }

    // 리스너 설정
    fun setOnEventAddedListener(listener: OnEventAddedListener) {
        onEventAddedListener = listener
    }
}