package com.example.mysec

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ProjectAdapter(context: Context, projects: List<Pair<String, String>>) : ArrayAdapter<Pair<String, String>>(context, 0, projects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_project, parent, false)
        val projectName = view.findViewById<TextView>(R.id.project_name)
        val projectDay = view.findViewById<TextView>(R.id.tvDay)

        val project = getItem(position)
        projectName.text = project?.first
        projectDay.text = project?.second

        return view
    }
}
