package com.core.password

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.core.password.databinding.DialButtonsViewBinding

class PasscodeInputView(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {

    var onDialButtonClickListener: OnDialButtonClickListener? = null
    private lateinit var binding: DialButtonsViewBinding
    private var currentInputLength = 0
    private val maxInputLength = 4

    init {
        setupAttributes(attributeSet)
        inflateLayout()
        setupPasscodeBoxes()
        setupDialButtons()
    }

    private fun inflateLayout() {
        binding = DialButtonsViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun setupPasscodeBoxes() {
        // Passcode boxes are already defined in XML as imgpass1, imgpass2, imgpass3, imgpass4
        updatePasscodeBoxes()
    }

    private fun updatePasscodeBoxes() {
        val passcodeBoxes = listOf(
            binding.imgpass1,
            binding.imgpass2,
            binding.imgpass3,
            binding.imgpass4
        )
        passcodeBoxes.forEachIndexed { index, imageView ->
            imageView.isSelected = index < currentInputLength
        }
    }

    private fun setupDialButtons() {
        // Setup number buttons
        val numberButtons = listOf(
            binding.oneBtn to "1",
            binding.twoBtn to "2",
            binding.threeBtn to "3",
            binding.fourBtn to "4",
            binding.fiveBtn to "5",
            binding.sixBtn to "6",
            binding.sevenBtn to "7",
            binding.eightBtn to "8",
            binding.nineBtn to "9",
            binding.zeroBtn to "0"
        )

        numberButtons.forEach { (button, number) ->
            button.setOnClickListener {
                if (currentInputLength < maxInputLength) {
                    currentInputLength++
                    updatePasscodeBoxes()
                    onDialButtonClickListener?.onDialButtonClick(number)
                }
            }
        }

        // Setup delete button
        binding.imvDelete.setOnClickListener {
            if (currentInputLength > 0) {
                currentInputLength--
                updatePasscodeBoxes()
                onDialButtonClickListener?.onDialButtonDeleteClick()
            }
        }
    }

    fun clear() {
        currentInputLength = 0
        updatePasscodeBoxes()
    }

    fun inputNumber(number: String) {
        if (currentInputLength < maxInputLength) {
            currentInputLength++
            updatePasscodeBoxes()
        }
    }

    fun setPasscodeLength(length: Int) {
        currentInputLength = length
        updatePasscodeBoxes()
    }

    fun getCurrentInputLength(): Int = currentInputLength

    fun isInputComplete(): Boolean = currentInputLength == maxInputLength

    private fun setupAttributes(attrs: AttributeSet?) {
        // Keep existing attribute setup if needed
        context.obtainStyledAttributes(attrs, R.styleable.DialButtons).apply {
            recycle()
        }
    }

    interface OnDialButtonClickListener {
        fun onDialButtonClick(text: String)
        fun onDialButtonDeleteClick()
    }
}