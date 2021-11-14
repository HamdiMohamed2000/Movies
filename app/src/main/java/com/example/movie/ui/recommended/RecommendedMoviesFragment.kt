package com.example.movie.ui.recommended

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie.R
import com.example.movie.adapter.SearchMoviesAdapter
import com.example.movie.databinding.FragmentPopularBinding
import com.example.movie.databinding.FragmentRecommendedBinding
import com.example.movie.models.Result
import com.example.movie.ui.details.DetailsFragmentArgs
import com.example.movie.ui.details.DetailsViewModel
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
class RecommendedMoviesFragment : Fragment(R.layout.fragment_recommended),
    SearchMoviesAdapter.SaveMovieClicked, SearchMoviesAdapter.OnMovieItemClicked {

    private var _binding: FragmentRecommendedBinding? = null
    private val binding get() = _binding!!
    private val args: RecommendedMoviesFragmentArgs by navArgs()
    private val viewModel by viewModels<DetailsViewModel>()
    private var popularMoviesAdapter = SearchMoviesAdapter(this, this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRecommendedBinding.bind(view)

        binding.textViewTitleBackToPre.setOnClickListener {
            findNavController().popBackStack()
        }



        observerData()
        setupRecyclerView()
        handleRecommendedEvents()
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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getRecommendedMovies(MOVIE_LANGUAGE, args.recommended.id!!)
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
        viewModel.movies.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { movieResponse ->
                        popularMoviesAdapter.differ.submitList(movieResponse.results.toList())

                        val totalPages = movieResponse.total_results / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.moviesPage == totalPages
                        if (isLastPage) {
                            binding.recommendedRecyclerView.setPadding(0, 0, 0, 0)
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

        viewModel.getRecommendedMovies(MOVIE_LANGUAGE, args.recommended.id!!)

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

        binding.recommendedRecyclerView.apply {
            adapter = popularMoviesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(this@RecommendedMoviesFragment.scrollListener)
        }
    }

    private fun handleRecommendedEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.moviesEvents.collect { event ->
                when (event) {
                    is DetailsViewModel.DetailsFragmentEvents.NavigateToDetailsFragment -> {
                        val action =
                            RecommendedMoviesFragmentDirections.actionRecommendedMoviesFragmentToDetailsFragment(
                                event.movie
                            )
                        findNavController().navigate(action)
                    }
                    is DetailsViewModel.DetailsFragmentEvents.SaveDetailsMovie -> {
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


    override fun onSaveMovieClicked(movie: Result) {
        viewModel.onSaveButtonClicked(movie)
    }

    override fun movieItemClicked(movie: Result) {
        viewModel.onMovieItemClicked(movie)
    }
}