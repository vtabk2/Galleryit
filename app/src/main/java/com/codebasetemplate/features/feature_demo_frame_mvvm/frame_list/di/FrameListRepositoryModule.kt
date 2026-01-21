package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.di

import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.data.repository.FrameRepositoryImpl
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.repository.FrameListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class FrameListRepositoryModule {

    @Provides
    fun provideFrameListRepository(frameRepositoryImpl: FrameRepositoryImpl): FrameListRepository {
        return frameRepositoryImpl
    }
}