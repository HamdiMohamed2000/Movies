package com.example.movie.api

import androidx.fragment.app.viewModels
import com.example.movie.models.LatestMoviesResponse
import com.example.movie.models.MoviesResponse
import com.example.movie.ui.home.HomeViewModel
import com.example.movie.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface MoviesApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key")
        apiKey: String = API_KEY,
        @Query("language")
        language: String,
        @Query("page")
        page: Int
    ): Response<MoviesResponse>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key")
        apiKey: String = API_KEY,
        @Query("language")
        language: String,
        @Query("page")
        page: Int
    ): Response<MoviesResponse>


    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key")
        apiKey: String = API_KEY,
        @Query("language")
        language: String,
        @Query("page")
        page: Int
    ): Response<MoviesResponse>

    @GET("movie/latest")
    suspend fun getLatestMovies(
        @Query("api_key")
        apiKey: String = API_KEY,
        @Query("language")
        language: String
    ): Response<LatestMoviesResponse>

    @GET("search/movie")
    suspend fun getSearchedMovies(
        @Query("api_key")
        apiKey: String = API_KEY,
        @Query("language")
        language: String,
        @Query("query")
        query: String,
        @Query("page")
        page: Int = 1

    ): Response<MoviesResponse>


    @GET("movie/now_playing")
    suspend fun getMovieNowPlay(
        @Query("api_key")
        apiKey: String = API_KEY,
        @Query("language")
        language: String,
        @Query("page")
        page: Int = 1

    ): Response<MoviesResponse>


    @GET("movie/{movie_id}/recommendations")
    suspend fun getRecommendedMovies(
        @Path("movie_id")
        id: Int,
        @Query("api_key")
        apiKey: String = API_KEY,
        @Query("language")
        language: String,
        @Query("page")
        page: Int = 1
    ): Response<MoviesResponse>
}