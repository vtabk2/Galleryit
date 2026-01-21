package com.codebasetemplate.features.app.customview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.codebasetemplate.R
import com.codebasetemplate.databinding.CustomTabLayoutViewBinding
import com.codebasetemplate.databinding.ItemTabLayoutBinding
import com.codebasetemplate.utils.extensions.smoothSnapToPosition
import com.codebasetemplate.utils.glide.CustomRequestListener
import com.codebasetemplate.utils.glide.svg.SvgSoftwareLayerSetter
import com.core.utilities.gone
import com.core.utilities.setCurrentItemFixCrash
import com.core.utilities.visibleIf
import kotlin.math.abs
import kotlin.math.roundToInt

class CustomTabLayoutView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private var binding: CustomTabLayoutViewBinding

    private var list = mutableListOf<TabItem>()
    private var adapter: RecyclerView.Adapter<*>? = null
    private var tabAdapter: TabAdapter? = null
    private var disabledList = mutableListOf<Int>()

    private var data = Data()
        set(value) {
            field = value
            invalidate()
        }

    var resBackground: Int = 0
        set(value) {
            field = value
            setBackgroundResource(field)
            invalidate()
        }

    var resBackgroundItem: Int = R.drawable.selector_bg_tab
        set(value) {
            field = value
            data.backgroundItem = field
            invalidate()
        }

    var numberItemInScreen: Int = 0
        set(value) {
            field = value
            if (field != 0) {
                data.widthItem = ((resources.displayMetrics.widthPixels - paddingLeft - paddingRight - itemMarginHorizontal * (field - 1)) / (field + 0.5f)).toInt()
            }
            invalidate()
        }

    var showIndicator: Boolean = false
        set(value) {
            field = value
            data.showIndicator = field
            invalidate()
        }

    var showIcon: Boolean = false
        set(value) {
            field = value
            data.showIcon = field
            invalidate()
        }

    var gravityItem: Int = Gravity.CENTER
        set(value) {
            field = value
            data.gravityItem = field
            invalidate()
        }

    var showBackgroundItem: Boolean = true
        set(value) {
            field = value
            data.showBackgroundItem = field
            invalidate()
        }

    var textColorSelector: ColorStateList? = null
        set(value) {
            field = value
            data.textColorSelector = field
            invalidate()
        }

    var textColorSelected: Int = Color.WHITE
        set(value) {
            field = value
            data.textColorSelected = field
            invalidate()
        }

    var textColorUnSelected: Int = Color.BLACK
        set(value) {
            field = value
            data.textColorUnSelected = field
            invalidate()
        }

    var textColorDisabled: Int = "#3f3f3f".toColorInt()
        set(value) {
            field = value
            data.textColorDisabled = field
            invalidate()
        }

    var indicatorColor: Int = Color.BLUE
        set(value) {
            field = value
            data.indicatorColor = field
            invalidate()
        }

    var indicatorSize = IndicatorSize.FULL
        set(value) {
            field = value
            data.indicatorSize = field
            invalidate()
        }

    var indicatorPaddingHorizontal: Float = 0f
        set(value) {
            field = value
            data.indicatorPaddingHorizontal = field
            invalidate()
        }

    var itemMarginHorizontal: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt()
        set(value) {
            field = 2 * value
            data.itemMarginHorizontal = field
            if (field != 0) {
                data.widthItem = ((resources.displayMetrics.widthPixels - paddingLeft - paddingRight - itemMarginHorizontal * (numberItemInScreen - 1)) / (numberItemInScreen + 0.5f)).toInt()
            }
            invalidate()
        }

    var itemMarginVertical: Int = 0
        set(value) {
            field = value
            data.itemMarginVertical = 2 * field
            invalidate()
        }

    var textSize: Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics)
        set(value) {
            field = value
            data.textSize = field
            invalidate()
        }

    private var viewPager: ViewPager2? = null
    private var pagerAdapterObserver: RecyclerView.AdapterDataObserver = PagerAdapterObserver()

    var onUpdateTitleTabLayoutListener: OnUpdateTitleTabLayoutListener? = null

    init {
        binding = CustomTabLayoutViewBinding.inflate(LayoutInflater.from(context), this, true)

        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.CustomTabLayoutView) {
                resBackground = getResourceId(R.styleable.CustomTabLayoutView_ctl_background, resBackground)
                resBackgroundItem = getResourceId(R.styleable.CustomTabLayoutView_ctl_background_item, resBackgroundItem)
                numberItemInScreen = getInt(R.styleable.CustomTabLayoutView_ctl_number_item_in_screen, numberItemInScreen)
                showIndicator = getBoolean(R.styleable.CustomTabLayoutView_ctl_show_indicator, showIndicator)
                showIcon = getBoolean(R.styleable.CustomTabLayoutView_ctl_show_icon, showIcon)
                showBackgroundItem = getBoolean(R.styleable.CustomTabLayoutView_ctl_show_background_item, showBackgroundItem)
                textColorSelector = getColorStateList(R.styleable.CustomTabLayoutView_ctl_text_color_selector)
                textColorSelected = getInt(R.styleable.CustomTabLayoutView_ctl_text_color_selected, textColorSelected)
                textColorUnSelected = getInt(R.styleable.CustomTabLayoutView_ctl_text_color_unselected, textColorUnSelected)
                textColorDisabled = getInt(R.styleable.CustomTabLayoutView_ctl_text_color_disabled, textColorDisabled)
                indicatorColor = getInt(R.styleable.CustomTabLayoutView_ctl_indicator_color, indicatorColor)
                indicatorSize = IndicatorSize.entries.toTypedArray()[getInt(R.styleable.CustomTabLayoutView_ctl_indicator_size, 0)]
                indicatorPaddingHorizontal = getDimension(R.styleable.CustomTabLayoutView_ctl_indicator_padding_horizontal, indicatorPaddingHorizontal)
                itemMarginHorizontal = getDimensionPixelOffset(R.styleable.CustomTabLayoutView_ctl_item_margin_horizontal, itemMarginHorizontal)
                itemMarginVertical = getDimensionPixelOffset(R.styleable.CustomTabLayoutView_ctl_item_margin_vertical, itemMarginVertical)
                textSize = getDimension(R.styleable.CustomTabLayoutView_ctl_text_size, textSize)
            }
        }
        tabAdapter = TabAdapter(context, data, list, callback = { _, position ->
            viewPager?.setCurrentItemFixCrash(position)
        })
        binding.recyclerView.adapter = tabAdapter
        binding.recyclerView.layoutManager = SpeedyLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false, SpeedyLinearLayoutManager.MILLISECONDS_PER_INCH_DEFAULT)
        binding.recyclerView.itemAnimator = null
    }

    fun setupWithViewPager(viewPager2: ViewPager2, onPageSelected: (position: Int) -> Unit) {
        viewPager = viewPager2
        adapter = viewPager2.adapter

        adapter?.registerAdapterDataObserver(pagerAdapterObserver)

        viewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onPageSelected.invoke(position)

                var delta = 0

                tabAdapter?.let {
                    delta = abs(it.getCurrentPosition() - position)
                }

                tabAdapter?.setCurrentSelected(position, hasCallback = false)

                if (delta > 3) {
                    binding.recyclerView.scrollToPosition(position)
                } else {
                    binding.recyclerView.smoothSnapToPosition(position, LinearSmoothScroller.SNAP_TO_ANY)
                }
            }
        })

        populateTabsFromPagerAdapter()
    }

    private fun populateTabsFromPagerAdapter() {
        adapter?.let {
            list.clear()
            val count = it.itemCount
            for (i in 0 until count) {
                val title = onUpdateTitleTabLayoutListener?.getTitle(i) ?: ""
                val thumb = onUpdateTitleTabLayoutListener?.getThumb(i) ?: ""
                val isVip = onUpdateTitleTabLayoutListener?.getVip(i) ?: false
                val enabled = disabledList.find { position -> position == i } == null
                list.add(TabItem(title = title, thumb = thumb, isEnabled = enabled, isVip = isVip))
            }

            viewPager?.let { vp ->
                if (count > 0) {
                    val lastItem = list.size - 1
                    val currentItem = vp.currentItem.coerceAtMost(lastItem)
                    if (currentItem != tabAdapter?.getCurrentPosition()) {
                        tabAdapter?.setCurrentSelected(currentItem)
                    }
                }
            }
        }
        invalidate()
    }

    fun enabledTab(vararg positionArray: Int, isEnabled: Boolean = true) {
        disabledList.clear()
        positionArray.forEach { position ->
            disabledList.add(position)
            list.getOrNull(position)?.isEnabled = isEnabled
        }
        tabAdapter?.notifyDataSetChanged()
    }

    fun reloadGlide() {
        if (data.showIcon) {
            tabAdapter?.notifyDataSetChanged()
        }
    }

    inner class PagerAdapterObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }
    }

    class TabAdapter(val context: Context, val data: Data, val list: MutableList<TabItem>, val callback: (tabItem: TabItem, position: Int) -> Unit) : RecyclerView.Adapter<TabViewHolder>() {
        private val layoutInflater = LayoutInflater.from(context)
        private var currentSelected: TabItem? = null
        private var minWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, context.resources.displayMetrics).toInt()
        private var colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf()
            ),
            intArrayOf(
                data.textColorSelected,
                data.textColorSelected,
                data.textColorDisabled,
                data.textColorUnSelected
            )
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
            val itemTabLayoutBinding = ItemTabLayoutBinding.inflate(layoutInflater, parent, false)

            val layoutParams = itemTabLayoutBinding.root.layoutParams as RecyclerView.LayoutParams
            when (data.gravityItem) {
                Gravity.BOTTOM -> {
                    layoutParams.marginStart = data.itemMarginHorizontal
                    layoutParams.marginEnd = data.itemMarginHorizontal
                }

                else -> {
                    layoutParams.setMargins(data.itemMarginHorizontal, data.itemMarginVertical, data.itemMarginVertical, data.itemMarginHorizontal)
                }
            }
            itemTabLayoutBinding.root.layoutParams = layoutParams
            itemTabLayoutBinding.root.invalidate()

            if (data.showBackgroundItem) {
                itemTabLayoutBinding.tvTitle.setBackgroundResource(data.backgroundItem)
                itemTabLayoutBinding.imageIconTab.setBackgroundResource(data.backgroundItem)
                itemTabLayoutBinding.root.setBackgroundResource(0)
            } else {
                itemTabLayoutBinding.tvTitle.setBackgroundResource(0)
                itemTabLayoutBinding.imageIconTab.setBackgroundResource(0)
                val background = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, TypedValue(), true)
                itemTabLayoutBinding.root.setBackgroundResource(background.resourceId)
            }

            if (data.textSize != 0f) {
                itemTabLayoutBinding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, data.textSize)
            }

            if (data.widthItem != 0) {
                itemTabLayoutBinding.tvTitle.minimumWidth = data.widthItem
                itemTabLayoutBinding.root.invalidate()
            }

            if (data.textColorSelector != null) {
                itemTabLayoutBinding.tvTitle.setTextColor(data.textColorSelector)
            } else {
                itemTabLayoutBinding.tvTitle.setTextColor(colorStateList)
            }

            if (data.indicatorColor != 0) {
                itemTabLayoutBinding.indicator.background = data.indicatorColor.toDrawable()
            }

            return TabViewHolder(itemTabLayoutBinding)
        }

        override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
            list.getOrNull(position)?.let { item ->
                holder.tvTitle.text = item.title
                holder.tvTitle.isSelected = item.isSelected
                holder.imageIconTab.isSelected = item.isSelected
                holder.tvTitle.isEnabled = item.isEnabled
                holder.imageIconTab.isEnabled = item.isEnabled
                holder.root.isEnabled = item.isEnabled
                holder.tvTitle.visibleIf(!data.showIcon)
                holder.imageIconTab.visibleIf(data.showIcon)
                holder.imageVipTab.visibleIf(item.isVip)
                holder.imageError.visibleIf(data.showIcon)

                if (data.showIcon) {
                    when {
                        item.thumb.endsWith(".svg") -> {
                            Glide.with(context)
                                .`as`(PictureDrawable::class.java)
                                .load(item.thumb.toUri())
                                .listener(SvgSoftwareLayerSetter(callback = {
                                    holder.imageError.gone()
                                }))
                                .error(R.drawable.effect_0_thumb)
                                .into(holder.imageIconTab)
                        }

                        item.thumb.startsWith("https") -> {
                            Glide.with(context)
                                .load(item.thumb.toUri())
                                .listener(CustomRequestListener(callback = {
                                    holder.imageError.gone()
                                }))
                                .error(R.drawable.effect_0_thumb)
                                .into(holder.imageIconTab)
                        }

                        else -> {
                            Glide.with(context)
                                .load(item.thumb)
                                .listener(CustomRequestListener(callback = {
                                    holder.imageError.gone()
                                }))
                                .error(R.drawable.effect_0_thumb)
                                .into(holder.imageIconTab)
                        }
                    }
                }

                val paramsIndicator: ViewGroup.LayoutParams = holder.indicator.layoutParams
                when (data.indicatorSize) {
                    IndicatorSize.FULL -> {
                        paramsIndicator.width = if (data.indicatorPaddingHorizontal != 0f) {
                            (holder.root.layoutParams.width - 2 * data.indicatorPaddingHorizontal).roundToInt()
                        } else {
                            LayoutParams.MATCH_PARENT
                        }
                    }

                    IndicatorSize.WRAP -> {
                        holder.tvTitle.apply {
                            val bounds = Rect()
                            val textCurrent = text.toString()
                            this.paint.getTextBounds(textCurrent, 0, textCurrent.length, bounds)

                            paramsIndicator.width = if (data.indicatorPaddingHorizontal != 0f) {
                                (this.paint.measureText(textCurrent) + 0.5f - 2 * data.indicatorPaddingHorizontal)
                            } else {
                                (this.paint.measureText(textCurrent) + 0.5f)
                            }.roundToInt()
                        }
                    }

                    IndicatorSize.MIN -> {
                        paramsIndicator.width = minWidth
                    }
                }
                holder.indicator.layoutParams = paramsIndicator
                holder.indicator.invalidate()
                holder.indicator.visibleIf(item.isSelected && data.showIndicator)
                holder.root.setOnClickListener {
                    setupSelected(item)
                }
            }
        }

        override fun onViewRecycled(holder: TabViewHolder) {
            super.onViewRecycled(holder)
            try {
                Glide.with(holder.imageIconTab.context).clear(holder.imageIconTab)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        fun getCurrentPosition(): Int {
            return list.indexOf(currentSelected)
        }

        fun setCurrentSelected(position: Int, hasCallback: Boolean = true) {
            list.getOrNull(position)?.let { item ->
                setupSelected(item, hasCallback)
            }
        }

        private fun setupSelected(item: TabItem, hasCallback: Boolean = true) {
//            val indexOld = list.indexOf(currentSelected)
            currentSelected?.isSelected = false
            currentSelected = item
            val indexNew = list.indexOf(item)
            currentSelected?.isSelected = true
            if (hasCallback) {
                callback(item, indexNew)
            }
//            notifyItemChanged(indexOld)
//            notifyItemChanged(indexNew)
            notifyDataSetChanged()
        }
    }

    class TabViewHolder(itemTabLayoutBinding: ItemTabLayoutBinding) : RecyclerView.ViewHolder(itemTabLayoutBinding.root) {
        val root = itemTabLayoutBinding.root
        val tvTitle = itemTabLayoutBinding.tvTitle
        val indicator = itemTabLayoutBinding.indicator
        val imageError = itemTabLayoutBinding.imageError
        val imageVipTab = itemTabLayoutBinding.imageVipTab
        val imageIconTab = itemTabLayoutBinding.imageIconTab
    }

    data class TabItem(var title: String = "", var thumb: String = "", var isSelected: Boolean = false, var isEnabled: Boolean = true, var isVip: Boolean = false)

    data class Data(
        var backgroundItem: Int = 0,
        var widthItem: Int = 0,
        var showIndicator: Boolean = false,
        var showBackgroundItem: Boolean = true,
        var textColorSelector: ColorStateList? = null,
        var textColorSelected: Int = Color.WHITE,
        var textColorUnSelected: Int = Color.BLACK,
        var textColorDisabled: Int = "#3f3f3f".toColorInt(),
        var indicatorColor: Int = 0,
        var indicatorSize: IndicatorSize = IndicatorSize.FULL,
        var indicatorPaddingHorizontal: Float = 0f,
        var itemMarginHorizontal: Int = 0,
        var itemMarginVertical: Int = 0,
        var textSize: Float = 0f,
        var showIcon: Boolean = false,
        var gravityItem: Int = Gravity.CENTER
    )

    interface OnUpdateTitleTabLayoutListener {
        fun getTitle(position: Int): String {
            return ""
        }

        fun getThumb(position: Int): String {
            return ""
        }

        fun getVip(position: Int): Boolean {
            return false
        }
    }

    enum class IndicatorSize {
        FULL,
        WRAP,
        MIN
    }
}