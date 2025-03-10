package com.example.cooksta_app.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.cooksta_app.databinding.SharedNumberSelectorBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class NumberSelector @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private val _currentValue = MutableStateFlow(1)
    val currentValue: StateFlow<Int> get() = _currentValue

    private var coerceValue: (Int) -> Int = { it.coerceIn(1, 999) }
    private var onValueChangeListener: ((Int) -> Unit)? = null

    private var _binding: SharedNumberSelectorBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = SharedNumberSelectorBinding.inflate(LayoutInflater.from(context), this, true)

        binding.selectorInput.setOnFocusChangeListener { view, hasFocus ->
            view as EditText
            if (hasFocus) {
                view.post { view.selectAll() }
            } else {
                view.post {
                    view.setSelection(0, 0)
                    view.isCursorVisible = false
                }
            }
        }

        binding.selectorInput.setOnEditorActionListener { view, actionId, _ ->
            view as EditText
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputValue = binding.selectorInput.text.toString().toIntOrNull() ?: 0
                val coercedValue = coerceValue(inputValue)
                onValueChangeListener?.invoke(coercedValue)
                _currentValue.value = coercedValue
                view.clearFocus()
            }
            false
        }
        binding.selectorDecButton.setOnClickListener {
            val coercedValue = coerceValue(_currentValue.value.minus(1))
            _currentValue.value = coercedValue
            onValueChangeListener?.invoke(coercedValue)
        }
        binding.selectorIncButton.setOnClickListener {
            val coercedValue = coerceValue(_currentValue.value.plus(1))
            _currentValue.value = coercedValue
            onValueChangeListener?.invoke(_currentValue.value)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            lifecycleOwner.lifecycleScope.launch {
                currentValue.collect {
                    binding.selectorInput.setText(String.format(Locale.getDefault(), "%d", coerceValue(it)))
                }
            }
        }
    }

    fun setCoercionFunction(coercion: (Int) -> Int) {
        coerceValue = coercion
    }

    fun setCurrentValue(value: Int) {
        _currentValue.value = value
    }

    fun setOnValueChangeListener(listener: (Int) -> Unit) {
        onValueChangeListener = listener
    }
}