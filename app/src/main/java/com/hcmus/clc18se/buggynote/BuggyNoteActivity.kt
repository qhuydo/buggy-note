package com.hcmus.clc18se.buggynote

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.hcmus.clc18se.buggynote.databinding.ActivityMainBinding

class BuggyNoteActivity : AppCompatActivity() {

    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }

    private val navController by lazy { navHostFragment.navController }

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var binding: ActivityMainBinding

    internal lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_notes, R.id.nav_tags
            ), drawerLayout
        )

        val navView: NavigationView = binding.navView
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)

            Handler().postDelayed({
                when (item.itemId) {
                    else -> NavigationUI.onNavDestinationSelected(
                        item, navController
                    ) || onOptionsItemSelected(item)
                }
            }, 280)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}