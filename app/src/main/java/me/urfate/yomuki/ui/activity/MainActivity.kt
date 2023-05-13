package me.urfate.yomuki.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking
import me.urfate.yomuki.R
import me.urfate.yomuki.databinding.ActivityMainBinding
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.db.entity.SourceEntity
import me.urfate.yomuki.ui.fragment.favourites.FavouritesFragment
import me.urfate.yomuki.ui.fragment.home.CatalogFragment
import me.urfate.yomuki.ui.fragment.home.updates.UpdatesFragment
import me.urfate.yomuki.ui.fragment.profile.PreferencesCatalogFragment
import me.urfate.yomuki.ui.fragment.profile.PreferencesFragment

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private val catalogFragment: Fragment = CatalogFragment()
    private val updatesFragment: Fragment = UpdatesFragment()
    private val favouritesFragment: Fragment = FavouritesFragment()
    private val preferencesFragment: Fragment = PreferencesFragment()
    private val preferencesCatalogFragment: Fragment = PreferencesCatalogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var entity: SourceEntity?

        runBlocking {
            entity = AppDatabase.getInstance(this@MainActivity)?.sourceDao()?.findDefault()
        }

        if(entity == null) {
            val intent = Intent(this@MainActivity, SetupActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment_activity_main, preferencesFragment, "preferences")
            hide(preferencesFragment)
            commit()
        }
        supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment_activity_main, preferencesCatalogFragment, "preferences_catalog")
            hide(preferencesCatalogFragment)
            commit()
        }
        supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment_activity_main, favouritesFragment, "favourites")
            hide(favouritesFragment)
            commit()
        }
        supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment_activity_main, updatesFragment, "updates")
            hide(updatesFragment)
            commit()
        }
        supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment_activity_main, catalogFragment, "catalog")
            commit()
        }

        navView.setOnItemReselectedListener { return@setOnItemReselectedListener }

        navView.setOnItemSelectedListener {
            hideAllFragments()
            when (it.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction().apply {
                        show(catalogFragment)
                        commit()
                    }

                    true
                }
                R.id.navigation_bookmarks -> {
                    supportFragmentManager.beginTransaction().apply {
                        show(favouritesFragment)
                        commit()
                    }

                    true
                }
                R.id.navigation_profile -> {
                    supportFragmentManager.beginTransaction().apply {
                        show(preferencesFragment)
                        commit()
                    }

                    true
                }
                else -> false
            }
        }
    }

    private fun hideAllFragments() {
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach { hide(it) }
            commit()
        }
    }
}