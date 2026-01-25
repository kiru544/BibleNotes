package com.example.mbible

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mbible.data.Note

class NotesAdapter(
    private var notes: List<Note>,
    private val onOpen: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.noteTitleRow)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDeleteNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val note = notes[position]
        holder.title.text = note.title

        holder.itemView.setOnClickListener { onOpen(note) }
        holder.deleteBtn.setOnClickListener { onDelete(note) }
    }

    override fun getItemCount(): Int = notes.size

    fun submit(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
