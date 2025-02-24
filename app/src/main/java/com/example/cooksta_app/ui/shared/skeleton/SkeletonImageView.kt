package com.example.cooksta_app.ui.shared.skeleton

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class SkeletonImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val skeletonLoader = SkeletonLoader(this, false)

    fun load(drawable: Drawable) {
        this.setImageDrawable(drawable)
        skeletonLoader.stop()
    }
    fun load(bitmap: Bitmap) {
        this.setImageBitmap(bitmap)
        skeletonLoader.stop()
    }
}
