package com.codebasetemplate.shared.frame.data.local.di

import com.codebasetemplate.core.data.local.AppDatabase
import com.codebasetemplate.shared.frame.data.local.dao.FrameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class FrameModule {

    @Provides
    fun provideFrameDao(appDatabase: AppDatabase): FrameDao {
        return appDatabase.frameDao()
    }
}