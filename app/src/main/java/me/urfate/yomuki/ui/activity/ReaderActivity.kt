package me.urfate.yomuki.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.urfate.yomuki.R
import me.urfate.yomuki.adapter.pages.PagesAdapter
import me.urfate.yomuki.source.SourceManager
import me.urfate.yomuki.ui.activity.chapters.ChaptersActivity


class ReaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        val manager = SourceManager.instance

        val toolbar = findViewById<Toolbar>(R.id.reader_toolbar)
        val viewPager = findViewById<ViewPager2>(R.id.chapter_view_pager)
        val chapterPager = findViewById<TextView>(R.id.chapter_pager)
        val chapterUrl = intent.getStringExtra("chapterUrl")
        val contentSource = intent.getStringExtra("source")?.let { manager.fromUrl(it) }

        toolbar.title = intent.getStringExtra("chapterTitle")
        setSupportActionBar(toolbar)
        toolbar.showOverflowMenu()

        lifecycleScope.launch {
            val pages: List<String> = withContext(Dispatchers.IO) {
                contentSource?.fetchChapterPages(chapterUrl!!) ?: emptyList()
            }

            withContext(Dispatchers.Main){
                viewPager.adapter =
                    PagesAdapter(this@ReaderActivity, toolbar, chapterPager,
                        supportActionBar!!, window, contentSource?.url,
                        pages as MutableList<String>
                    )
                chapterPager.text =
                    String.format(resources.getString(R.string.chapter_pager), 1, pages.size)

                viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        chapterPager.text = String.format(
                            resources.getString(R.string.chapter_pager),
                            position + 1,
                            pages.size
                        )
                    }
                })
            }
        }

        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reader_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_show_chapters) {
            val intent = Intent(this, ChaptersActivity::class.java)
            intent.putExtra("bookUrl", getIntent().getStringExtra("bookUrl"))
            intent.putExtra("source", getIntent().getStringExtra("source"))
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}