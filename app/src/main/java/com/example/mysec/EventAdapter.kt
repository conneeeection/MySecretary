package com.example.mysec

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView

// Event 객체를 표시하는 ArrayAdapter 클래스
class EventAdapter(
    private val context: Context,
    private var events: List<Event>, // 이벤트 목록
    private val onDeleteClick: (Event) -> Unit // 삭제 버튼 클릭 시 호출될 콜백
) : ArrayAdapter<Event>(context, R.layout.list_item_event, events) {

    // ViewHolder 패턴을 사용하여 뷰의 성능을 향상시킴
    private class ViewHolder(
        val eventTextView: TextView, // 이벤트 텍스트를 표시할 TextView
        val deleteButton: ImageButton // 삭제 버튼
    )

    // 각 아이템의 뷰를 반환
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        // convertView가 null일 경우 새로운 뷰를 생성
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_event, parent, false)
            viewHolder = ViewHolder(
                eventTextView = view.findViewById(R.id.event_text_view),
                deleteButton = view.findViewById(R.id.delete_button)
            )
            view.tag = viewHolder // ViewHolder를 태그로 저장
        } else {
            // convertView가 존재할 경우 기존 ViewHolder를 재사용
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // 현재 위치의 이벤트 객체를 가져와서 뷰에 설정
        val event = getItem(position)!!
        viewHolder.eventTextView.text = event.event

        // 삭제 버튼 클릭 시 onDeleteClick 콜백 호출
        viewHolder.deleteButton.setOnClickListener {
            onDeleteClick(event)
        }

        return view
    }

    // 이벤트 목록 업데이트
    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged() // 데이터 변경을 어댑터에 알림
    }
}