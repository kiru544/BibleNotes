package com.example.mbible

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mbible.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private val onOpen: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.VH>(DIFF) {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.noteTitleRow)
        val preview: TextView = itemView.findViewById(R.id.notePreview)
        val date: TextView = itemView.findViewById(R.id.noteDate)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDeleteNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val note = getItem(position)
        holder.title.text = note.title

        // One-line preview from the body (newlines collapsed).
        val preview = note.body.replace(Regex("\\s+"), " ").trim()
        holder.preview.text = preview
        holder.preview.visibility = if (preview.isEmpty()) View.GONE else View.VISIBLE

        holder.date.text = DATE_FMT.format(Date(note.updatedAt))

        holder.itemView.setOnClickListener { onOpen(note) }
        holder.deleteBtn.setOnClickListener { onDelete(note) }
    }

    companion object {
        private val DATE_FMT = SimpleDateFormat("MMM d", Locale.getDefault())

        private val DIFF = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(old: Note, new: Note) = old.id == new.id
            override fun areContentsTheSame(old: Note, new: Note) = old == new
        }
    }
}
