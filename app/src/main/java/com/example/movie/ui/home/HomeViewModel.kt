package com.example.movie.ui.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.ContactsContract
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movie.MovieApplication
import com.example.movie.db.MoviesDao
import com.example.movie.models.MoviesResponse
import com.example.movie.models.Result
import com.example.movie.repository.MoviesRepository
import com.example.movie.util.Constants.Companion.MOVIE_LANGUAGE
import com.example.movie.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class HomeViewModel @ViewModelInject constructor(
    val moviesRepository: MoviesRepository,
    private val movieDao: MoviesDao,
    app: Application
) : AndroidViewModel(app) {

    val popularMoviesMutableList: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()
    val topRatesMoviesMutableList: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()
    val upcomingMoviesMutableList: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()
    val moviePlayNowMutableList: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()


    var movieResponse: MoviesResponse? = null
    var moviesPage = 1

    private val moviesEventsChannel = Channel<HomeFragmentEvents>()
    val moviesEvents = moviesEventsChannel.receiveAsFlow()

    fun onMovieItemClicked(movie: Result) = viewModelScope.launch {
        moviesEventsChannel.send(HomeFragmentEvents.NavigateToDetailsFragment(movie))
    }

    fun onSaveMovieClicked(movie: Result) = viewModelScope.launch {
        moviesEventsChannel.send(HomeFragmentEvents.SaveMovieToFavorite(movie))

    }

    fun onBtnPopularLoadMoreClicked() = viewModelScope.launch {
        moviesEventsChannel.send(HomeFragmentEvents.OnLoadMorePopularClicked)
    }


    fun onBtnTopRatedLoadMoreClicked() = viewModelScope.launch {
        moviesEventsChannel.send(HomeFragmentEvents.OnLoadMoreTopRatedClicked)
    }

    fun onBtnUpcomingLoadMoreClicked() = viewModelScope.launch {
        moviesEventsChannel.send(HomeFragmentEvents.OnLoadMoreUpcomingClicked)
    }


    init {
        getPopularMovies(MOVIE_LANGUAGE)
        getTopRatedMovies(MOVIE_LANGUAGE)
        getUpcomingMovies(MOVIE_LANGUAGE)
        getMoviePlayNow(MOVIE_LANGUAGE)

    }


    fun getPopularMovies(language: String) = viewModelScope.launch {
        safePopularMoviesCall(language)
    }


    private fun handlePopularMoviesResponse(response: Response<MoviesResponse>): Resource<MoviesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                moviesPage++
                if (movieResponse == null) {

                    movieResponse = resultResponse
                } else {
                    val oldMovie = movieResponse?.results
                    val newMovie = resultResponse.results
                    oldMovie?.addAll(newMovie)
                }
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())


    }

    fun getTopRatedMovies(language: String) = viewModelScope.launch {
        safeTopRatedMoviesCall(language)
    }

    private fun handleTopRatedMoviesResponse(response: Response<MoviesResponse>): Resource<MoviesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                moviesPage++
                if (movieResponse == null) {

                    movieResponse = resultResponse
                } else {
                    val oldMovie = movieResponse?.results
                    val newMovie = resultResponse.results
                    oldMovie?.addAll(newMovie)
                }
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getUpcomingMovies(language: String) = viewModelScope.launch {
        safeUpcomingMoviesCall(language)
    }

    private fun handleUpcomingMoviesResponse(response: Response<MoviesResponse>): Resource<MoviesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                moviesPage++
                if (movieResponse == null) {

                    movieResponse = resultResponse
                } else {
                    val oldMovie = movieResponse?.results
                    val newMovie = resultResponse.results
                    oldMovie?.addAll(newMovie)
                }
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    private fun getMoviePlayNow(language: String) = viewModelScope.launch {
        safePlayNowMoviesCall(language)
    }

    private fun handleMoviePlayNow(response: Response<MoviesResponse>): Resource<MoviesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                moviesPage++
                if (movieResponse == null) {

                    movieResponse = resultResponse
                } else {
                    val oldMovie = movieResponse?.results
                    val newMovie = resultResponse.results
                    oldMovie?.addAll(newMovie)
                }
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveMovie(movie: Result) = viewModelScope.launch {
        movieDao.upsert(movie)
    }


    private suspend fun safePopularMoviesCall(language: String) {
        popularMoviesMutableList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val popularResponse = moviesRepository.getPopularMovies(language, moviesPage)
                popularMoviesMutableList.postValue(handlePopularMoviesResponse(popularResponse))

            } else {
                popularMoviesMutableList.postValue(Resource.Error("No Internet connection"))


            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> popularMoviesMutableList.postValue(Resource.Error("Network Failure"))
                else -> popularMoviesMutableList.postValue(Resource.Error("Convertion Error"))
            }

        }
    }

    private suspend fun safePlayNowMoviesCall(language: String) {
        moviePlayNowMutableList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val playNowResponse = moviesRepository.getMovieNowPlay(language, moviesPage)
                moviePlayNowMutableList.postValue(handleMoviePlayNow(playNowResponse))

            } else {
                moviePlayNowMutableList.postValue(Resource.Error("No Internet connection"))


            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> moviePlayNowMutableList.postValue(Resource.Error("Network Failure"))
                else -> moviePlayNowMutableList.postValue(Resource.Error("Convertion Error"))
            }

        }
    }

    private suspend fun safeUpcomingMoviesCall(language: String) {
        upcomingMoviesMutableList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val upcomingResponse = moviesRepository.getUpcomingMovies(language, moviesPage)
                upcomingMoviesMutableList.postValue(handleUpcomingMoviesResponse(upcomingResponse))

            } else {
                upcomingMoviesMutableList.postValue(Resource.Error("No Internet connection"))


            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> upcomingMoviesMutableList.postValue(Resource.Error("Network Failure"))
                else -> upcomingMoviesMutableList.postValue(Resource.Error("Convertion Error"))
            }

        }
    }

    private suspend fun safeTopRatedMoviesCall(language: String) {
        topRatesMoviesMutableList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val topRatedResponse = moviesRepository.getTopRatedMovies(language, moviesPage)
                topRatesMoviesMutableList.postValue(handleTopRatedMoviesResponse(topRatedResponse))

            } else {
                topRatesMoviesMutableList.postValue(Resource.Error("No Internet connection"))


            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> topRatesMoviesMutableList.postValue(Resource.Error("Network Failure"))
                else -> topRatesMoviesMutableList.postValue(Resource.Error("Convertion Error"))
            }

        }
    }


    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<MovieApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val actionNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(actionNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ContactsContract.CommonDataKinds.Email.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    sealed class HomeFragmentEvents {

        data class NavigateToDetailsFragment(val movie: Result) : HomeFragmentEvents()
        data class SaveMovieToFavorite(val movie: Result) : HomeFragmentEvents()
        object OnLoadMorePopularClicked : HomeFragmentEvents()
        object OnLoadMoreTopRatedClicked : HomeFragmentEvents()
        object OnLoadMoreUpcomingClicked : HomeFragmentEvents()

    }


}