package com.example.movie.ui.saved

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie.R
import com.example.movie.adapter.SavedMoviesAdapter
import com.example.movie.databinding.FragmentSavedBinding
import com.example.movie.models.Result
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SavedMoviesFragment : Fragment(R.layout.fragment_saved),
    SavedMoviesAdapter.DeleteMovieClicked, SavedMoviesAdapter.OnMovieItemClicked {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!


    private var favoriteAdapter = SavedMoviesAdapter(this, this)
    private val viewModel by viewModels<SavedViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSavedBinding.bind(view)
        setupRecyclerView()
        observingData()
        savedEventHandling()


    }


    private fun setupRecyclerView() {
        binding.favoriteRecyclerView.apply {
            adapter = favoriteAdapter
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)

        }
    }


    private fun observingData() {
        viewModel.getSavedMovie().observe(viewLifecycleOwner, Observer { movie ->
            favoriteAdapter.differ.submitList(movie)


        }
        )
    }


    private fun savedEventHandling() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.favoriteMoviesEvents.collect { event ->
                when (event) {
                    is SavedViewModel.FavoriteFragmentEvents.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Movie deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.movie)
                            }.show()

                    }
                    is SavedViewModel.FavoriteFragmentEvents.DeleteMovieSaved -> {
                        viewModel.deleteSavedMovie(event.movie)
                    }
                    is SavedViewModel.FavoriteFragmentEvents.NavigateToDetailsFragment -> {
                        val action =
                            SavedMoviesFragmentDirections.actionFavoriteFragmentToDetailsFragment(
                                event.movie
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }

    }

    override fun onDeleteMovieClicked(movie: Result) {
        viewModel.onDeleteSavedMovieClicked(movie)
    }

    override fun onItemMovieClicked(movie: Result) {
        viewModel.onSavedMovieItemClicked(movie)
    }


}