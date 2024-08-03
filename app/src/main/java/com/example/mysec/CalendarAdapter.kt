package com.example.mysec

import DateCalendar
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysec.Event
import java.util.*

class CalendarAdapter(
    private val context: Context,
    private var date: Date,
    private var events: MutableList<Event>
) : RecyclerView.Adapter<CalendarAdapter.CalendarItemHolder>() {

    private var dataList: ArrayList<Int> = arrayListOf()
    private var dateCalendar: DateCalendar = DateCalendar(date)

    init {
        updateDate(date)
    }

    fun updateDate(newDate: Date) {
        date = newDate
        dateCalendar = DateCalendar(date)
        dateCalendar.initBaseCalendar()
        dataList = dateCalendar.dateList
        notifyDataSetChanged()
    }

    fun updateEvents(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged() // Adapter에 데이터 변경을 알림
    }

    var itemClick: ItemClick? = null

    interface ItemClick {
        fun onClick(view: View, position: Int, day: Int)
    }

    override fun onBindViewHolder(holder: CalendarItemHolder, position: Int) {
        val day = dataList[position]
        holder.bind(day, position)

        holder.itemView.setOnClickListener { v ->
            val isPreviousMonth = position < dateCalendar.prevTail
            val isNextMonth = position > dataList.size - dateCalendar.nextHead - 1

            if (isPreviousMonth || isNextMonth) {
                return@setOnClickListener
            }

            itemClick?.onClick(v, position, day)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_calendar, parent, false)
        return CalendarItemHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    inner class CalendarItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemCalendarDateText: TextView = itemView.findViewById(R.id.item_calendar_date_text)
        private val itemEventIndicator: View = itemView.findViewById(R.id.item_calendar_dot_view)

        fun bind(day: Int, position: Int) {
            val firstDateIndex = dateCalendar.prevTail
            val lastDateIndex = dataList.size - dateCalendar.nextHead - 1

            itemCalendarDateText.text = day.toString()

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

            if (isPreviousMonth || isNextMonth) {
                textColor = if (isWeekend) context.getColor(R.color.light_red) else context.getColor(R.color.light_gray)
                itemView.isClickable = false
                itemView.isFocusable = false
                itemEventIndicator.visibility = View.INVISIBLE // 이전 및 다음 달 날짜에는 점이 보이지 않도록 설정
            } else {
                itemView.isClickable = true
                itemView.isFocusable = true

                if (isToday) {
                    textColor = context.getColor(R.color.light_green)
                    textStyle = Typeface.BOLD
                } else if (isWeekend) {
                    textColor = context.getColor(R.color.red)
                }

                // 현재 달의 날짜인 경우에만 이벤트 점을 표시
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

            // 데이터가 업데이트된 후 UI를 즉시 갱신
            itemView.post {
                itemEventIndicator.visibility = if (itemEventIndicator.visibility == View.VISIBLE) View.VISIBLE else View.INVISIBLE
            }
        }
    }
}
