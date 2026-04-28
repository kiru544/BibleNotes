package com.example.mbible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import android.graphics.Typeface
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.mbible.data.BibleRepository
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mbible.data.NotesRepository
import android.widget.Button
import android.widget.EditText
import com.example.mbible.data.Note
import android.app.AlertDialog
import android.widget.ImageButton
import android.text.Editable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import com.example.mbible.BookAliasRepository
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.TextPaint


class MainActivity : ComponentActivity() {

    // Tabs
    private lateinit var tabOT: TextView
    private lateinit var tabNT: TextView
    private lateinit var tabDoc: TextView
    private lateinit var tabNotes: TextView

    // Bible views
    private lateinit var bookList: ListView
    private lateinit var chaptersRecycler: RecyclerView
    private lateinit var chaptersTopBar: View
    private lateinit var selectedBookTitle: TextView
    private lateinit var bibleRepo: BibleRepository

    // Notes views (list)
    private lateinit var notesContainer: View
    private lateinit var btnNewNote: Button
    private lateinit var notesRecycler: RecyclerView

    private lateinit var docsList: ListView

    // Notes views (editor page)
    private lateinit var noteEditorContainer: View
    private lateinit var noteTitleText: TextView
    private lateinit var noteTitleEdit: EditText
    private lateinit var noteBody: EditText
    private lateinit var btnSaveNote: Button

    // Notes data
    private lateinit var notesRepo: NotesRepository
    private lateinit var notesAdapter: NotesAdapter
    private var currentNoteId: Long? = null

    // State
    private var currentTestament: String = "Old"
    private var currentBook: String? = null
    private var inChaptersView = false
    private var inVersesView = false
    private var inNotesList = false
    private var inNoteEditor = false
    private lateinit var aliasRepo: BookAliasRepository
    private var isHighlighting = false
    private val highlightHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var highlightRunnable: Runnable? = null
    private var pendingHighlight = false
    private var inDocsList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Tabs
        tabOT = findViewById(R.id.tabOT)
        tabNT = findViewById(R.id.tabNT)
        tabDoc = findViewById(R.id.tabDoc)
        tabNotes = findViewById(R.id.tabNotes)

        // Bible views
        bookList = findViewById(R.id.bookList)
        chaptersRecycler = findViewById(R.id.chaptersRecycler)
        chaptersTopBar = findViewById(R.id.chaptersTopBar)
        selectedBookTitle = findViewById(R.id.selectedBookTitle)

        docsList = findViewById(R.id.docsList)

        // Notes list views
        notesContainer = findViewById(R.id.notesContainer)
        btnNewNote = findViewById(R.id.btnNewNote)
        notesRecycler = findViewById(R.id.notesRecycler)

        // Notes editor views
        noteEditorContainer = findViewById(R.id.noteEditorContainer)
        noteTitleText = findViewById(R.id.noteTitleText)
        noteTitleEdit = findViewById(R.id.noteTitleEdit)
        noteBody = findViewById(R.id.noteBody)
        noteBody.movementMethod = LinkMovementMethod.getInstance()
        noteBody.highlightColor = 0x00000000  // transparent click highlight
        btnSaveNote = findViewById(R.id.btnSaveNote)

        // Repos
        bibleRepo = BibleRepository(this)
        notesRepo = NotesRepository(this)

        // Highlight
        aliasRepo = BookAliasRepository(this)

        // Recycler setups
        chaptersRecycler.layoutManager = GridLayoutManager(this, 6)

        noteBody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return

                // cancel previous scheduled run
                highlightRunnable?.let { highlightHandler.removeCallbacks(it) }

                highlightRunnable = Runnable {
                    // If currently highlighting, queue one more run
                    if (isHighlighting) {
                        pendingHighlight = true
                        return@Runnable
                    }
                    highlightVerseRefs(noteBody.text) // always use latest text
                }

