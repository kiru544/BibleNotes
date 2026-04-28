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

class NotesListFragment(
    private val onOpenNote: (Long) -> Unit
) : Fragment() {

    private lateinit var notesRepo: NotesRepository
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesRepo = NotesRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notes_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notesRecycler = view.findViewById<RecyclerView>(R.id.notesRecycler)
        val btnNewNote = view.findViewById<Button>(R.id.btnNewNote)

        val btnExport = view.findViewById<Button>(R.id.btnExportNotes)
        val btnImport = view.findViewById<Button>(R.id.btnImportNotes)
        val btnAliasSettings = view.findViewById<android.widget.ImageButton>(R.id.btnAliasSettings)
        btnAliasSettings.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), SettingsActivity::class.java))
        }

        btnExport.setOnClickListener {
            exportNotes()
        }

        btnImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "text/plain"))
        }

        notesAdapter = NotesAdapter(
            onOpen = { note -> onOpenNote(note.id) },
            onDelete = { note ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete note?")
                    .setMessage("This will permanently delete \"${note.title}\".")
                    .setPositiveButton("Delete") { _, _ ->
                        notesRepo.delete(note.id)
                        refreshNotes()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        notesRecycler.layoutManager = LinearLayoutManager(requireContext())
        notesRecycler.adapter = notesAdapter
        notesRecycler.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                requireContext(),
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )

        btnNewNote.setOnClickListener {
            val input = EditText(requireContext()).apply { hint = "Note name" }
            AlertDialog.Builder(requireContext())
                .setTitle("New note")
                .setView(input)
                .setPositiveButton("Create") { _, _ ->
                    val title = input.text.toString().trim().ifEmpty { "New Note" }
                    val newId = notesRepo.create(title)
                    refreshNotes()
                    onOpenNote(newId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        refreshNotes()
    }

    override fun onResume() {
        super.onResume()
        refreshNotes()
    }

    private fun refreshNotes() {
        notesAdapter.submitList(notesRepo.getAll())
    }
    private val importLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val json = requireContext().contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.readText() ?: return@registerForActivityResult

            val count = notesRepo.importFromJson(json)
            refreshNotes()
            android.widget.Toast.makeText(
                requireContext(),
                "Imported $count notes",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Import failed: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun exportNotes() {
        try {
            val json = notesRepo.exportToJson(requireContext())
            val fileName = "mbible_notes_${System.currentTimeMillis()}.json"

            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/json")
            }

            val resolver = requireContext().contentResolver
            val uri = resolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                android.widget.Toast.makeText(
                    requireContext(),
                    "Exported to Downloads/$fileName",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Export failed: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

}