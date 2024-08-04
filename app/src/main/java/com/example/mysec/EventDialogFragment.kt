package com.example.mysec

import android.app.Dialog
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

// 사용자 ID를 저장하기 위한 상수
private const val ARG_USER_ID = "user_id"
// Fragment 인스턴스를 생성하기 위한 상수
private const val ARG_DATE = "date"
private const val ARG_DAY = "day"
// 로그 태그
private const val TAG = "EventDialogFragment"

class EventDialogFragment : DialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(userId: String, date: Date, day: Int) =
            EventDialogFragment().apply {
                Log.d(TAG, "Creating EventDialogFragment with userId: $userId")
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId) // 사용자 ID를 Bundle에 저장
                    putSerializable(ARG_DATE, date)
                    putInt(ARG_DAY, day)
                }
            }
    }

    private var onEventAddedListener: OnEventAddedListener? = null
    private lateinit var eventAdapter: EventAdapter
    private val eventList = mutableListOf<Event>()
    private var userId: String? = null
    private lateinit var dbHelper: DBHelper

    interface OnEventAddedListener {
        fun onEventAdded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onEventAddedListener = parentFragment as? OnEventAddedListener

        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            Log.d(TAG, "User ID from arguments in onCreate: $userId")
        }

        dbHelper = DBHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 사용자 ID가 null인 경우 DBHelper에서 설정하지 않도록 수정
        if (userId == null) {
            Log.e(TAG, "User ID is null in onCreateView")
        }
        Log.d(TAG, "onCreateView: userId = $userId")

        return inflater.inflate(R.layout.dialog_event, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // 다이얼로그 배경 설정
        dialog.window?.setBackgroundDrawableResource(R.drawable.background_shape)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        val eventListView = view.findViewById<ListView>(R.id.event_list_view)
        val addEventButton = view.findViewById<Button>(R.id.add_event_button)
        val dateTextView = view.findViewById<TextView>(R.id.date_text_view)

        // Fragment 인자에서 날짜와 일자 가져옴
        val date = arguments?.getSerializable(ARG_DATE) as Date
        val day = arguments?.getInt(ARG_DAY) ?: 1

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
            val events = dbHelper.getEventsForDate(it, date.time)
            Log.d(TAG, "Loaded events: $events")
            eventList.clear()
            eventList.addAll(events)
            eventAdapter.updateEvents(eventList) // 어댑터의 데이터 갱신
        } ?: Log.e(TAG, "User ID is null")
    }

    // 이벤트 추가 다이얼로그를 표시
    private fun showAddEventBottomSheet(date: Date) {
        val bottomSheetFragment = AddEventBottomSheetFragment().apply {
            setOnEventAddedListener(object : AddEventBottomSheetFragment.OnEventAddedListener {
                override fun onEventAdded(event: String) {
                    userId?.let {
                        val success = dbHelper.addEvent(it, date, event)
                        if (success) {
                            Log.d(TAG, "Event added: $event")
                            loadEvents(date) // 새로 추가된 일정을 로드
                            onEventAddedListener?.onEventAdded() // 부모 Fragment에게 알림
                        } else {
                            Log.e(TAG, "Failed to add event: $event")
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
            val success = dbHelper.deleteEvent(it, event) // Event 객체 전달
            if (success) {
                Log.d(TAG, "Event deleted: ${event.event}") // 디버깅 로그 추가
                loadEvents(arguments?.getSerializable(ARG_DATE) as Date) // 삭제 후 일정을 새로 로드
            } else {
                Log.e(TAG, "Failed to delete event: ${event.event}") // 실패 로그 추가
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
