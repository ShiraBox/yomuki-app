package me.urfate.yomuki.ui.fragment.home.updates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionInflater
import me.urfate.yomuki.R
import me.urfate.yomuki.adapter.UpdatesAdapter
import me.urfate.yomuki.databinding.FragmentUpdatedReleasesBinding

class UpdatesFragment : Fragment() {
    private var binding: FragmentUpdatedReleasesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdatedReleasesBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        val updatesViewModel: UpdatesViewModel = ViewModelProvider(this)[UpdatesViewModel::class.java]

        val transitionInflater = TransitionInflater.from(requireContext())
        enterTransition = transitionInflater.inflateTransition(R.transition.slide_right)
        exitTransition = transitionInflater.inflateTransition(R.transition.fade)
        returnTransition = transitionInflater.inflateTransition(R.transition.fade)

        val toolbar = root.findViewById<Toolbar>(R.id.updates_toolbar)
        val updatesProgressBar = binding!!.updatedBooksProgressBar

        updatesViewModel.getUpdates().observe(viewLifecycleOwner) {
            val updatesRecycler = binding!!.updatedReleasesRecycler
            updatesRecycler.layoutManager = GridLayoutManager(context, 3)
            val updatesAdapter = UpdatesAdapter(root.context, it, true)
            updatesRecycler.adapter = updatesAdapter

            updatesProgressBar.visibility = View.GONE
        }

        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}