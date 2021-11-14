package com.example.movie.ui.upcoming

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie.R
import com.example.movie.adapter.SearchMoviesAdapter
import com.example.movie.databinding.FragmentUpcomingBinding
import com.example.movie.models.Result
import com.example.movie.ui.home.HomeViewModel
import com.example.movie.util.Constants
import com.example.movie.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_upcoming.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class UpcomingMoviesFragment : Fragment(R.layout.fragment_upcoming),
    SearchMoviesAdapter.SaveMovieClicked, SearchMoviesAdapter.OnMovieItemClicked {

    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()
    private var upcomingMoviesAdapter = SearchMoviesAdapter(this, this)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUpcomingBinding.bind(view)

        binding.textViewTitleBackToPre.setOnClickListener {
            findNavController().popBackStack()
        }
        observerData()
        setupRecyclerView()
        handleUpcomingEvents()

    }


    var isLoading = false
    var isLastPage = false
    var isScrolling = false


    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount

            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getUpcomingMovies(Constants.MOVIE_LANGUAGE)
                isScrolling = false
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }


        }

    }

    private fun observerData() {
        viewModel.upcomingMoviesMutableList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { movieResponse ->
                        upcomingMoviesAdapter.differ.submitList(movieResponse.results.toList())
                        val totalPages = movieResponse.total_results / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.moviesPage == totalPages
                        if (isLastPage) {
                            binding.upcomingRecyclerView.setPadding(0, 0, 0, 0)
                        }

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "An error occured ${message.toString()}",
                            Toast.LENGTH_LONG
                        )
                            .show()

                        Log.e("HMD", message.toString())
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }

            }
        })
    }

    private fun handleUpcomingEvents(){
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.moviesEvents.collect { event ->
                when (event) {
                    is HomeViewModel.HomeFragmentEvents.NavigateToDetailsFragment -> {
                        val action =
                            UpcomingMoviesFragmentDirections.actionUpcomingMoviesFragmentToDetailsFragment(
                                event.movie
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.HomeFragmentEvents.SaveMovieToFavorite -> {
                        viewModel.saveMovie(event.movie)
                        Snackbar.make(requireView(), "Movie saved successfully", Snackbar.LENGTH_LONG).show()
                    }

                }
            }
        }
    }

    private fun setupRecyclerView() {

        binding.upcomingRecyclerView.apply {
            adapter = upcomingMoviesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(this@UpcomingMoviesFragment.scrollListener)
        }

    }

    private fun hideProgressBar() {
        binding.upcomingProgressBar.visibility = View.INVISIBLE
        isLoading = false

    }

    private fun showProgressBar() {
        binding.upcomingProgressBar.visibility = View.VISIBLE
        isLoading = true


    }

    override fun onSaveMovieClicked(movie: Result) {
        viewModel.onSaveMovieClicked(movie)
    }

    override fun movieItemClicked(movie: Result) {
        viewModel.onMovieItemClicked(movie)
    }
}




