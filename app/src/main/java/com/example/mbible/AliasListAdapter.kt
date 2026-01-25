package com.example.mbible

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView

class AliasListAdapter(
    context: Context,
    private val items: List<String>,
    private val onDelete: (String) -> Unit
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_alias, parent, false)

        val aliasText = view.findViewById<TextView>(R.id.aliasText)
        val deleteBtn = view.findViewById<ImageButton>(R.id.btnDeleteAlias)

        val alias = items[position]
        aliasText.text = alias
        deleteBtn.setOnClickListener { onDelete(alias) }

        return view
    }
}