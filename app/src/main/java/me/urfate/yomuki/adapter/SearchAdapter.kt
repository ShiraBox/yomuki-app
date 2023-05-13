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

class SearchAdapter(context: Context) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private val inflater: LayoutInflater
    private val context: Context
    private var results: MutableList<Book>

    init {
        inflater = LayoutInflater.from(context)
        this.context = context
        results = ArrayList()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = inflater.inflate(R.layout.item_release_card, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (title, _, _, coverUrl, _, _, _, bookUrl, _, _, _, _, source) = results[position]
        holder.volume.visibility = View.GONE

        holder.title.text = title

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

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return results.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setResults(results: List<Book>) {
        this.results.clear()
        this.results.addAll(results)
        notifyDataSetChanged()
    }

    fun addResults(results: List<Book>) {
        var pos = this.results.size - 1
        for (i in results.indices) {
            this.results.add(results[i])
            pos++
            notifyItemInserted(pos)
        }
    }

    class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView
        val coverView: ImageView
        val title: TextView
        val volume: TextView

        init {
            cardView = view.findViewById(R.id.release_card_view)
            coverView = view.findViewById(R.id.item_image)
            title = view.findViewById(R.id.release_name)
            volume = view.findViewById(R.id.release_volume)
        }
    }
}