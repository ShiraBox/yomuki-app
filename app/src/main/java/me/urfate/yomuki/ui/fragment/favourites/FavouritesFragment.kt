package me.urfate.yomuki.ui.fragment.favourites

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import me.urfate.yomuki.R
import me.urfate.yomuki.adapter.FavouritesAdapter
import me.urfate.yomuki.databinding.FragmentFavouritesBinding

class FavouritesFragment : Fragment() {
    private var binding: FragmentFavouritesBinding? = null

    private var favouritesAdapter: FavouritesAdapter? = null
    private var favouritesViewModel: FavouritesViewModel? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transitionInflater = TransitionInflater.from(requireContext())
        enterTransition = transitionInflater.inflateTransition(R.transition.fade)
        exitTransition = transitionInflater.inflateTransition(R.transition.fade)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        favouritesViewModel = ViewModelProvider(this)[FavouritesViewModel::class.java]

        favouritesViewModel!!.getFavourites().observe(viewLifecycleOwner) {
            it.ifEmpty {
                binding!!.favouritesEmpty.visibility = View.VISIBLE
            }

            val favouritesRecyclerView = binding!!.favouritesRecyclerView

            favouritesAdapter = FavouritesAdapter(inflater.context, it)
            favouritesRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            favouritesRecyclerView.adapter = favouritesAdapter
        }

        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        // Update the favourites list if the user has changed the favourites list
        favouritesViewModel?.getFavourites()?.observe(viewLifecycleOwner) {
            if((favouritesAdapter?.itemCount ?: 0) != it.size) {
                favouritesAdapter?.notifyDataSetChanged()
            }
            if (it.isEmpty()) {
                binding?.favouritesEmpty?.visibility = View.VISIBLE
            } else {
                binding?.favouritesEmpty?.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}