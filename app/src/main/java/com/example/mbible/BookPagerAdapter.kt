package com.example.mbible

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class BookPagerAdapter(
    private val books: List<String>,
    private val onBookSelected: (String) -> Unit
) : RecyclerView.Adapter<BookPagerAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookCard: CardView = itemView.findViewById(R.id.bookCard)
        val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
        val bookName: TextView = itemView.findViewById(R.id.bookName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_book_page, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val book = books[position]
        holder.bookName.text = book

        // Placeholder — later swap with real book art
        holder.bookImage.setImageResource(android.R.drawable.ic_menu_agenda)

        holder.bookCard.setOnClickListener {
            onBookSelected(book)
        }
    }

    override fun getItemCount() = books.size
}