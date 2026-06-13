package com.example.mbible

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mbible.BookAliasRepository

/** Settings book rows: badge + name + existing short names shown as tags. */
class SettingsBookAdapter(
    context: Context,
    private val books: List<String>,
    private val aliasRepo: BookAliasRepository
) : ArrayAdapter<String>(context, 0, books) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_settings_book, parent, false)

        val book = books[position]
        view.findViewById<TextView>(R.id.bookAbbrev).text =
            book.filter { !it.isWhitespace() }.take(3)
        view.findViewById<TextView>(R.id.bookName).text = book

        val tagRow = view.findViewById<LinearLayout>(R.id.aliasTagRow)
        val noAlias = view.findViewById<TextView>(R.id.noAliasText)
        tagRow.removeAllViews()

        val aliases = aliasRepo.getAliasesForBook(book)
        if (aliases.isEmpty()) {
            tagRow.visibility = View.GONE
            noAlias.visibility = View.VISIBLE
        } else {
            tagRow.visibility = View.VISIBLE
            noAlias.visibility = View.GONE
            for (alias in aliases) {
                val chip = TextView(context).apply {
                    text = alias
                    setBackgroundResource(R.drawable.bg_chip_accent)
                    typeface = androidx.core.content.res.ResourcesCompat.getFont(
                        context, R.font.archivo_semibold
                    )
                    setTextColor(context.getColor(R.color.accent_red))
                    textSize = 12f
                    setPadding(22, 8, 22, 8)
                }
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 14 }
                tagRow.addView(chip, lp)
            }
        }
        return view
    }
}