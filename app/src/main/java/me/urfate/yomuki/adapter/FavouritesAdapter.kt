package me.urfate.yomuki.adapter

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

class FavouritesAdapter(context: Context, favourites: List<Book>) :
    RecyclerView.Adapter<FavouritesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater
    private val favourites: List<Book>
    private val context: Context

    init {
        inflater = LayoutInflater.from(context)
        this.context = context
        this.favourites = favourites
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_book_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (title, _, description, coverUrl, releaseDate, _, _, bookUrl, _, _, _, _, source) = favourites[position]
        holder.title.text = title
        holder.description.text =
            description.ifEmpty { context.getString(R.string.no_descrription) }
        holder.releaseYear.text = releaseDate
        if (releaseDate.isEmpty()) holder.releaseYear.visibility = View.GONE

        holder.coverView.load(coverUrl) {
            crossfade(true)
            addHeader("User-Agent", userAgent)
        }

        holder.cardView.setOnClickListener {
            val intent = Intent(inflater.context, BookActivity::class.java)
            intent.putExtra("source", source)
            intent.putExtra("bookUrl", bookUrl)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return favourites.size
    }

    class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView
        val coverView: ImageView
        val title: TextView
        val description: TextView
        val releaseYear: TextView

        init {
            cardView = view.findViewById(R.id.item_book_card_view)
            coverView = view.findViewById(R.id.item_image)
            title = view.findViewById(R.id.book_title)
            description = view.findViewById(R.id.book_description)
            releaseYear = view.findViewById(R.id.book_info)
        }
    }
}