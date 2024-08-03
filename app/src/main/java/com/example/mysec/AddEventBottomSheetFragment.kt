package com.example.mysec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// BottomSheetDialogFragment를 상속받은 이벤트 추가 다이얼로그 프래그먼트
class AddEventBottomSheetFragment : BottomSheetDialogFragment() {

    // 이벤트가 추가되었을 때 호출되는 리스너 인터페이스
    interface OnEventAddedListener {
        fun onEventAdded(event: String)
    }

    private var listener: OnEventAddedListener? = null // 리스너 객체를 저장할 변수

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 다이얼로그에 표시될 레이아웃을 인플레이트하여 반환
        return inflater.inflate(R.layout.dialog_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 레이아웃에서 EditText와 Button 찾기
        val eventInput = view.findViewById<EditText>(R.id.event_input)
        val addButton = view.findViewById<Button>(R.id.add_event_button)

        // 버튼 클릭 시 이벤트 처리
        addButton.setOnClickListener {
            // EditText에서 입력된 문자열 가져오기
            val event = eventInput.text.toString()
            // 입력된 문자열이 비어있지 않은 경우
            if (event.isNotBlank()) {
                // 리스너가 설정되어 있으면 onEventAdded 호출
                listener?.onEventAdded(event)
                // 다이얼로그 닫기
                dismiss()
            }
        }
    }

    // 외부에서 리스너를 설정할 수 있는 메서드
    fun setOnEventAddedListener(listener: OnEventAddedListener) {
        this.listener = listener
    }
}