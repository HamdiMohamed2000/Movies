package com.example.movie.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.movie.di.ApplicationScope
import com.example.movie.models.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Result::class] ,  version = 1 )
abstract class MovieDatabase : RoomDatabase(){

    abstract fun movieDao() : MoviesDao

    class Callback @Inject constructor(
        private val database: Provider<MovieDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope

    ): RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

        }
    }

}
