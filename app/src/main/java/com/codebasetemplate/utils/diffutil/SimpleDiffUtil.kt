package com.codebasetemplate.utils.diffutil

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.core.baseui.BaseItemUI
import javax.inject.Inject

class SimpleDiffUtil @Inject constructor() : DiffUtil.ItemCallback<BaseItemUI>() {
    override fun areItemsTheSame(oldItem: BaseItemUI, newItem: BaseItemUI): Boolean {
        return oldItem.identify == newItem.identify
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: BaseItemUI, newItem: BaseItemUI): Boolean {
        return oldItem == newItem
    }
}