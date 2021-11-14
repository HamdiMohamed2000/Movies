package com.example.movie.ui.details

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.db.MoviesDao
import com.example.movie.models.MoviesResponse
import com.example.movie.models.Result
import com.example.movie.repository.MoviesRepository
import com.example.movie.ui.home.HomeViewModel
import com.example.movie.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class DetailsViewModel @ViewModelInject constructor(
    val moviesRepository: MoviesRepository,
    val movieDao: MoviesDao
) : ViewModel() {

    val movies: MutableLiveData<Resource<MoviesResponse>> = MutableLiveData()
    var moviesPage = 1


    private val moviesEventsChannel = Channel<DetailsFragmentEvents>()
    val moviesEvents = moviesEventsChannel.receiveAsFlow()

    fun onMovieItemClicked(movie: Result) = viewModelScope.launch {
        moviesEventsChannel.send(DetailsFragmentEvents.NavigateToDetailsFragment(movie))
    }

    fun onLoadMoreRecommendedClicked(movie: Result) = viewModelScope.launch {
        moviesEventsChannel.send(DetailsFragmentEvents.OnLoadMoreRecommendedClicked(movie))
    }

    fun onSaveButtonClicked(movie: Result) = viewModelScope.launch {
        moviesEventsChannel.send(DetailsFragmentEvents.SaveDetailsMovie(movie))
    }

    suspend fun changSaveButton(id: Int): Boolean {
        if (movieDao.getMovieId(id)) {
            return true
        }
        return false
    }


    fun getRecommendedMovies(language: String, id: Int) = viewModelScope.launch {
        movies.postValue(Resource.Loading())
        val response = moviesRepository.getRecommendedMovie(id, language, moviesPage)
        movies.postValue(handleRecommendedMovies(response))
    }


    private fun handleRecommendedMovies(response: Response<MoviesResponse>): Resource<MoviesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())


    }

    fun saveMovie(movie: Result) = viewModelScope.launch {
        movieDao.upsert(movie)
    }


    sealed class DetailsFragmentEvents {

        data class NavigateToDetailsFragment(val movie: Result) : DetailsFragmentEvents()
        data class SaveDetailsMovie(val movie: Result) : DetailsFragmentEvents()
        data class OnLoadMoreRecommendedClicked(val movie: Result) : DetailsFragmentEvents()

    }
}
