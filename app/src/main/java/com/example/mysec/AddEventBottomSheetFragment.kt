package com.example.mysec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddEventBottomSheetFragment : BottomSheetDialogFragment() {

    interface OnEventAddedListener {
        fun onEventAdded(event: String)
    }

    private var listener: OnEventAddedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventInput = view.findViewById<EditText>(R.id.event_input)
        val addButton = view.findViewById<Button>(R.id.add_event_button)

        addButton.setOnClickListener {
            val event = eventInput.text.toString()
            if (event.isNotBlank()) {
                listener?.onEventAdded(event)
                dismiss()
            }
        }
    }

    fun setOnEventAddedListener(listener: OnEventAddedListener) {
        this.listener = listener
    }
}
