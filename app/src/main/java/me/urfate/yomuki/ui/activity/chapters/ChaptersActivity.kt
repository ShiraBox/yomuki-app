package me.urfate.yomuki.ui.activity.chapters

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import me.urfate.yomuki.R
import me.urfate.yomuki.adapter.ChaptersAdapter
import me.urfate.yomuki.source.SourceManager

class ChaptersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapters)

        val bookUrl = intent.getStringExtra("bookUrl")
        val contentSource = intent.getStringExtra("source")
            ?.let { SourceManager.instance.fromUrl(it) }
        val toolbar = findViewById<Toolbar>(R.id.chapters_toolbar)
        val chaptersRecyclerView = findViewById<RecyclerView>(R.id.chapters_recycler)
        val chaptersProgressBar = findViewById<CircularProgressIndicator>(R.id.book_chapters_progress_bar)

        chaptersRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        chaptersProgressBar.setVisibilityAfterHide(View.GONE)

        val chaptersViewModel: ChaptersViewModel =
            ViewModelProvider(this)[ChaptersViewModel::class.java]

        chaptersViewModel.getChapters(contentSource!!, bookUrl!!).observe(this) { chapterList ->
            chaptersRecyclerView.adapter = ChaptersAdapter(this, chapterList,
                bookUrl, contentSource.url)

            chaptersProgressBar.hide()
        }

        toolbar.setNavigationOnClickListener { finishAndRemoveTask() }
    }
}