                highlightHandler.postDelayed(highlightRunnable!!, 300)
            }
        })
        notesAdapter = NotesAdapter(
            emptyList(),
            onOpen = { note ->
                val full = notesRepo.getById(note.id) ?: return@NotesAdapter
                openNote(full)
            },
            onDelete = { note ->
                AlertDialog.Builder(this)
                    .setTitle("Delete note?")
                    .setMessage("This will permanently delete “${note.title}”.")
                    .setPositiveButton("Delete") { _, _ ->
                        notesRepo.delete(note.id)   // you need delete() in NotesRepository
                        refreshNotes()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        notesRecycler.layoutManager = LinearLayoutManager(this)
        notesRecycler.adapter = notesAdapter


        // Default: OT
        currentTestament = "Old"
        selectTab(tabOT)
        loadBooks(currentTestament)
        showBooks()

        // Tab clicks
        tabOT.setOnClickListener {
            currentTestament = "Old"
            selectTab(tabOT)
            loadBooks(currentTestament)
            showBooks()
        }

        tabNT.setOnClickListener {
            currentTestament = "New"
            selectTab(tabNT)
            loadBooks(currentTestament)
            showBooks()
        }
        tabDoc.setOnClickListener {
            selectTab(tabDoc)
            showDocsList()
        }
        tabNotes.setOnClickListener {
            selectTab(tabNotes)
            showNotesList()
        }

        // Notes buttons
        btnNewNote.setOnClickListener {
            val input = EditText(this).apply {
                hint = "Note name"
            }

            android.app.AlertDialog.Builder(this)
                .setTitle("New note")
                .setView(input)
                .setPositiveButton("Create") { _, _ ->
                    val title = input.text.toString().trim().ifEmpty { "New Note" }

                    val newId = notesRepo.create(title)   // <-- new function
                    refreshNotes()

                    // open the real saved note (so it won't be empty)
                    val note = notesRepo.getById(newId) ?: return@setPositiveButton
                    openNote(note)
                    highlightVerseRefs(noteBody.text)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnSaveNote.setOnClickListener {
            val id = currentNoteId ?: return@setOnClickListener

            if (noteTitleEdit.visibility == View.VISIBLE) finishTitleEdit()

            val bodyText = noteBody.text.toString()
            val titleText = noteTitleText.text.toString().trim().ifEmpty { "New Note" }

            notesRepo.update(id, titleText, bodyText)
            refreshNotes()
            showNotesList()
        }

        noteTitleText.setOnClickListener {
            startTitleEdit()
        }

        noteTitleEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                finishTitleEdit()
                true
            } else false
        }

// Also finish if user taps away from the title edit
        noteTitleEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && noteTitleEdit.visibility == View.VISIBLE) {
                finishTitleEdit()
            }
        }
    }

    // ---------- Tabs ----------
    private fun selectTab(selected: TextView) {
        val tabs = listOf(tabOT, tabNT,tabDoc , tabNotes)
        for (t in tabs) {
            if (t == selected) setSelectedTab(t) else setUnselectedTab(t)
        }
    }

    private fun setSelectedTab(tab: TextView) {
        tab.setBackgroundResource(R.drawable.tab_bg_selected)
        tab.setTextColor(0xFFFFFFFF.toInt())
    }

    private fun setUnselectedTab(tab: TextView) {
        tab.setBackgroundResource(R.drawable.tab_bg_unselected)
        tab.setTextColor(0xFFB8C1EC.toInt())
    }

    // ---------- Bible ----------
    private fun loadBooks(testament: String) {
        val books = bibleRepo.getBooks(testament)
        val adapter = ArrayAdapter(this, R.layout.item_book, R.id.bookName, books)
        bookList.adapter = adapter

        bookList.setOnItemClickListener { _, _, position, _ ->
            val bookName = books[position]
            showChapters(bookName, testament)
        }
    }

    private fun showBooks() {
        // reset state
        inChaptersView = false
        inVersesView = false
        inNotesList = false
        inNoteEditor = false
        currentNoteId = null
        inDocsList = false

        // hide others
        notesContainer.visibility = View.GONE
        noteEditorContainer.visibility = View.GONE
        chaptersTopBar.visibility = View.GONE
        chaptersRecycler.visibility = View.GONE
        docsList.visibility = View.GONE

        // show books
        bookList.visibility = View.VISIBLE
    }

    private fun showChapters(bookName: String, testament: String) {
        inChaptersView = true
        inVersesView = false
        currentBook = bookName

        selectedBookTitle.text = bookName

        // swap views
        bookList.visibility = View.GONE
        notesContainer.visibility = View.GONE
        noteEditorContainer.visibility = View.GONE

        chaptersTopBar.visibility = View.VISIBLE
        chaptersRecycler.visibility = View.VISIBLE

        chaptersRecycler.layoutManager = GridLayoutManager(this, 6)

        val count = bibleRepo.getChapterCount(bookName, testament)
        val chapters = (1..count).toList()

        chaptersRecycler.adapter = ChapterAdapter(chapters) { chapter ->
            showVerses(bookName, testament, chapter)
        }
    }

    private fun showVerses(bookName: String, testament: String, chapter: Int) {
        inChaptersView = false
        inVersesView = true

        selectedBookTitle.text = "$bookName $chapter"

        chaptersRecycler.layoutManager = LinearLayoutManager(this)

        val verses = bibleRepo.getVerses(bookName, testament, chapter)
        val adapter = VerseAdapter(verses)

        chaptersRecycler.adapter = adapter

        // Build and set the spannable after adapter is attached
        chaptersRecycler.post {
            val vh = chaptersRecycler.findViewHolderForAdapterPosition(0) as? VerseAdapter.VH
            vh?.textView?.text = adapter.buildSpannable(this)
        }
    }

    private fun showDocsList() {
        inDocsList = true
        inNotesList = false
        inNoteEditor = false
        inChaptersView = false
        inVersesView = false

        // hide bible + notes views
        bookList.visibility = View.GONE
        chaptersTopBar.visibility = View.GONE
        chaptersRecycler.visibility = View.GONE
        notesContainer.visibility = View.GONE
        noteEditorContainer.visibility = View.GONE

        // show docs list
        docsList.visibility = View.VISIBLE

        // docs data (for now just one)
        val docs = listOf("Catechism of the Catholic Church")

        val adapter = ArrayAdapter(this, R.layout.item_book, R.id.bookName, docs)
        docsList.adapter = adapter

        docsList.setOnItemClickListener { _, _, position, _ ->
            when (docs[position]) {
                "Catechism of the Catholic Church" -> {
                    startActivity(android.content.Intent(this, CatechismActivity::class.java))
                }
            }
        }
    }
    // ---------- Notes ----------
    private fun refreshNotes() {
        notesAdapter.submit(notesRepo.getAll())
    }

    private fun showNotesList() {
        inNotesList = true
        inNoteEditor = false
        currentNoteId = null
        docsList.visibility = View.GONE

        // hide bible views
        bookList.visibility = View.GONE
        chaptersTopBar.visibility = View.GONE
        chaptersRecycler.visibility = View.GONE
        inDocsList = false

        // hide editor, show list
        noteEditorContainer.visibility = View.GONE
        notesContainer.visibility = View.VISIBLE

        refreshNotes()
    }

    private fun openNote(note: Note) {
        inNotesList = false
        inNoteEditor = true
        currentNoteId = note.id

        // hide list, show editor
        notesContainer.visibility = View.GONE
        noteEditorContainer.visibility = View.VISIBLE

        noteTitleText.text = note.title
        noteTitleEdit.setText(note.title)
        noteTitleEdit.visibility = View.GONE
        noteTitleText.visibility = View.VISIBLE
        noteBody.setText(note.body)
        highlightVerseRefs(noteBody.text)
    }

    private fun startTitleEdit() {
        noteTitleEdit.visibility = View.VISIBLE
        noteTitleText.visibility = View.GONE

        noteTitleEdit.setText(noteTitleText.text.toString())
        noteTitleEdit.requestFocus()
        noteTitleEdit.setSelection(noteTitleEdit.text.length)

        // show keyboard
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(noteTitleEdit, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun finishTitleEdit() {
        val newTitle = noteTitleEdit.text.toString().trim().ifEmpty { "New Note" }

        noteTitleText.text = newTitle
        noteTitleText.visibility = View.VISIBLE
        noteTitleEdit.visibility = View.GONE

        // hide keyboard
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(noteTitleEdit.windowToken, 0)
    }

    private val refRegex =
        Regex("""(?i)\b([1-3]?\s*[a-z\.]+)\s*(\d{1,3})\s*:(\s*(\d{1,3})(?:\s*-\s*(\d{1,3}))?)?(?=\s|${'$'}|\W)""")

    private fun highlightVerseRefs(editable: Editable) {
        if (isHighlighting) return
        isHighlighting = true

        try {
            // Remove old clickable spans we previously applied
            val oldClickSpans = editable.getSpans(0, editable.length, ClickableSpan::class.java)
            for (s in oldClickSpans) editable.removeSpan(s)

            val text = editable.toString()

            for (m in refRegex.findAll(text)) {
                val bookToken = m.groupValues[1]
                val chapterStr = m.groupValues[2]
                val verseStartStr = m.groupValues[4]  // was [3]
                val verseEndStr = m.groupValues[5]    // was [4]

                val canonical = aliasRepo.resolveBookToken(bookToken) ?: continue

                val ch = chapterStr.toIntOrNull() ?: continue

                val isFullChapter = verseStartStr.isBlank()

                val vsStart = if (isFullChapter) 1 else verseStartStr.toIntOrNull() ?: continue
                val vsEnd = when {
                    isFullChapter -> bibleRepo.getVerseCount(canonical, ch)
                    verseEndStr.isNotBlank() -> verseEndStr.toIntOrNull() ?: vsStart
                    else -> vsStart
                }

                if (ch <= 0 || vsStart <= 0 || vsEnd <= 0) continue
                if (vsEnd < vsStart) continue

                // Validate existence (cheap version: validate start + end)
                if (!isFullChapter) {
                    if (!bibleRepo.verseExists(canonical, ch, vsStart)) continue
                    if (!bibleRepo.verseExists(canonical, ch, vsEnd)) continue
                }

                val start = m.range.first
                val end = m.range.last + 1

                val clickSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // For now: show a verse preview dialog (later: replace with your top “display tab”)
                        val verses = bibleRepo.getVerseRange(canonical, ch, vsStart, vsEnd)
                        val message = verses.joinToString("\n\n") { v -> "${v.verse}. ${v.text}" }

                        android.app.AlertDialog.Builder(this@MainActivity)
                            .setTitle("$canonical $ch:${vsStart}${if (vsEnd != vsStart) "-$vsEnd" else ""}")
                            .setMessage(message)
                            .setPositiveButton("Close", null)
                            .show()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = 0xFFFF6F61.toInt() // dark red
                    }
                }

                editable.setSpan(
                    clickSpan,
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } finally {
            isHighlighting = false
            if (pendingHighlight) {
                pendingHighlight = false
                highlightHandler.post { highlightVerseRefs(noteBody.text) }
            }
        }
    }

    // ---------- Back ----------
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            inNoteEditor -> showNotesList()
            inNotesList -> {
                // return to books view of current testament
                selectTab(if (currentTestament == "Old") tabOT else tabNT)
                showBooks()
            }

            inVersesView -> {
                val book = currentBook ?: return
                showChapters(book, currentTestament)
            }

            inChaptersView -> showBooks()
            inDocsList -> {
                selectTab(if (currentTestament == "Old") tabOT else tabNT)
                showBooks()
            }
            else -> super.onBackPressed()
        }
    }
}