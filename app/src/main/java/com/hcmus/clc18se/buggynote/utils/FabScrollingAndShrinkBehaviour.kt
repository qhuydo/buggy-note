package com.hcmus.clc18se.buggynote.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar.SnackbarLayout

class FabScrollingAndShrinkBehaviour(context: Context, attrs: AttributeSet)
    : HideBottomViewOnScrollBehavior<FloatingActionButton>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout,
                                 child: FloatingActionButton,
                                 dependency: View): Boolean {

        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout,
                                        child: FloatingActionButton,
                                        dependency: View): Boolean {

        if (dependency is SnackbarLayout) {
            child.translationY = getFabTranslationYForSnackbar(parent, child)
        }
        return false
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout,
                                        child: FloatingActionButton,
                                        dependency: View) {
        if (dependency is SnackbarLayout) {
            child.translationY = 0f
        }
    }

    private fun getFabTranslationYForSnackbar(parent: CoordinatorLayout,
                                              fab: FloatingActionButton): Float {
        var minOffset = 0f
        val dependencies = parent.getDependencies(fab)

        for (i in 0 until dependencies.size) {

            val view = dependencies[i]
            if (view is SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                minOffset = Math.min(minOffset, view.translationY - view.height)
            }

        }

        return minOffset
    }


}