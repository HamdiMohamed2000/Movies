package com.example.movie.ui.popular

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
import com.example.movie.databinding.FragmentPopularBinding
import com.example.movie.models.Result
import com.example.movie.ui.home.HomeViewModel
import com.example.movie.util.Constants.Companion.MOVIE_LANGUAGE
import com.example.movie.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.movie.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_popular.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class PopularMoviesFragment : Fragment(R.layout.fragment_popular),
    SearchMoviesAdapter.SaveMovieClicked, SearchMoviesAdapter.OnMovieItemClicked {

    private var _binding: FragmentPopularBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()
    private var popularMoviesAdapter = SearchMoviesAdapter(this, this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPopularBinding.bind(view)

        binding.textViewTitleBackToPre.setOnClickListener {
            findNavController().popBackStack()
        }


        observerData()
        setupRecyclerView()
        handlePopularEvents()
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private fun handlePopularEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.moviesEvents.collect { event ->
                when (event) {
                    is HomeViewModel.HomeFragmentEvents.NavigateToDetailsFragment -> {
                        val action =
                            PopularMoviesFragmentDirections.actionPopularMoviesFragmentToDetailsFragment(
                                event.movie
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.HomeFragmentEvents.SaveMovieToFavorite -> {
                        viewModel.saveMovie(event.movie)
                        Snackbar.make(
                            requireView(),
                            "Movie saved successfully",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }
    }


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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getPopularMovies(MOVIE_LANGUAGE)
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
        viewModel.popularMoviesMutableList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { movieResponse ->
                        popularMoviesAdapter.differ.submitList(movieResponse.results.toList())
                        val totalPages = movieResponse.total_results / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.moviesPage == totalPages
                        if (isLastPage) {
                            binding.popularRecyclerView.setPadding(0, 0, 0, 0)
                        }

                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "An error occured ${message}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }

            }
        })

    }

    private fun hideProgressBar() {
        binding.popularProgressBar.visibility = View.INVISIBLE
        isLoading = false

    }

    private fun showProgressBar() {
        binding.popularProgressBar.visibility = View.VISIBLE
        isLoading = true


    }

    private fun setupRecyclerView() {

        binding.popularRecyclerView.apply {
            adapter = popularMoviesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(this@PopularMoviesFragment.scrollListener)
        }
    }


    override fun onSaveMovieClicked(movie: Result) {
        viewModel.onSaveMovieClicked(movie)
    }

    override fun movieItemClicked(movie: Result) {
        viewModel.onMovieItemClicked(movie)
    }
}