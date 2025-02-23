package com.example.cooksta_app.ui.shared

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.example.cooksta_app.R
import com.google.android.material.textview.MaterialTextView

class SkeletonTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    private var objectAnimator: ObjectAnimator

    init {
        this.background = ContextCompat.getDrawable(context, R.drawable.skeleton_bg)
        this.objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.5f, 1f).apply {
            duration = 500
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    fun load(value: String) {
        this.objectAnimator.cancel()
        this.alpha = 1f
        this.background = null
        this.text = value
    }
}