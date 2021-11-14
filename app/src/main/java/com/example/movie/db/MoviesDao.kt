package com.example.movie.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.movie.models.Result

@Dao
interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(movie: Result) : Long

    @Query("SELECT * FROM movies")
    fun getAllMovies() : LiveData<List<Result>>

    @Delete
    suspend fun deleteMovie(movie: Result)

    @Query("SELECT id FROM movies where (:movieId) like id")
     suspend fun getMovieId(movieId : Int) : Boolean
}