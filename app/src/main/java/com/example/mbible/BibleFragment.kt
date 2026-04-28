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
import androidx.viewpager2.widget.ViewPager2
import com.example.mbible.data.BibleRepository

class BibleFragment : Fragment() {

    private lateinit var bibleRepo: BibleRepository
    private lateinit var bookPager: ViewPager2
    private lateinit var chaptersRecycler: RecyclerView
    private lateinit var chaptersTopBar: View
    private lateinit var selectedBookTitle: TextView

    private var testament: String = "Old"
    private var currentBook: String? = null
    private var inChaptersView = false
    private var inVersesView = false
    private lateinit var books: List<String>

    private lateinit var btnSwitchMode: Button
    private var isCardMode = true
    private lateinit var bookList: android.widget.ListView

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

        books = bibleRepo.getBooks(testament)

        // Book pager setup
        bookPager.adapter = BookPagerAdapter(books) { bookName ->
            showChapters(bookName)
        }

        // List mode setup
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
        chaptersTopBar.visibility = View.VISIBLE
        chaptersRecycler.visibility = View.VISIBLE

        chaptersRecycler.layoutManager = GridLayoutManager(requireContext(), 6)

        val count = bibleRepo.getChapterCount(bookName, testament)
        chaptersRecycler.adapter = ChapterAdapter((1..count).toList()) { chapter ->
            showVerses(bookName, chapter)
        }

        btnSwitchMode.visibility = View.GONE

        bookList.visibility = View.GONE
    }

    private fun showVerses(bookName: String, chapter: Int) {
        inChaptersView = false
        inVersesView = true

        selectedBookTitle.text = "$bookName $chapter"
        chaptersRecycler.layoutManager = LinearLayoutManager(requireContext())

        val verses = bibleRepo.getVerses(bookName, testament, chapter)
        val adapter = VerseAdapter(verses)
        chaptersRecycler.adapter = adapter

        chaptersRecycler.post {
            val vh = chaptersRecycler.findViewHolderForAdapterPosition(0) as? VerseAdapter.VH
            vh?.textView?.text = adapter.buildSpannable(requireContext())
        }
        btnSwitchMode.visibility = View.GONE
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