package com.hcmus.clc18se.buggynote

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.hcmus.clc18se.buggynote.databinding.ActivityMainBinding
import com.hcmus.clc18se.buggynote.utils.ControllableDrawerActivity
import com.hcmus.clc18se.buggynote.utils.OnBackPressed
import timber.log.Timber

class BuggyNoteActivity : AppCompatActivity(), ControllableDrawerActivity {

    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }

    private val navController by lazy { navHostFragment.navController }

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var binding: ActivityMainBinding

    internal lateinit var appBarConfiguration: AppBarConfiguration

    companion object {
        private val topDestination = listOf(R.id.nav_notes, R.id.nav_tags, R.id.nav_archive)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        appBarConfiguration = AppBarConfiguration(setOf(*topDestination.toTypedArray()), drawerLayout)

        val navView: NavigationView = binding.navView
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)

            Handler(Looper.getMainLooper()).postDelayed({
                when (item.itemId) {
                    else -> NavigationUI.onNavDestinationSelected(
                            item, navController
                    ) || onOptionsItemSelected(item)
                }
            }, 280)
        }

        navController.addOnDestinationChangedListener(onDestinationChangedListener)
    }

    override fun onBackPressed() {

        // get the current fragment
        val currentFragment = navHostFragment.childFragmentManager.fragments[0] as? OnBackPressed
        val defaultBackPress = currentFragment?.onBackPress()?.not() ?: true
        Timber.d(defaultBackPress.toString())

        if (defaultBackPress) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }

    }

    override fun onDestroy() {
        navController.removeOnDestinationChangedListener(onDestinationChangedListener)
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id in topDestination) {
            false -> lockTheDrawer()
            else -> unlockTheDrawer()
        }
    }

    override fun lockTheDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun unlockTheDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
}