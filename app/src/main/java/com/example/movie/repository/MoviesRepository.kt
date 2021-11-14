package com.example.movie.repository

import com.example.movie.api.RetrofitInstance
import com.example.movie.util.Constants.Companion.API_KEY
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MoviesRepository @Inject constructor(
    //val db: MoviesDatabase
) {
    suspend fun getPopularMovies(language: String, pageNumber: Int) =
        RetrofitInstance.api.getPopularMovies(API_KEY,language,pageNumber)

    suspend fun getTopRatedMovies(language: String, pageNumber: Int) =
        RetrofitInstance.api.getTopRatedMovies(API_KEY,language,pageNumber)

    suspend fun getUpcomingMovies(language: String, pageNumber: Int) =
        RetrofitInstance.api.getUpcomingMovies(API_KEY,language,pageNumber)

    suspend fun getLatestMovies(language: String) =
        RetrofitInstance.api.getLatestMovies(API_KEY,language)

    suspend fun getSearchedMovie(language: String,query:String) =
        RetrofitInstance.api.getSearchedMovies(API_KEY,language,query)

    suspend fun getMovieNowPlay(language: String,pageNumber : Int) =
        RetrofitInstance.api.getMovieNowPlay(API_KEY,language,pageNumber)

  suspend fun getRecommendedMovie(id : Int,language: String,pageNumber : Int) =
      RetrofitInstance.api.getRecommendedMovies(id ,API_KEY,language,pageNumber)


}