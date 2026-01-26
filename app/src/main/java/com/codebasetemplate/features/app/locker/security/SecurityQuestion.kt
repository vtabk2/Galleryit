package com.codebasetemplate.features.app.locker.security

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.codebasetemplate.databinding.CustomMenuSecurityBinding

class SecurityQuestion : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    var indexSecurity = 0

    var onClickQuestion: ((Int) -> Unit)? = null

    private var binding: CustomMenuSecurityBinding = CustomMenuSecurityBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        val question = getSecurityQuestions(context)
        listOf(binding.question0, binding.question1, binding.question2, binding.question3, binding.question4).forEachIndexed { index, view ->
            view.text = question[index]
            view.setOnClickListener {
                onClickQuestion?.invoke(index)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // Ensure the view is measured correctly
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}