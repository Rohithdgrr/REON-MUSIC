/*
 * REON Music App - Repository Module
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.data.repository.di

import com.reon.music.data.repository.RecommendationDataSource
import com.reon.music.data.repository.RecommendationEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindRecommendationDataSource(
        engine: RecommendationEngine
    ): RecommendationDataSource
}
