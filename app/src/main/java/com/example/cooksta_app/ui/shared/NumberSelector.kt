package com.example.cooksta_app.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.example.cooksta_app.R
import com.google.android.material.button.MaterialButton

class NumberSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val _value = MutableLiveData<Int>(0)
    val value: LiveData<Int> = _value

    private var coerceValue: (Int) -> Int = { it.coerceIn(1, 999) }
    private var onValueChangeListener: ((Int) -> Unit)? = null

    private val inputField: EditText
    private val incrementButton: MaterialButton
    private val decrementButton: MaterialButton

    init {
        LayoutInflater.from(context).inflate(R.layout.shared_numberselector, this, true)

        inputField = findViewById(R.id.selectorInput)
        decrementButton = findViewById(R.id.selectorDecButton)
        incrementButton = findViewById(R.id.selectorIncButton)

        inputField.setOnFocusChangeListener { view, hasFocus ->
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

        inputField.setOnEditorActionListener { view, actionId, _ ->
            view as EditText
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputValue = inputField.text.toString().toIntOrNull() ?: 0
                val coercedValue = coerceValue(inputValue)
                onValueChangeListener?.invoke(coercedValue)
                _value.value = coercedValue
                view.clearFocus()
            }
            false
        }
        decrementButton.setOnClickListener {
            val coercedValue = coerceValue(_value.value?.minus(1) ?: 0)
            _value.value = coercedValue
            onValueChangeListener?.invoke(coercedValue)
        }
        incrementButton.setOnClickListener {
            val coercedValue = coerceValue(_value.value?.plus(1) ?: 0)
            _value.value = coercedValue
            onValueChangeListener?.invoke(_value.value!!)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            value.observe(lifecycleOwner) { newValue ->
                inputField.setText(coerceValue(newValue).toString())
            }
        }
    }

    fun setCoercionFunction(coercion: (Int) -> Int) {
        coerceValue = coercion
    }

    fun setValue(value: Int) {
        _value.value = value
    }

    fun setOnValueChangeListener(listener: (Int) -> Unit) {
        onValueChangeListener = listener
    }
}