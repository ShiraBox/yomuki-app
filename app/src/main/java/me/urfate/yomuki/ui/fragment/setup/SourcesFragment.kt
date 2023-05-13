package me.urfate.yomuki.ui.fragment.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.urfate.yomuki.R
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.db.entity.SourceEntity
import me.urfate.yomuki.source.SourceManager
import me.urfate.yomuki.ui.activity.MainActivity
import me.urfate.yomuki.util.ViewUtil

class SourcesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_sources, container, false)
        val sourcesLayout = root.findViewById<LinearLayout>(R.id.sources_layout)

        val sources = SourceManager.instance.sources()
        
        lifecycleScope.launch(Dispatchers.IO) {
            sources.forEach {
                AppDatabase.getInstance(context)!!.sourceDao()!!
                    .insertAll(SourceEntity(it, false))
            }
        }

        val cards: MutableList<View> = mutableListOf()

        SourceManager.instance.sources().forEach { source ->
            val cardView = ViewUtil.getSourceCardView(source, inflater, container) {
                lifecycleScope.launch(Dispatchers.IO){
                    val entity = AppDatabase.getInstance(context)!!.sourceDao()!!
                        .findByUrl(source.url)
                    entity!!.isDefault = 1
                    AppDatabase.getInstance(context)!!.sourceDao()!!.update(entity)
                }

                val intent = Intent(activity, MainActivity::class.java)
                activity?.finish()
                startActivity(intent)
                activity?.overridePendingTransition(0, 0)
            }
            cards.add(cardView)
        }

        cards.forEach { sourcesLayout.addView(it) }
        return root
    }
}