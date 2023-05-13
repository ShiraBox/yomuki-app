package me.urfate.yomuki.util

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import coil.Coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageRequest.Builder
import com.google.android.flexbox.FlexboxLayout
import me.urfate.yomuki.R
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.model.Chapter
import me.urfate.yomuki.source.ContentSource
import me.urfate.yomuki.source.ContentSource.Companion.userAgent

object ViewUtil {
    @SuppressLint("DefaultLocale")
    fun getBookCardView(
        book: Book,
        inflater: LayoutInflater,
        root: ViewGroup?,
        onClickListener: View.OnClickListener?
    ): View {
        val coverView: ImageView
        val title: TextView
        val description: TextView
        val bookInfo: TextView
        val genresLayout: FlexboxLayout
        val bookCardView = inflater.inflate(R.layout.item_book_card, root)
        coverView = bookCardView.findViewById(R.id.item_image)
        title = bookCardView.findViewById(R.id.book_title)
        description = bookCardView.findViewById(R.id.book_description)
        bookInfo = bookCardView.findViewById(R.id.book_info)
        genresLayout = bookCardView.findViewById(R.id.genres_layout)
        title.text = book.title
        description.text = book.description
        bookInfo.text = book.releaseDate

        if(book.description.isEmpty()) {
            description.text = inflater.context.resources.getString(R.string.no_descrription)
            if(book.genres.isNotEmpty()) description.visibility = View.GONE
        }

        book.genres.subList(0, book.genres.size.coerceAtMost(3)).forEach {
            val badge = inflater.inflate(R.layout.item_badge, root)
            badge.findViewById<TextView>(R.id.badge_text).apply {
                text = it.replaceFirstChar { it.uppercase() }
            }
            genresLayout.addView(badge)
        }

        val imageLoader = imageLoader(inflater.context)
        val request: ImageRequest = Builder(inflater.context)
            .data(book.coverUrl)
            .crossfade(true)
            .addHeader("User-Agent", userAgent)
            .target(coverView)
            .build()
        imageLoader.enqueue(request)
        if (book.releaseDate.length != 4) bookInfo.visibility = View.GONE
        bookCardView.findViewById<View>(R.id.item_book_card_view)
            .setOnClickListener(onClickListener)
        return bookCardView
    }

    fun getChapterCardView(
        chapter: Chapter,
        inflater: LayoutInflater,
        root: ViewGroup?,
        onClickListener: View.OnClickListener?
    ): View {
        val cardView: CardView
        val volumeNumber: TextView
        val chapterNumber: TextView
        val chapterName: TextView
        val chapterDate: TextView
        val chapterCardLayout = inflater.inflate(R.layout.item_chapter_card, root)
        cardView = chapterCardLayout.findViewById(R.id.chapter_card)
        volumeNumber = chapterCardLayout.findViewById(R.id.chapter_volume)
        chapterNumber = chapterCardLayout.findViewById(R.id.chapter_number)
        chapterName = chapterCardLayout.findViewById(R.id.chapter_title)
        chapterDate = chapterCardLayout.findViewById(R.id.chapter_release_date)
        volumeNumber.text = chapter.volume.toString()
        chapterNumber.text =
            if (chapter.chapterNumber != -1) chapter.chapterNumber.toString() else "[Extra]"
        chapterName.text = chapter.name
        chapterDate.text = chapter.releaseDate
        cardView.setOnClickListener(onClickListener)
        return chapterCardLayout
    }

    fun getSourceCardView(
        source: ContentSource,
        inflater: LayoutInflater,
        root: ViewGroup?,
        onClickListener: View.OnClickListener?
    ): View {
        val sourceLayout = inflater.inflate(R.layout.item_source, root)
        val sourceCardView = sourceLayout.findViewById<CardView>(R.id.source_item_card)
        val sourceText = sourceLayout.findViewById<TextView>(R.id.source_item_name)
        val sourceLink = sourceLayout.findViewById<TextView>(R.id.source_item_url)
        val sourceImage = sourceLayout.findViewById<ImageView>(R.id.source_item_icon)
        sourceText.text = source.name
        sourceLink.text = source.url
        val imageLoader = imageLoader(inflater.context)
        val request: ImageRequest = Builder(inflater.context)
            .data(source.iconUrl)
            .crossfade(true)
            .target(sourceImage)
            .addHeader("User-Agent", userAgent)
            .build()
        imageLoader.enqueue(request)
        sourceCardView.setOnClickListener(onClickListener)
        return sourceLayout
    }
}