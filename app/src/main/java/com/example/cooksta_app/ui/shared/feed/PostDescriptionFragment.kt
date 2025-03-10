package com.example.cooksta_app.ui.shared.feed

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.cooksta_app.R

class PostDescriptionFragment : Fragment() {

    companion object {
        fun newInstance(description: String) = PostDescriptionFragment().apply {
            arguments = Bundle().apply {
                putString("description", description)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val textView = TextView(requireContext())

        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        textView.setPadding(padding)
        textView.text = arguments?.getString("description") ?: getString(R.string.no_description)

        return textView
    }
}