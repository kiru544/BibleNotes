package com.example.mbible

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mbible.data.Verse

class VerseAdapter(
    private val verses: List<Verse>
) : RecyclerView.Adapter<VerseAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val num: TextView = itemView.findViewById(R.id.verseNum)
        val text: TextView = itemView.findViewById(R.id.verseText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verse, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val v = verses[position]
        holder.num.text = v.verse.toString()
        holder.text.text = v.text
    }

    override fun getItemCount(): Int = verses.size
}
