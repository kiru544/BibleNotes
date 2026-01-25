package com.example.mbible

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.InputType
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.io.File
import kotlin.math.abs
import com.github.chrisbanes.photoview.PhotoView



class CatechismActivity : ComponentActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var fileDescriptor: ParcelFileDescriptor
    private var currentPage: PdfRenderer.Page? = null

    private lateinit var pageImage: PhotoView
    private lateinit var pageIndicator: TextView
    private lateinit var detector: GestureDetector

    private var pageIndex = 0

    // swipe tuning
    private val SWIPE_DISTANCE = 120
    private val SWIPE_VELOCITY = 120

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catechism)

        pageImage = findViewById(R.id.pageImage)
        pageIndicator = findViewById(R.id.pageIndicator)

        // Copy PDF from assets -> cache (PdfRenderer needs a real file)
        val file = File(cacheDir, "catechism.pdf")
        if (!file.exists()) {
            assets.open("catechism.pdf").use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }

        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)

        // initial render
        showPage(0)

        // Tap page indicator to jump
        pageIndicator.setOnClickListener {
            showJumpDialog()
        }

        // Swipe up/down to change pages
        detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean = false

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                // Don't page flip if user is zoomed in
                if (pageImage.scale > 1.05f) return false

                val dy = e2.y - e1.y
                val ady = abs(dy)
                val avy = abs(velocityY)

                if (ady > SWIPE_DISTANCE && avy > SWIPE_VELOCITY && ady > abs(e2.x - e1.x)) {
                    if (dy < 0) nextPage() else prevPage()
                    return true
                }
                return false
            }
        })

    }

    private fun showJumpDialog() {
        val total = pdfRenderer.pageCount
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "1 - $total"
            setText((pageIndex + 1).toString())
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Go to page")
            .setView(input)
            .setPositiveButton("Go") { _, _ ->
                val raw = input.text.toString().trim()
                val page = raw.toIntOrNull()
                if (page != null) {
                    val idx = (page - 1).coerceIn(0, total - 1)
                    showPage(idx)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun nextPage() {
        if (pageIndex < pdfRenderer.pageCount - 1) {
            showPage(pageIndex + 1)
        }
    }

    private fun prevPage() {
        if (pageIndex > 0) {
            showPage(pageIndex - 1)
        }
    }

    private fun showPage(index: Int) {
        currentPage?.close()

        pageIndex = index
        val page = pdfRenderer.openPage(index)
        currentPage = page

        // Make text more readable: render at higher scale + white background bitmap
        val scale = 3
        val bitmap = Bitmap.createBitmap(
            page.width * scale,
            page.height * scale,
            Bitmap.Config.ARGB_8888
        )

        // Fill bitmap white so black PDF text is readable on dark UI
        bitmap.eraseColor(0xFFFFFFFF.toInt())

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        pageImage.setImageBitmap(bitmap)

        // "x/628"
        pageIndicator.text = "${index + 1}/${pdfRenderer.pageCount}"
    }

    override fun onDestroy() {
        currentPage?.close()
        pdfRenderer.close()
        fileDescriptor.close()
        super.onDestroy()
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Only consider swipe-to-change-page for 1-finger gestures
        if (ev.pointerCount == 1 && pageImage.scale <= 1.05f) {
            detector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }
}
