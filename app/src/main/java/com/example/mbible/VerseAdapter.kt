package com.example.mbible

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mbible.data.Verse

class VerseAdapter(
    private val verses: List<Verse>
) : RecyclerView.Adapter<VerseAdapter.VH>() {

    class VH(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verse, parent, false) as TextView
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        // Only one item — the whole chapter as one paragraph
    }

    override fun getItemCount() = 1

    fun buildSpannable(context: android.content.Context): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()
        val numColor = context.getColor(R.color.text_secondary)

        for ((index, verse) in verses.withIndex()) {
            val numStr = "${verse.verse}"
            val numStart = spannable.length

            spannable.append(numStr)
            spannable.setSpan(SuperscriptSpan(), numStart, numStart + numStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(RelativeSizeSpan(0.65f), numStart, numStart + numStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(numColor), numStart, numStart + numStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannable.append(" ")
            spannable.append(verse.text)

            if (index < verses.size - 1) spannable.append("\n\n")
        }

        return spannable
    }
}