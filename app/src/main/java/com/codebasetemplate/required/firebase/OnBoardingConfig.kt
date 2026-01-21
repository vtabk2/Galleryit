package com.codebasetemplate.required.firebase

data class OnBoardingConfig (
    var version: Int = ONBOARDING_VERSION_1
) {
    companion object {
        const val ONBOARDING_VERSION_1 = 1
        const val ONBOARDING_VERSION_2 = 2
    }

}