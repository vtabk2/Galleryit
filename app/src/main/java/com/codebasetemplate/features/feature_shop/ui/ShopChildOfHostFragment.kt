package com.codebasetemplate.features.feature_shop.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.codebasetemplate.R
import com.codebasetemplate.databinding.CoreFragmentShopBinding
import com.codebasetemplate.features.main.ui.host.MainHostEvent
import com.codebasetemplate.features.main.ui.host.MainHostViewModel
import com.codebasetemplate.required.inapp.InAppBillingViewModel
import com.codebasetemplate.required.inapp.ProductIdProviderImpl
import com.codebasetemplate.required.shortcut.AppScreenType
import com.core.baseui.BillingViewModel
import com.core.baseui.fragment.BaseChildOfHostFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.collectFlowOn
import com.core.baseui.toolbar.CoreToolbarView
import com.core.utilities.setOnSingleClick
import com.core.utilities.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopChildOfHostFragment : BaseChildOfHostFragment<CoreFragmentShopBinding, MainHostEvent, MainHostViewModel>() {

    private val inAppBillingViewModel: BillingViewModel by viewModels<InAppBillingViewModel>()

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): CoreFragmentShopBinding {
        return CoreFragmentShopBinding.inflate(inflater, container, false)
    }

    override val hostViewModel: MainHostViewModel by viewModels(ownerProducer = { requireParentFragment() })
    override val screenType: ScreenType
        get() = AppScreenType.Shop

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
        viewBinding.run {
            //Subscription

            buyLifetimeButton.setOnSingleClick {
                inAppBillingViewModel.launchBillingFlow(
                    requireActivity(),
                    ProductIdProviderImpl.Companion.PRO
                )
            }

            collectFlowOn(inAppBillingViewModel.checkReadyState) { isReady ->
                Log.d("TAG5", "ShopChildOfHostFragment_initViews: isReady = $isReady")
            }

            yearlyCard.selectButton.setOnSingleClick {
                inAppBillingViewModel.launchBillingFlow(
                    requireActivity(),
                    ProductIdProviderImpl.Companion.PRO_BY_YEAR
                )
            }

            monthlyCard.selectButton.setOnSingleClick {
                inAppBillingViewModel.launchBillingFlow(
                    requireActivity(),
                    ProductIdProviderImpl.Companion.PRO_BY_MONTH
                )
            }

            weeklyCard.selectButton.setOnSingleClick {
                inAppBillingViewModel.launchBillingFlow(
                    requireActivity(),
                    ProductIdProviderImpl.Companion.PRO_BY_WEEK
                )
            }

            restoreLayout.setOnSingleClick {
                inAppBillingViewModel.restorePurchased(false, true)
            }

            removeVipLayout.setOnSingleClick {
                inAppBillingViewModel.removeVip()
            }

            toolbar.onToolbarListener = object : CoreToolbarView.OnToolbarListener {
                override fun onBack() {
                    hostViewModel.navigateTo(MainHostEvent.ActionBack)
                }
            }

            viewBinding.toolbar.textAction = if (purchasePreferences.isUserVip()) "Vip" else "None Vip"

        }
        setupSubscriptionCards()
    }

    private fun setupSubscriptionCards() {
        // Gói 1 Năm
        viewBinding.run {
            yearlyCard.subscriptionIcon.setImageResource(R.drawable.ic_calendar_today)
            yearlyCard.subscriptionIcon.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green_500
                )
            )
            yearlyCard.subscriptionTitle.text = "Gói 1 Năm"
            yearlyCard.subscriptionDescription.text = "Tiết kiệm hơn với gói dài hạn"

            // Gói 1 Tháng
            monthlyCard.subscriptionIcon.setImageResource(R.drawable.ic_date_range)
            monthlyCard.subscriptionIcon.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.purple_500
                )
            )
            monthlyCard.subscriptionTitle.text = "Gói 1 Tháng"
            monthlyCard.subscriptionDescription.text = "Linh hoạt theo nhu cầu"

            // Gói 1 Tuần
            weeklyCard.subscriptionIcon.setImageResource(R.drawable.ic_view_week)
            weeklyCard.subscriptionIcon.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber_500
                )
            )
            weeklyCard.subscriptionTitle.text = "Gói 7 Ngày"
            weeklyCard.subscriptionDescription.text = "Dùng thử các tính năng VIP"
        }

    }


    @SuppressLint("SetTextI18n")
    override fun handleObservable() {

        collectFlowOn(inAppBillingViewModel.productVipLifeTime) { productVipLifeTime ->
            productVipLifeTime?.let {
                viewBinding.lifetimeTitle.text = String.format("%s %s", "Gói Trọn Đời /", it.formattedPrice)
            }
        }

        collectFlowOn(inAppBillingViewModel.productYearly) { productYearly ->
            productYearly?.let {
                viewBinding.yearlyCard.subscriptionTitle.text = String.format("%s %s", "Gói 1 Năm - ", it.formattedPrice)
            }
        }

        collectFlowOn(inAppBillingViewModel.productMonthly) { productMonthly ->
            productMonthly?.let {
                viewBinding.monthlyCard.subscriptionTitle.text = String.format("%s %s", "Gói 1 Tháng - ", it.formattedPrice)
            }
        }

        collectFlowOn(inAppBillingViewModel.productWeekly) { productWeekly ->
            productWeekly?.let {
                viewBinding.weeklyCard.subscriptionTitle.text = String.format("%s %s", "Gói 7 Ngày - ", it.formattedPrice)
            }
        }

        collectFlowOn(inAppBillingViewModel.vipState) { vipState ->
            vipState?.getOrNull()?.let {
                viewBinding.toolbar.textAction = if (it) "Vip" else "None Vip"
                viewBinding.buyLifetimeButton.isEnabled = !purchasePreferences.isProLifeTime
                viewBinding.yearlyCard.selectButton.isEnabled = !purchasePreferences.isProByYear && !purchasePreferences.isProLifeTime
                viewBinding.monthlyCard.selectButton.isEnabled = !purchasePreferences.isProByMonth && !purchasePreferences.isProLifeTime
                viewBinding.weeklyCard.selectButton.isEnabled = !purchasePreferences.isProByWeek && !purchasePreferences.isProLifeTime
            }
        }

        collectFlowOn(inAppBillingViewModel.newPurchaseProLifeTime) {
            if (it != null) {
                toast("Purchase life time success")
                hostViewModel.navigateTo(MainHostEvent.ActionBack)
            }
        }

        collectFlowOn(inAppBillingViewModel.newPurchaseYearly) {
            if (it != null) {
                toast("Purchase yearly success")
//                hostViewModel.navigateTo(MainHostEvent.ActionBack)
            }
        }

        collectFlowOn(inAppBillingViewModel.newPurchaseMonthly) {
            if (it != null) {
                toast("Purchase monthly success")
//                hostViewModel.navigateTo(MainHostEvent.ActionBack)
            }
        }

        collectFlowOn(inAppBillingViewModel.newPurchaseWeekly) {
            if (it != null) {
                toast("Purchase weekly success")
//                hostViewModel.navigateTo(MainHostEvent.ActionBack)
            }
        }

        collectFlowOn(inAppBillingViewModel.restorePurchaseState) { state ->
            state?.getOrNull()?.let {
                if (purchasePreferences.isUserVip()) {
                    toast("VIP restored successfully")
                } else {
                    toast("You are not a VIP member yet.")
                }
            }
        }
    }

}