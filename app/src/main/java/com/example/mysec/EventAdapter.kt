package com.example.mysec

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView

class EventAdapter(
    private val context: Context,
    private var events: List<Event>,
    private val onDeleteClick: (Event) -> Unit
) : ArrayAdapter<Event>(context, R.layout.list_item_event, events) {

    private class ViewHolder(
        val eventTextView: TextView,
        val deleteButton: ImageButton
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_event, parent, false)
            viewHolder = ViewHolder(
                eventTextView = view.findViewById(R.id.event_text_view),
                deleteButton = view.findViewById(R.id.delete_button)
            )
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val event = getItem(position)!!
        viewHolder.eventTextView.text = event.event

        viewHolder.deleteButton.setOnClickListener {
            onDeleteClick(event)
        }

        return view
    }

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
