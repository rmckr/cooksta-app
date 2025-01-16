package com.example.cooksta_app.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration

abstract class RecyclerViewFragment<T: RecyclerView.Adapter<*>> : Fragment() {

    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: T

    abstract fun provideAdapter(): T

    open fun provideLayoutManager(): LayoutManager {
        return LinearLayoutManager(context)
    }

    open fun provideDecoration(): ItemDecoration? {
        return MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
    }

    open fun onRecyclerViewCreated(recyclerView: RecyclerView) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView = RecyclerView(requireContext())
        recyclerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        adapter = provideAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = provideLayoutManager()
        provideDecoration()?.let { recyclerView.addItemDecoration(it) }

        onRecyclerViewCreated(recyclerView)

        return recyclerView
    }
}
