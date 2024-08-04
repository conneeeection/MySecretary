package com.example.mysec

import DateCalendar
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysec.Event
import java.util.*

// 로그 태그
private const val TAG = "CalendarAdapter"

// RecyclerView Adapter를 상속받아 달력 항목을 표시하는 어댑터 클래스
class CalendarAdapter(
    private val context: Context,
    private var date: Date, // 현재 표시하고 있는 날짜
    private var events: MutableList<Event> // 이벤트 목록
) : RecyclerView.Adapter<CalendarAdapter.CalendarItemHolder>() {

    private var dataList: ArrayList<Int> = arrayListOf() // 날짜 데이터를 담을 리스트
    private var dateCalendar: DateCalendar = DateCalendar(date) // 날짜 관련 정보를 처리하는 객체

    init {
        Log.d(TAG, "CalendarAdapter initialized with date: $date")
        updateDate(date) // 초기 날짜 데이터 설정
    }

    // 날짜 데이터 업데이트 메서드
    fun updateDate(newDate: Date) {
        date = newDate
        dateCalendar = DateCalendar(date)
        dateCalendar.initBaseCalendar()
        dataList = dateCalendar.dateList
        notifyDataSetChanged() // 데이터 변경 알림
        Log.d(TAG, "Date updated to: $date")
    }

    // 이벤트 목록 업데이트 메서드
    fun updateEvents(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged() // 데이터 변경 알림
        Log.d(TAG, "Events updated: $events")
    }

    // 클릭 이벤트 리스너 인터페이스
    var itemClick: ItemClick? = null

    interface ItemClick {
        fun onClick(view: View, position: Int, day: Int)
    }

    // ViewHolder를 데이터에 맞게 바인딩하는 메서드
    override fun onBindViewHolder(holder: CalendarItemHolder, position: Int) {
        val day = dataList[position]
        holder.bind(day, position) // 날짜와 위치를 바인딩

        // 항목 클릭 시 이벤트 처리
        holder.itemView.setOnClickListener { v ->
            val isPreviousMonth = position < dateCalendar.prevTail
            val isNextMonth = position > dataList.size - dateCalendar.nextHead - 1

            // 이전 달 또는 다음 달 날짜 클릭 시 아무 동작도 하지 않음
            if (isPreviousMonth || isNextMonth) {
                return@setOnClickListener
            }

            // 리스너가 설정되어 있으면 클릭 이벤트 전달
            itemClick?.onClick(v, position, day)
        }
    }

    // ViewHolder를 생성하는 메서드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_calendar, parent, false)
        return CalendarItemHolder(view)
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int = dataList.size

    // RecyclerView의 ViewHolder 클래스
    inner class CalendarItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemCalendarDateText: TextView = itemView.findViewById(R.id.item_calendar_date_text)
        private val itemEventIndicator: View = itemView.findViewById(R.id.item_calendar_dot_view)

        // 날짜를 View에 바인딩하는 메서드
        fun bind(day: Int, position: Int) {
            val firstDateIndex = dateCalendar.prevTail
            val lastDateIndex = dataList.size - dateCalendar.nextHead - 1

            itemCalendarDateText.text = day.toString() // 날짜 텍스트 설정

            val currentCalendar = Calendar.getInstance()
            val todayDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
            val todayMonth = currentCalendar.get(Calendar.MONTH)
            val todayYear = currentCalendar.get(Calendar.YEAR)

            val isToday = day == todayDay && date.month == todayMonth && date.year + 1900 == todayYear
            val isPreviousMonth = position < firstDateIndex
            val isNextMonth = position > lastDateIndex
            val columnIndex = position % 7
            val isWeekend = columnIndex == 0

            var textColor = context.getColor(R.color.black)
            var textStyle = Typeface.NORMAL

            // 이전/다음 달 날짜의 스타일 설정
            if (isPreviousMonth || isNextMonth) {
                textColor = if (isWeekend) context.getColor(R.color.light_red) else context.getColor(R.color.light_gray)
                itemView.isClickable = false
                itemView.isFocusable = false
                itemEventIndicator.visibility = View.INVISIBLE // 이벤트 점 숨기기
            } else {
                itemView.isClickable = true
                itemView.isFocusable = true

                // 오늘 날짜 및 주말에 대한 스타일 설정
                if (isToday) {
                    textColor = context.getColor(R.color.light_green)
                    textStyle = Typeface.BOLD
                } else if (isWeekend) {
                    textColor = context.getColor(R.color.red)
                }

                // 현재 달의 날짜에 이벤트가 있는지 확인
                val calendar = Calendar.getInstance().apply {
                    set(date.year + 1900, date.month, day)
                }

                val hasEvent = events.any { event ->
                    val eventDate = Calendar.getInstance().apply {
                        time = event.date
                    }
                    eventDate.get(Calendar.DAY_OF_MONTH) == day &&
                            eventDate.get(Calendar.MONTH) == date.month &&
                            eventDate.get(Calendar.YEAR) == date.year + 1900
                }

                itemEventIndicator.visibility = if (hasEvent) View.VISIBLE else View.INVISIBLE
            }

            itemCalendarDateText.setTextColor(textColor)
            itemCalendarDateText.setTypeface(itemCalendarDateText.typeface, textStyle)

            // UI 갱신
            itemView.post {
                itemEventIndicator.visibility = if (itemEventIndicator.visibility == View.VISIBLE) View.VISIBLE else View.INVISIBLE
            }
        }
    }
}