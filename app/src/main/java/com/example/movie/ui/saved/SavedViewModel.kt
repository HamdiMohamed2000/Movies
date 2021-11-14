package com.example.movie.ui.saved

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.db.MoviesDao
import com.example.movie.models.Result
import com.example.movie.repository.MoviesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SavedViewModel @ViewModelInject constructor(
    val moviesRepository: MoviesRepository,
    val movieDao: MoviesDao
) : ViewModel() {

    private val favoriteMoviesEventsChannel = Channel<FavoriteFragmentEvents>()
    val favoriteMoviesEvents = favoriteMoviesEventsChannel.receiveAsFlow()

    fun getSavedMovie() = movieDao.getAllMovies()

    fun onDeleteSavedMovieClicked(movie: Result) = viewModelScope.launch {
        favoriteMoviesEventsChannel.send(FavoriteFragmentEvents.DeleteMovieSaved(movie))
    }

    fun deleteSavedMovie(movie: Result) = viewModelScope.launch {
        movieDao.deleteMovie(movie)
        favoriteMoviesEventsChannel.send(FavoriteFragmentEvents.ShowUndoDeleteTaskMessage(movie))
    }

    fun onUndoDeleteClick(movie: Result) = viewModelScope.launch {
        movieDao.upsert(movie)
    }

    fun onSavedMovieItemClicked(movie: Result) = viewModelScope.launch{
        favoriteMoviesEventsChannel.send(FavoriteFragmentEvents.NavigateToDetailsFragment(movie))
    }
    sealed class FavoriteFragmentEvents {
        data class ShowUndoDeleteTaskMessage(val movie: Result) : FavoriteFragmentEvents()
        data class DeleteMovieSaved(val movie: Result) : FavoriteFragmentEvents()
        data class NavigateToDetailsFragment(val movie: Result) : FavoriteFragmentEvents()
    }

}