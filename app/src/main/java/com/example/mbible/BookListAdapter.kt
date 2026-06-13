package com.example.mbible

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/** OT/NT book list rows: badge + name + "N chapters" + chevron. */
class BookListAdapter(
    context: Context,
    private val rows: List<BookRow>
) : ArrayAdapter<BookListAdapter.BookRow>(context, 0, rows) {

    data class BookRow(val name: String, val abbrev: String, val chapters: Int)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_book_row, parent, false)

        val row = rows[position]
        view.findViewById<TextView>(R.id.bookAbbrev).text = row.abbrev
        view.findViewById<TextView>(R.id.bookName).text = row.name
        view.findViewById<TextView>(R.id.bookChapters).text = "${row.chapters} chapters"
        return view
    }
}