package me.urfate.yomuki.ui.fragment.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.get
import androidx.transition.TransitionInflater
import me.urfate.yomuki.R

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val transitionInflater = TransitionInflater.from(requireContext())
        enterTransition = transitionInflater.inflateTransition(R.transition.fade)
        exitTransition = transitionInflater.inflateTransition(R.transition.fade)

        preferenceScreen.get<Preference>("default_source")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val catalogPreferencesFragment: Fragment = parentFragmentManager.findFragmentByTag("preferences_catalog")!!

                parentFragmentManager.beginTransaction().apply {
                    hide(this@PreferencesFragment)
                    show(catalogPreferencesFragment)
                    addToBackStack(null)
                    commit()
                }
                true
            }
    }

}