package com.example.mysec

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class EventAdapter(context: Context, private val events: List<String>) :
    ArrayAdapter<String>(context, R.layout.list_item_event, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_event, parent, false)
        val eventTextView = view.findViewById<TextView>(R.id.event_text_view)
        eventTextView.text = getItem(position)
        return view
    }
}
