package com.example.mbible

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
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
        val numColor = context.getColor(R.color.accent_red)

        for ((index, verse) in verses.withIndex()) {
            val numStr = "${verse.verse}"
            val numStart = spannable.length

            spannable.append(numStr)
            val numEnd = numStart + numStr.length
            spannable.setSpan(SuperscriptSpan(), numStart, numEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(RelativeSizeSpan(0.62f), numStart, numEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(numColor), numStart, numEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), numStart, numEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannable.append(" ")

            val textStart = spannable.length
            spannable.append(verse.text)

            // Raised initial on the first letter of verse 1.
            if (index == 0 && verse.text.isNotEmpty()) {
                spannable.setSpan(RelativeSizeSpan(2.0f), textStart, textStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(numColor), textStart, textStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            if (index < verses.size - 1) spannable.append("\n\n")
        }

        return spannable
    }
}
