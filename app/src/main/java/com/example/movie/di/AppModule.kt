package com.example.movie.di

import android.app.Application
import androidx.room.Room
import com.example.movie.db.MovieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule{

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application ,
        callback:MovieDatabase.Callback
    ) = Room.databaseBuilder(app, MovieDatabase::class.java,"movie_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun provideMovieDao(db:MovieDatabase) = db.movieDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())




}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope