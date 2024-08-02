package com.example.mysec

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class ProjectAdapter(context: Context, events: List<String>) : ArrayAdapter<String>(context, 0, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_project, parent, false)
        val projectName = view.findViewById<TextView>(R.id.project_name)

        projectName.text = getItem(position)

        return view
    }
}