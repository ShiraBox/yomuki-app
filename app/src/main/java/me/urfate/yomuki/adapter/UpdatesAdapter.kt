package me.urfate.yomuki.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import me.urfate.yomuki.R
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.source.ContentSource.Companion.userAgent
import me.urfate.yomuki.ui.activity.book.BookActivity
import kotlin.math.min

class UpdatesAdapter(context: Context?, updatedBooks: List<Book>, fullList: Boolean) :
    RecyclerView.Adapter<UpdatesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater
    private var updatedBooks: List<Book>
    private val fullList: Boolean

    init {
        inflater = LayoutInflater.from(context)
        this.updatedBooks = updatedBooks
        this.fullList = fullList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_release_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!fullList) updatedBooks = updatedBooks.subList(0, min(updatedBooks.size, 9))
        val (title, _, _, coverUrl, _, _, _, bookUrl, _, _, volume, chapter, source) = updatedBooks[position]
        holder.name.text = title

        holder.coverView.load(coverUrl) {
            crossfade(true)
            addHeader("User-Agent", userAgent)
        }

        if (volume == -1) {
            holder.volume.visibility = View.GONE
        } else {
            holder.volume.visibility = View.VISIBLE
            holder.volume.text = String.format("Том %d", volume)
        }
        if (chapter == -1) {
            holder.chapter.visibility = View.GONE
        } else {
            holder.chapter.visibility = View.VISIBLE
            holder.chapter.text = String.format("Глава %d", chapter)
        }
        holder.cardView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookActivity::class.java).apply {
                putExtra("source", source)
                putExtra("bookUrl", bookUrl)
            }

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return updatedBooks.size
    }

    class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView
        val coverView: ImageView
        val name: TextView
        val volume: TextView
        val chapter: TextView

        init {
            cardView = view.findViewById(R.id.release_card_view)
            coverView = view.findViewById(R.id.item_image)
            name = view.findViewById(R.id.release_name)
            volume = view.findViewById(R.id.release_volume)
            chapter = view.findViewById(R.id.release_chapter)
        }
    }
}