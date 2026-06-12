package com.example.mbible

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
import android.widget.ImageButton
import android.widget.TextView
import com.example.mbible.data.Verse
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Slide-up sheet that displays a reference and its verses.
 * Replaces the old AlertDialog popups. Pure presentation — callers fetch
 * the verses (via BibleRepository.getVerseRange) and hand them in.
 */
object VerseSheet {

    fun show(context: Context, reference: String, verses: List<Verse>) {
        val dialog = BottomSheetDialog(context)
        dialog.setContentView(R.layout.sheet_verse)

        dialog.findViewById<TextView>(R.id.sheetReference)?.text = reference

        val body = dialog.findViewById<TextView>(R.id.sheetVerses)
        body?.text = buildVerses(context, verses)

        dialog.findViewById<ImageButton>(R.id.sheetClose)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun buildVerses(context: Context, verses: List<Verse>): SpannableStringBuilder {
        val sb = SpannableStringBuilder()
        val numColor = context.getColor(R.color.accent_red)

        for ((index, verse) in verses.withIndex()) {
            val numStr = "${verse.verse}"
            val start = sb.length
            sb.append(numStr)
            val end = start + numStr.length
            sb.setSpan(SuperscriptSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(RelativeSizeSpan(0.62f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(ForegroundColorSpan(numColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.append(" ")
            sb.append(verse.text)
            if (index < verses.size - 1) sb.append("\n\n")
        }
        return sb
    }
}
