package com.example.mbible

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mbible.data.NotesRepository

class NotesFragment : Fragment() {

    private lateinit var notesRepo: NotesRepository
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notesRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesRepo = NotesRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load notes list as default child fragment
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.notesContainer, buildNotesList())
                .commit()
        }
    }

    private fun buildNotesList(): Fragment {
        return NotesListFragment { noteId ->
            openEditor(noteId)
        }
    }

    fun openEditor(noteId: Long) {
        childFragmentManager.beginTransaction()
            .replace(R.id.notesContainer, NoteEditorFragment.newInstance(noteId))
            .addToBackStack(null)
            .commit()
    }
}