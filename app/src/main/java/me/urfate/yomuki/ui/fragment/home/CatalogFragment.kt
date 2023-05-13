package me.urfate.yomuki.ui.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.google.android.material.search.SearchBar
import me.urfate.yomuki.R
import me.urfate.yomuki.adapter.UpdatesAdapter
import me.urfate.yomuki.databinding.FragmentCatalogBinding
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.source.SourceManager
import me.urfate.yomuki.ui.activity.SearchActivity
import me.urfate.yomuki.ui.activity.book.BookActivity
import me.urfate.yomuki.util.ViewUtil
import java.util.*
import java.util.function.Consumer

class CatalogFragment : Fragment() {
    private var binding: FragmentCatalogBinding? = null

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        val transitionInflater = TransitionInflater.from(requireContext())
        enterTransition = transitionInflater.inflateTransition(R.transition.fade)
        exitTransition = transitionInflater.inflateTransition(R.transition.fade)

        val catalogViewModel = ViewModelProvider(this)[CatalogViewModel::class.java]
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(root.context)

        val excludeExplicit = sharedPreferences.getBoolean("explicit_content", true)

        val updatesIndicator = binding!!.updatesProgressBar
        val popularsIndicator = binding!!.popularsProgressBar
        val updatesListText = binding!!.updatesListText
        val updatesListLayout = binding!!.fullUpdatesListButton
        val searchBar: SearchBar = binding!!.materialSearchBar

        updatesIndicator.setVisibilityAfterHide(View.GONE)
        popularsIndicator.setVisibilityAfterHide(View.GONE)

        catalogViewModel.getUpdates().observe(viewLifecycleOwner) { updates: List<Book> ->
            updatesIndicator.hide()
            updatesListText.visibility = View.VISIBLE
            updatesListLayout.isClickable = true
            updatesListLayout.isFocusable = true

            val explicitGenres: Collection<String> = SourceManager.instance.fromUrl(updates[0].source)
                ?.explicitGenres()?.map { it.lowercase(Locale.getDefault()) } ?: emptyList()

            var filteredUpdates: List<Book> = updates

            if(excludeExplicit) {
                filteredUpdates = updates.filter { book: Book ->
                    val bookGenres: Collection<String> = book.genres
                        .map { it.lowercase(Locale.getDefault()) }
                    !bookGenres.containsAll(explicitGenres)
                }
            }

            val updatesRecycler = binding!!.updatesRecycler
            updatesRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val updatesAdapter = UpdatesAdapter(root.context, filteredUpdates, false)
            updatesRecycler.adapter = updatesAdapter
        }
        catalogViewModel.getPopulars().observe(viewLifecycleOwner) { populars: List<Book> ->
            popularsIndicator.hide()
            val popularsListLayout = binding!!.popularsLayout

            val explicitGenres: List<String>? = populars.firstOrNull()?.let { book ->
                SourceManager.instance.fromUrl(book.source)
                    ?.explicitGenres()?.map { it.lowercase(Locale.getDefault()) } ?: emptyList()
            }

            var filteredPopulars: List<Book> = populars

            if(excludeExplicit) {
                explicitGenres?.let {
                    filteredPopulars = filteredPopulars.filter { book: Book ->
                        val bookGenres: Collection<String> = book.genres
                            .map { it.lowercase(Locale.getDefault()) }
                        !bookGenres.any(explicitGenres::contains)
                    }
                }
            }

            filteredPopulars.subList(0, filteredPopulars.size.coerceAtMost(9))
                .forEach(Consumer { book: Book ->
                    popularsListLayout.addView(
                        ViewUtil.getBookCardView(
                            book,
                            inflater,
                            null
                        ) {
                            val intent = Intent(inflater.context, BookActivity::class.java).apply {
                                putExtra("source", book.source)
                                putExtra("bookUrl", book.bookUrl)
                            }

                            inflater.context.startActivity(intent)
                        })
            })
        }
        updatesListLayout.setOnClickListener {
            val updatesFragment: Fragment = parentFragmentManager.findFragmentByTag("updates")!!
            val catalogFragment: Fragment = parentFragmentManager.findFragmentByTag("catalog")!!

            parentFragmentManager.beginTransaction().apply {
                hide(catalogFragment)
                show(updatesFragment)
                addToBackStack(null)
                commit()
            }
        }

        val intent = Intent(inflater.context, SearchActivity::class.java)
        searchBar.setOnClickListener { inflater.context.startActivity(intent) }
        searchBar.setNavigationOnClickListener { inflater.context.startActivity(intent) }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}