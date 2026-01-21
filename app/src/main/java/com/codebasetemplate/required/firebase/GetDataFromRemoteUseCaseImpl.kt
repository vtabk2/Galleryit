package com.codebasetemplate.required.firebase

import com.core.config.data.RemoteConfigService
import com.core.config.domain.GetDataFromRemoteConfigUseCase
import com.core.utilities.util.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDataFromRemoteUseCaseImpl @Inject constructor(): GetDataFromRemoteConfigUseCase {
    var onBoardingConfig: OnBoardingConfig = OnBoardingConfig()
    override fun invoke(remoteConfig: RemoteConfigService) {
        val onBoardingConfigModel = remoteConfig.fetchOtherConfig<OnBoardingConfigModel>("onboarding_config")
        onBoardingConfig = OnBoardingConfig(
            onBoardingConfigModel?.version ?: OnBoardingConfig.ONBOARDING_VERSION_1
        )
        Timber.Forest.d("onBoardingConfig: $onBoardingConfig")
    }
}