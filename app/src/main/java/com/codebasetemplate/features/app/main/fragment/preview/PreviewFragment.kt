package com.codebasetemplate.features.app.main.fragment.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.codebasetemplate.core.base_ui.CoreFragment
import com.codebasetemplate.databinding.FragmentPreviewBinding
import com.codebasetemplate.required.shortcut.AppScreenType
import com.core.baseui.fragment.ScreenType
import com.core.baseui.toolbar.CoreToolbarView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewFragment : CoreFragment<FragmentPreviewBinding>() {
    private var backCallback: OnBackPressedCallback? = null

    private var path: String? = null

    override fun bindingProvider(inflater: LayoutInflater, container: ViewGroup?): FragmentPreviewBinding {
        return FragmentPreviewBinding.inflate(inflater)
    }

    override val screenType: ScreenType = AppScreenType.PreviewFragment

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        context?.let { ct ->
            val move = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

            sharedElementEnterTransition = move
            sharedElementReturnTransition = move

            viewBinding.toolbar.onToolbarListener = object : CoreToolbarView.OnToolbarListener {
                override fun onBack() {
                    closeWithAnimation()
                }
            }

            backCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    closeWithAnimation()
                }
            }

            backCallback?.let {
                activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, it)
            }

            path = arguments?.getString(EXTRA_PATH) ?: path

            Glide.with(ct)
                .load(path)
                .into(viewBinding.imageView)
        }
    }

    private fun closeWithAnimation() {
        viewBinding.toolbar.animate().alpha(0f).setDuration(120).start()
//        controlView.animate().alpha(0f).setDuration(120).start()

        view?.postDelayed({
            parentFragmentManager.popBackStack()
        }, 120)
    }

    companion object {
        private const val EXTRA_PATH = "EXTRA_PATH"

        fun newInstance(path: String) = PreviewFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_PATH, path)
            }
        }
    }
}