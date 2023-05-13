package me.urfate.yomuki.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.search.SearchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.urfate.yomuki.R
import me.urfate.yomuki.adapter.SearchAdapter
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.source.ContentSource
import me.urfate.yomuki.source.SourceManager

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchView = findViewById<SearchView>(R.id.search_view)
        val resultsLayout = findViewById<LinearLayout>(R.id.search_results_layout)
        val searchIndicator = findViewById<LinearProgressIndicator>(R.id.search_indicator)

        searchIndicator.setVisibilityAfterHide(View.GONE)
        searchView.requestFocusAndShowKeyboard()
        searchView.show()

        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.ifEmpty { return }

                val sources: MutableList<ContentSource> = SourceManager.instance.sources() as MutableList<ContentSource>

                resultsLayout.removeAllViews().let {
                    sources.forEach { resultsLayout.addView(resultCard(it.name)) }
                }
                searchIndicator.show()

                lifecycleScope.launch {
                    sources.forEach {
                        val results: List<Book> = withContext(Dispatchers.IO) {
                            it.search(s.toString())
                        }
                        val card = resultsLayout.findViewWithTag<View>(it.name)

                        if (results.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                val adapter: SearchAdapter = resultsLayout.findViewWithTag<View>(it.name)
                                    .findViewById<RecyclerView>(R.id.results_recycler)
                                    .adapter as SearchAdapter

                                adapter.setResults(results)
                                card.visibility = View.VISIBLE
                            }
                            return@forEach
                        }
                        card.visibility = View.GONE
                    }.let {
                        searchIndicator.hide()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }
        })

        searchView.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun resultCard(source: String) : View {
        val card = layoutInflater.inflate(R.layout.item_search_result, null)
        val title = card.findViewById<TextView>(R.id.source_name)
        val recyclerView = card.findViewById<RecyclerView>(R.id.results_recycler)
        val adapter = SearchAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        title.text = source

        card.visibility = View.GONE
        card.tag = source

        return card
    }
}