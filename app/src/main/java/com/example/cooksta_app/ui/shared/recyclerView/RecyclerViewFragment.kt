package com.example.cooksta_app.ui.shared.recyclerView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.cooksta_app.databinding.FragmentRecyclerViewBinding

abstract class RecyclerViewFragment<T: RecyclerView.Adapter<*>> : Fragment() {

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!

    abstract fun provideAdapter(): T

    open fun provideLayoutManager(): LayoutManager {
        return LinearLayoutManager(context)
    }

    open fun provideDecoration(): ItemDecoration {
        return DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)

        binding.recyclerView.adapter = provideAdapter()
        binding.recyclerView.layoutManager = provideLayoutManager()
        binding.recyclerView.addItemDecoration(provideDecoration())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
