package com.hcmus.clc18se.buggynote.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import timber.log.Timber

/**
 * Base fragment to setup the toolbar using navigation controller
 */
abstract class BaseFragment: Fragment() {

    // get the toolbar object from the layout which needs to be setup navigation
    abstract fun getToolbarView(): Toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
    }

    protected open fun setUpNavigation() {
        val toolbar = getToolbarView()
        val parentActivity = requireActivity() as? BuggyNoteActivity

        if (parentActivity == null) {
            Timber.e("Parent activity of fragment ${this.tag} is not BuggyNoteActivity")
        }

        parentActivity?.setSupportActionBar(toolbar)
        parentActivity?.setupActionBarWithNavController(
                findNavController(),
                parentActivity.appBarConfiguration
        )
    }

}