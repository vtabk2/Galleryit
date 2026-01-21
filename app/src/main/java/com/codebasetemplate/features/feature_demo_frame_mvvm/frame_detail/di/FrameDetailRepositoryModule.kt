package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.di

import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.data.repository.FrameDetailRepositoryImpl
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.repository.FrameDetailRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class FrameDetailRepositoryModule {


    @Provides
    fun provideFrameDetailRepository(frameDetailRepositoryImpl: FrameDetailRepositoryImpl): FrameDetailRepository {
        return frameDetailRepositoryImpl
    }
}