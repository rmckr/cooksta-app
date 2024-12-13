package com.example.cooksta_app.ui.shoppingList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cooksta_app.databinding.FragmentShoppingListEmptyBinding
import com.example.cooksta_app.databinding.FragmentShoppingListFullBinding

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListEmptyBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListEmptyBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}