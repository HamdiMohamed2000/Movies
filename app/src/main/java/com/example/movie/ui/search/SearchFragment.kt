package com.example.movie.ui.search

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie.R
import com.example.movie.adapter.SearchMoviesAdapter
import com.example.movie.databinding.FragmentSearchBinding
import com.example.movie.models.Result
import com.example.movie.util.Constants.Companion.MOVIE_LANGUAGE
import com.example.movie.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.movie.util.Constants.Companion.SEARCH_Movies_TIME_DELAY
import com.example.movie.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_popular.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search), SearchMoviesAdapter.SaveMovieClicked,
    SearchMoviesAdapter.OnMovieItemClicked {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SearchViewModel>()
    private val searchedAdapter = SearchMoviesAdapter(this, this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        setupRecyclerView()
        observerData()
        handleSearchEvents()

        var job: Job? = null
        binding.searchEditText.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_Movies_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.getSearchMovies(editable.toString())
                    }
                }
            }
        }

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
                viewModel.getSearchMovies(MOVIE_LANGUAGE)
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
        viewModel.searchedMoviesMutableList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { movieResponse ->
                        searchedAdapter.differ.submitList(movieResponse.results.toList())
                        val totalPages = movieResponse.total_results / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.moviesPage == totalPages
                        if (isLastPage) {
                            binding.searchRecyclerView.setPadding(0, 0, 0, 0)
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

    private fun hideProgressBar() {
        binding.searchProgressBar.visibility = View.INVISIBLE
        isLoading = false

    }

    private fun handleSearchEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchEvents.collect { event ->

                when (event) {
                    is SearchViewModel.SearchFragmentEvents.SaveMovieToFavorite -> {
                        viewModel.saveMovie(event.movie)
                        Snackbar.make(
                            requireView(),
                            "Movie saved successfully",
                            Snackbar.LENGTH_LONG
                        ).show()

                    }

                    is SearchViewModel.SearchFragmentEvents.NavigateToDetails -> {
                        val action =
                            SearchFragmentDirections.actionSearchFragmentToDetailsFragment(event.movie)
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun showProgressBar() {
        binding.searchProgressBar.visibility = View.VISIBLE

        isLoading = true

    }

    private fun setupRecyclerView() {

        binding.searchRecyclerView.apply {
            adapter = searchedAdapter
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

    override fun onSaveMovieClicked(movie: Result) {
        viewModel.onSaveMovieClicked(movie)
    }

    override fun movieItemClicked(movie: Result) {
        viewModel.onMovieItemClicked(movie)
    }

}
