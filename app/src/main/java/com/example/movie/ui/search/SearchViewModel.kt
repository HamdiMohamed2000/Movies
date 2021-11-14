package com.example.movie.ui.search

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

class SearchViewModel @ViewModelInject constructor(
    val moviesRepository: MoviesRepository,
    private val movieDao: MoviesDao,
    app : Application
) : AndroidViewModel(app) {


    val searchedMoviesMutableList: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()
    private val searchEventsChannel = Channel<SearchFragmentEvents>()
    val searchEvents = searchEventsChannel.receiveAsFlow()

    var moviesPage = 1
    private var movieResponse: MoviesResponse? = null

    fun onSaveMovieClicked(movie: Result) = viewModelScope.launch {
        searchEventsChannel.send(SearchFragmentEvents.SaveMovieToFavorite(movie))

    }

    fun getSearchMovies(query: String) = viewModelScope.launch {
       safeSearchMoviesCall(query)
    }

    fun onMovieItemClicked(movie: Result) = viewModelScope.launch {
        searchEventsChannel.send(SearchFragmentEvents.NavigateToDetails(movie))
    }

    private fun handleSearchedMoviesResponse(response: Response<MoviesResponse>): Resource<MoviesResponse> {
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

    fun saveMovie(movie : Result) = viewModelScope.launch {
        movieDao.upsert(movie)
    }

    private suspend fun safeSearchMoviesCall(query: String) {
        searchedMoviesMutableList.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val SearchResponse = moviesRepository.getSearchedMovie(MOVIE_LANGUAGE,query)
                searchedMoviesMutableList.postValue(handleSearchedMoviesResponse(SearchResponse))

            } else {
                searchedMoviesMutableList.postValue(Resource.Error("No Internet connection"))


            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchedMoviesMutableList.postValue(Resource.Error("Network Failure"))
                else -> searchedMoviesMutableList.postValue(Resource.Error("Convertion Error"))
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


    sealed class SearchFragmentEvents {
        data class SaveMovieToFavorite(val movie: Result) : SearchFragmentEvents()
        data class NavigateToDetails(val movie: Result)  :SearchFragmentEvents()
    }

}