package com.example.mbible

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mbible.data.BibleRepository
import kotlinx.coroutines.launch

class VersePagerAdapter(
    private val bookName: String,
    private val testament: String,
    private val chapterCount: Int,
    private val bibleRepo: BibleRepository,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<VersePagerAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.chapterText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter_page, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chapter = position + 1
        holder.textView.text = "Loading…"

        lifecycleOwner.lifecycleScope.launch {
            val verses = bibleRepo.getVerses(bookName, testament, chapter)
            val adapter = VerseAdapter(verses)
            holder.textView.text = adapter.buildSpannable(holder.itemView.context)
        }
    }

    override fun getItemCount() = chapterCount
}