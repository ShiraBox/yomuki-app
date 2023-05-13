package me.urfate.yomuki.ui.fragment.profile

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.transition.TransitionInflater
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.urfate.yomuki.R
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.source.SourceManager


class PreferencesCatalogFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        screen.setTitle(R.string.preferences)

        val transitionInflater = TransitionInflater.from(requireContext())
        enterTransition = transitionInflater.inflateTransition(R.transition.slide_right)
        exitTransition = transitionInflater.inflateTransition(R.transition.slide_right)

        lifecycleScope.launch{

            val sources = SourceManager.instance.sources()

            sources.forEach {
                val preference = Preference(context).apply {
                    key = it.url
                    title = it.name
                    summary = it.url
                    icon = withContext(Dispatchers.IO){ fetchIcon(it.iconUrl) }
                    onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
                        lifecycleScope.launch { setDefaultSource(it.url) }
                        true
                    }
                }
                withContext(Dispatchers.Main){ screen.addPreference(preference) }
            }

            withContext(Dispatchers.Main){
                preferenceScreen = screen
            }
        }
    }

    private suspend fun setDefaultSource(source: String) = withContext(Dispatchers.IO){
        val entity = AppDatabase.getInstance(requireContext())!!.sourceDao()!!
            .findByUrl(source)

        val entities = AppDatabase.getInstance(requireContext())!!.sourceDao()!!.all
        entities?.forEach {
            it?.isDefault = 0
            AppDatabase.getInstance(requireContext())!!.sourceDao()!!.update(it!!)
        }

        entity!!.isDefault = 1
        AppDatabase.getInstance(requireContext())!!.sourceDao()!!.update(entity)

        relaunchApp()
    }

    private suspend fun relaunchApp() = withContext(Dispatchers.Main) {
        activity?.finish()
        startActivity(activity?.intent)
        activity?.overridePendingTransition(0, 0)
    }

    private suspend fun fetchIcon(url: String): Drawable? {
        val imageLoader = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext())
            .data(url)
            .size(64, 64)
            .build()

        return imageLoader.execute(request).drawable
    }
}