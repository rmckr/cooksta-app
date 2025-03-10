package com.example.cooksta_app.ui.shared.skeleton

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

class SkeletonTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : MaterialTextView(context, attrs, defStyleAttr) {

    private val skeletonLoader = SkeletonLoader(this)

    fun load(value: String) {
        this.text = value
        skeletonLoader.stop()
    }

    fun unload() {
        this.text = ""
        skeletonLoader.start()
    }
}