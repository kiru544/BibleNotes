package com.example.mbible

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.mbible.data.BibleRepository
import kotlinx.coroutines.launch

class BibleFragment : Fragment() {

    private lateinit var bibleRepo: BibleRepository
    private lateinit var bookPager: ViewPager2
    private lateinit var chaptersRecycler: RecyclerView
    private lateinit var chaptersTopBar: View
    private lateinit var selectedBookTitle: TextView

    private var testament: String = "Old"
    private var currentBook: String? = null
    private var currentChapter: Int? = null
    private lateinit var translationPicker: TextView
    private var inChaptersView = false
    private var inVersesView = false
    private lateinit var books: List<String>

    private lateinit var btnSwitchMode: Button
    private var isCardMode = true
    private lateinit var bookList: android.widget.ListView

    private lateinit var versePager: androidx.viewpager2.widget.ViewPager2

    companion object {
        private const val ARG_TESTAMENT = "testament"

        fun newInstance(testament: String): BibleFragment {
            val fragment = BibleFragment()
            val args = Bundle()
            args.putString(ARG_TESTAMENT, testament)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testament = arguments?.getString(ARG_TESTAMENT) ?: "Old"
        bibleRepo = BibleRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bible, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookPager = view.findViewById(R.id.bookPager)
        chaptersRecycler = view.findViewById(R.id.chaptersRecycler)
        chaptersTopBar = view.findViewById(R.id.chaptersTopBar)
        selectedBookTitle = view.findViewById(R.id.selectedBookTitle)
        btnSwitchMode = view.findViewById(R.id.btnSwitchMode)
        bookList = view.findViewById(R.id.bookList)
        translationPicker = view.findViewById(R.id.translationPicker)
        versePager = view.findViewById(R.id.versePager)
        updateTranslationLabel()
        translationPicker.setOnClickListener { showTranslationMenu() }

        viewLifecycleOwner.lifecycleScope.launch {
            books = bibleRepo.getBooks(testament)

            bookPager.adapter = BookPagerAdapter(books) { bookName ->
                showChapters(bookName)
            }

            val adapter = android.widget.ArrayAdapter(
                requireContext(),
                R.layout.item_book,
                R.id.bookName,
                books
            )
            bookList.adapter = adapter
            bookList.setOnItemClickListener { _, _, position, _ ->
                showChapters(books[position])
            }
        }

        // Switch mode toggle
        btnSwitchMode.setOnClickListener {
            isCardMode = !isCardMode
            if (isCardMode) {
                bookPager.visibility = View.VISIBLE
                bookList.visibility = View.GONE
            } else {
                bookPager.visibility = View.GONE
                bookList.visibility = View.VISIBLE
            }
        }

        showBookPager()
    }

    private fun showBookPager() {
        inChaptersView = false
        inVersesView = false
        chaptersTopBar.visibility = View.GONE
        chaptersRecycler.visibility = View.GONE
        versePager.visibility = View.GONE
        btnSwitchMode.visibility = View.VISIBLE

        if (isCardMode) {
            bookPager.visibility = View.VISIBLE
            bookList.visibility = View.GONE
        } else {
            bookPager.visibility = View.GONE
            bookList.visibility = View.VISIBLE
        }
    }

    private fun showChapters(bookName: String) {
        inChaptersView = true
        inVersesView = false
        currentBook = bookName

        selectedBookTitle.text = bookName
        bookPager.visibility = View.GONE
        versePager.visibility = View.GONE
        chaptersTopBar.visibility = View.VISIBLE
        chaptersRecycler.visibility = View.VISIBLE

        chaptersRecycler.layoutManager = GridLayoutManager(requireContext(), 6)
        btnSwitchMode.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val count = bibleRepo.getChapterCount(bookName, testament)
            chaptersRecycler.adapter = ChapterAdapter((1..count).toList()) { chapter ->
                showVerses(bookName, chapter)
            }
        }

        bookList.visibility = View.GONE
    }

    private fun showVerses(bookName: String, chapter: Int) {
        inChaptersView = false
        inVersesView = true
        currentChapter = chapter

        selectedBookTitle.text = "$bookName $chapter"
        btnSwitchMode.visibility = View.GONE

        // Hide the recycler, show the pager
        chaptersRecycler.visibility = View.GONE
        versePager.visibility = View.VISIBLE
        chaptersTopBar.visibility = View.VISIBLE
        bookPager.visibility = View.GONE
        bookList.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val chapterCount = bibleRepo.getChapterCount(bookName, testament)

            versePager.adapter = VersePagerAdapter(
                bookName, testament, chapterCount, bibleRepo, viewLifecycleOwner
            )

            // Update title + currentChapter when swiping
            versePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentChapter = position + 1
                    selectedBookTitle.text = "$bookName ${position + 1}"
                }
            })

            versePager.setCurrentItem(chapter - 1, false)
        }
    }
    private fun updateTranslationLabel() {
        translationPicker.text = "${bibleRepo.activeTranslation.abbreviation} ▾"
    }

    private fun showTranslationMenu() {
        val popup = android.widget.PopupMenu(requireContext(), translationPicker)
        val translations = com.example.mbible.data.Translations.ALL
        translations.forEachIndexed { index, t ->
            popup.menu.add(0, index, index, "${t.abbreviation} — ${t.displayName}")
        }
        popup.setOnMenuItemClickListener { item ->
            val chosen = translations[item.itemId]
            bibleRepo.setActiveTranslation(chosen.id)
            updateTranslationLabel()
            refreshCurrentView()
            true
        }
        popup.show()
    }

    private fun refreshCurrentView() {
        when {
            inVersesView -> {
                val book = currentBook ?: return
                val chapter = currentChapter ?: return
                showVerses(book, chapter)
            }
            inChaptersView -> {
                val book = currentBook ?: return
                showChapters(book)
            }
            else -> {
                // On the book pager — book names are the same across
                // translations, so nothing needs reloading.
            }
        }
    }
    fun onBackPressed(): Boolean {
        return when {
            inVersesView -> {
                val book = currentBook ?: return false
                showChapters(book)
                true
            }
            inChaptersView -> {
                showBookPager()
                true
            }
            else -> false
        }
    }
}