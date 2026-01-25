package com.example.mbible

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChapterAdapter(
    private val chapters: List<Int>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false) as TextView
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chapter = chapters[position]
        holder.text.text = chapter.toString()
        holder.text.setOnClickListener { onClick(chapter) }
    }

    override fun getItemCount(): Int = chapters.size
}
