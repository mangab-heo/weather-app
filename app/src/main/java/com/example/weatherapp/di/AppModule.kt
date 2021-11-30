package com.example.weatherapp.di

import com.example.weatherapp.data.source.DataSource
import com.example.weatherapp.data.source.DefaultRepository
import com.example.weatherapp.data.source.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Qualifier
    @RemoteDataSource
    annotation class RemoteDataSource

    @Singleton
    @RemoteDataSource
    @Provides
    fun provideRemoteDataSource(): DataSource {
        return com.example.weatherapp.data.source.remote.RemoteDataSource()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideRepository(
        @AppModule.RemoteDataSource remoteDataSource: DataSource
    ): Repository {
        return DefaultRepository(remoteDataSource)
    }
}