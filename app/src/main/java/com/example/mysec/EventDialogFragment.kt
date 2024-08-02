package com.example.mysec

import android.app.Dialog
import android.content.Context
import android.os.Bundle
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
        private const val ARG_DATE = "date"
        private const val ARG_DAY = "day"

        fun newInstance(date: Date, day: Int): EventDialogFragment {
            val fragment = EventDialogFragment()
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
                putInt(ARG_DAY, day)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var onEventAddedListener: OnEventAddedListener? = null
    private lateinit var eventAdapter: EventAdapter
    private val eventList = mutableListOf<String>()

    interface OnEventAddedListener {
        fun onEventAdded()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.background_shape)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventListView = view.findViewById<ListView>(R.id.event_list_view)
        val addEventButton = view.findViewById<Button>(R.id.add_event_button)
        val dateTextView = view.findViewById<TextView>(R.id.date_text_view)

        val date = arguments?.getSerializable(ARG_DATE) as Date
        val day = arguments?.getInt(ARG_DAY) ?: 1

        // 현재 달력의 월을 확인하여 올바른 날짜를 설정합니다.
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, day)
        }

        // 날짜를 포맷팅하여 텍스트 뷰에 설정
        val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        dateTextView.text = formattedDate

        // 어댑터 설정
        eventAdapter = EventAdapter(requireContext(), eventList)
        eventListView.adapter = eventAdapter

        // 선택된 날짜의 이벤트 로드
        loadEvents(calendar.time)

        addEventButton.setOnClickListener {
            showAddEventBottomSheet()
        }
    }

    private fun loadEvents(date: Date) {
        val userId = (parentFragment as? CalendarFragment)?.userId
        if (userId != null) {
            val dbHelper = DBHelper(requireContext())
            val events = dbHelper.getEventsForDate(userId, date.time)
            eventList.clear()
            eventList.addAll(events)
            eventAdapter.notifyDataSetChanged()
        }
    }

    private fun showAddEventBottomSheet() {
        val bottomSheetFragment = AddEventBottomSheetFragment().apply {
            setOnEventAddedListener(object : AddEventBottomSheetFragment.OnEventAddedListener {
                override fun onEventAdded(event: String) {
                    eventList.add(event)
                    eventAdapter.notifyDataSetChanged()
                    onEventAddedListener?.onEventAdded()
                }
            })
        }
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onEventAddedListener = targetFragment as? OnEventAddedListener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(850, 1200)
    }

    fun setOnEventAddedListener(listener: OnEventAddedListener) {
        onEventAddedListener = listener
    }
}
