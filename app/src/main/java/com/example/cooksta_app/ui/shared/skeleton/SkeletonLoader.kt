package com.example.cooksta_app.ui.shared.skeleton

import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import com.example.cooksta_app.R

class SkeletonLoader(private val view: View, hasRoundedCorners: Boolean = true) {

    private val objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f).apply {
        duration = 500
        repeatMode = ObjectAnimator.REVERSE
        repeatCount = ObjectAnimator.INFINITE
        start()
    }

    init {
        val drawable = ContextCompat.getDrawable(view.context, R.drawable.skeleton_bg) as GradientDrawable
        if (!hasRoundedCorners) { drawable.cornerRadius = 0f }
        view.background = drawable
    }

    fun stop() {
        this.objectAnimator.cancel()
        view.alpha = 1f
        view.background = null
    }
}