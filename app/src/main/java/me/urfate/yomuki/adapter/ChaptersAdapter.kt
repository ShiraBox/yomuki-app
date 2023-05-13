package me.urfate.yomuki.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import me.urfate.yomuki.R
import me.urfate.yomuki.model.Chapter
import me.urfate.yomuki.ui.activity.ReaderActivity

class ChaptersAdapter(context: Context?, chapters: List<Chapter>, bookUrl: String, source: String) :
    RecyclerView.Adapter<ChaptersAdapter.ViewHolder>() {
    private val inflater: LayoutInflater
    private var chapters: List<Chapter>
    private val bookUrl: String
    private val source: String

    init {
        inflater = LayoutInflater.from(context)
        this.chapters = chapters
        this.bookUrl = bookUrl
        this.source = source
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_chapter_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (title, volume, number, releaseDate, url, _) = chapters[position]

        holder.title.text = title
        holder.volume.text = volume.toString()
        holder.chapterNumber.text =
            if (number != -1) number.toString() else "[Extra]"
        holder.releaseDate.text = releaseDate

        holder.cardView.setOnClickListener {
            val intent = Intent(inflater.context, ReaderActivity::class.java).apply {
                putExtra("chapterTitle", String.format(inflater.context.
                getString(R.string.chapter_toolbar_text), volume, number))
                putExtra("chapterUrl", url)
                putExtra("bookUrl", bookUrl)
                putExtra("source", source)
            }
            inflater.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chapters.size
    }

    class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView
        val volume: TextView
        val chapterNumber: TextView
        val releaseDate: TextView
        val title: TextView

        init {
            cardView = view.findViewById(R.id.chapter_card)
            volume = view.findViewById(R.id.chapter_volume)
            chapterNumber = view.findViewById(R.id.chapter_number)
            releaseDate = view.findViewById(R.id.chapter_release_date)
            title = view.findViewById(R.id.chapter_title)
        }
    }
}