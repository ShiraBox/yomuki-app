package me.urfate.yomuki.ui.activity.book

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import coil.load
import coil.size.Size
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.*
import me.urfate.yomuki.R
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.db.entity.BookEntity
import me.urfate.yomuki.model.Status
import me.urfate.yomuki.source.ContentSource
import me.urfate.yomuki.source.SourceManager
import me.urfate.yomuki.ui.activity.ReaderActivity
import me.urfate.yomuki.ui.activity.chapters.ChaptersActivity
import me.urfate.yomuki.ui.activity.chapters.ChaptersViewModel
import me.urfate.yomuki.util.ViewUtil
import java.util.*

class BookActivity : AppCompatActivity() {
    private var bookUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        bookUrl = intent.getStringExtra("bookUrl")

        val manager = SourceManager.instance

        val contentSource = intent.getStringExtra("source")?.let { manager.fromUrl(it) }

        val appBarLayout = findViewById<AppBarLayout>(R.id.book_appbar_layout)
        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.book_appbar)
        val toolbar = findViewById<Toolbar>(R.id.selected_book_toolbar)
        val bookProgressBar = findViewById<ProgressBar>(R.id.selected_book_progress_bar)
        val chaptersProgressBar = findViewById<ProgressBar>(R.id.selected_book_chapters_progress_bar)
        val scrollView = findViewById<NestedScrollView>(R.id.selected_book_scroll_view)
        val title = findViewById<TextView>(R.id.selected_book_title)
        val cover = findViewById<ImageView>(R.id.selected_book_cover)
        val coverBackground = findViewById<ImageView>(R.id.selected_book_cover_background)
        val releaseYear = findViewById<TextView>(R.id.selected_book_release_year)
        val rating = findViewById<RatingBar>(R.id.selected_book_rating_bar)
        val status = findViewById<TextView>(R.id.selected_book_status_text)
        val description = findViewById<TextView>(R.id.selected_book_description)
        val fab = findViewById<ExtendedFloatingActionButton>(R.id.read_start_button)
        val chaptersLayout = findViewById<LinearLayout>(R.id.chapter_list_layout)
        val chaptersListButtonLayout = findViewById<LinearLayout>(R.id.chapters_list_button)
        val chaptersListButton = findViewById<TextView>(R.id.book_chapters_list_button)
        val descriptionLabel = findViewById<TextView>(R.id.description_label)
        val noChaptersLabel = findViewById<TextView>(R.id.no_chapters_label)

        val bookViewModel: BookViewModel = ViewModelProvider(this)[BookViewModel::class.java]
        val chaptersViewModel: ChaptersViewModel = ViewModelProvider(this)[ChaptersViewModel::class.java]

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val excludeExplicit = sharedPreferences.getBoolean("explicit_content", true)

        appBarLayout.visibility = View.GONE
        scrollView.visibility = View.GONE
        fab.visibility = View.GONE
        bookProgressBar.visibility = View.VISIBLE
        chaptersListButton.visibility = View.GONE

        bookViewModel.getBook(contentSource!!, bookUrl!!).observe(this) {
            toolbar.title = it!!.title
            title.text = it.title
            releaseYear.text = it.releaseDate
            rating.rating = it.rating
            status.text = getString(it.status.toInt())
            description.text = it.description
            descriptionLabel.visibility = if (it.description.isEmpty()) View.GONE else View.VISIBLE
            description.visibility = if (it.description.isEmpty()) View.GONE else View.VISIBLE
            releaseYear.visibility = if (it.releaseDate.isEmpty()) View.GONE else View.VISIBLE

            cover.load(it.coverUrl) {
                crossfade(true)
                addHeader("User-Agent", ContentSource.userAgent)
            }
            coverBackground.load(it.coverUrl) {
                crossfade(true)
                size(Size.ORIGINAL)
                addHeader("User-Agent", ContentSource.userAgent)
            }

            appBarLayout.visibility = View.VISIBLE
            scrollView.visibility = View.VISIBLE
            bookProgressBar.visibility = View.GONE
            setSupportActionBar(toolbar)
            fab.visibility = View.VISIBLE

            // Care about description
            if (description.text.toString().isEmpty()) {
                description.visibility = View.GONE
            }

            // Manga status
            if (it.status === Status.ONGOING) {
                status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_access_time_24, 0, 0, 0)
            } else {
                status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_done_all_24, 0, 0, 0)
            }

            val explicitGenres: Collection<String> = contentSource
                .explicitGenres().map { s -> s.lowercase(Locale.getDefault()) }
            val bookGenres: Collection<String> = it.genres
                .map { s -> s.lowercase(Locale.getDefault()) }

            if (excludeExplicit && bookGenres.any { s -> explicitGenres.contains(s)}) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.explicit_content_title))
                    .setMessage(getString(R.string.explicit_content_message))
                    .setIcon(R.drawable.outline_explicit_24)
                    .setPositiveButton(getString(R.string.yes)) { _, _ -> }
                    .setNegativeButton(getString(R.string.no)) { _, _ -> finish() }
                    .show()
            }

            bookViewModel.getBookEntity(bookUrl!!).observe(this) { entity ->
                if (entity == null) {
                    val newBookEntity = BookEntity(
                        it.title,
                        it.description,
                        it.releaseDate,
                        it.bookUrl,
                        it.coverUrl,
                        0,
                        "",
                        -1,
                        -1,
                        -1,
                        it.source)
                    bookViewModel.insertBookEntity(newBookEntity)
                }
            }
        }

        chaptersViewModel.getChapters(contentSource, bookUrl!!).observe(this) {
            it.subList(0, it.size.coerceAtMost(8)).forEach { chapter ->
                val chapterCardView = ViewUtil.getChapterCardView(chapter, LayoutInflater.from(chaptersLayout.context), null) {
                    val intent = Intent(this@BookActivity, ReaderActivity::class.java).apply {
                        putExtra("chapterTitle", String.format(resources.getString(R.string.chapter_toolbar_text), chapter.volume, chapter.chapterNumber))
                        putExtra("chapterUrl", chapter.chapterUrl)
                        putExtra("bookUrl", bookUrl)
                        putExtra("source", contentSource.url)
                    }
                    startActivity(intent)
                }
                chaptersLayout.addView(chapterCardView)
            }

            fab.setOnClickListener { _ ->
                val firstChapter = it.last()

                val intent = Intent(this@BookActivity, ReaderActivity::class.java).apply {
                    putExtra("chapterTitle", String.format(resources.getString(R.string.chapter_toolbar_text), firstChapter.volume, firstChapter.chapterNumber))
                    putExtra("chapterUrl", firstChapter.chapterUrl)
                    putExtra("bookUrl", bookUrl)
                    putExtra("source", contentSource.url)
                }
                startActivity(intent)
            }

            chaptersProgressBar.visibility = View.GONE

            if(it.isNotEmpty()){
                chaptersListButton.visibility = View.VISIBLE
                chaptersListButtonLayout.isClickable = true
                return@observe
            }
            noChaptersLabel.visibility = View.VISIBLE
            fab.visibility = View.GONE
        }

        chaptersListButtonLayout.setOnClickListener {
            val intent = Intent(this@BookActivity, ChaptersActivity::class.java)
            intent.putExtra("bookUrl", bookUrl)
            intent.putExtra("source", contentSource.url)
            startActivity(intent)
        }

        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.transparent))
        toolbar.setNavigationOnClickListener { finishAndRemoveTask() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.book_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val addToFavouritesItem = menu.findItem(R.id.action_favorite)

        lifecycleScope.launch{
            val bookEntity: BookEntity? = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@BookActivity)!!.booksDao()!!.findByUrl(bookUrl)
            }

            if (bookEntity != null && bookEntity.favorite == 1) {
                addToFavouritesItem.icon = ContextCompat.getDrawable(this@BookActivity, R.drawable.ic_baseline_favorite_24)
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_favorite) {

            lifecycleScope.launch{
                val bookEntity: BookEntity? = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(this@BookActivity)!!.booksDao()!!.findByUrl(bookUrl)
                }

                withContext(Dispatchers.Main) {
                    val isFavorite = bookEntity?.favorite ?: 0
                    if (isFavorite == 0) {
                        bookEntity?.favorite = 1

                        lifecycleScope.launch {
                            AppDatabase.getInstance(this@BookActivity)!!.booksDao()!!.updateBook(bookEntity)
                        }

                        item.setIcon(R.drawable.ic_baseline_favorite_24)
                        Toast.makeText(this@BookActivity, R.string.added_to_favourites, Toast.LENGTH_SHORT).show()
                    } else {
                        bookEntity?.favorite = 0

                        lifecycleScope.launch {
                            AppDatabase.getInstance(this@BookActivity)!!.booksDao()!!.updateBook(bookEntity)
                        }

                        item.setIcon(R.drawable.ic_baseline_favorite_border_24)
                        Toast.makeText(this@BookActivity, R.string.removed_from_favourites, Toast.LENGTH_SHORT).show()
                    }
                }
            }


        } else if (itemId == R.id.action_share) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, bookUrl)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
        return super.onOptionsItemSelected(item)
    }
}