package com.example.movie.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.movie.R
import com.example.movie.adapter.HomeAdapter
import com.example.movie.databinding.FragmentHomeBinding
import com.example.movie.models.Result
import com.example.movie.util.Constants.Companion.IMAGE_URL
import com.example.movie.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), HomeAdapter.OnMovieItemClicked {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()

    private var popularMoviesAdapter = HomeAdapter(this)
    private var topRatedMoviesAdapter = HomeAdapter(this)
    private var upcomingMoviesAdapter = HomeAdapter(this)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        observingData()
        homeEvents()
        loadMoreButtons()


    }


    private fun setupRecyclerView() {

        popularRecyclerView.apply {
            adapter = popularMoviesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

            val scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val isPopularAtLastItem =
                        firstVisibleItemPosition + visibleItemCount >= totalItemCount


                    if (binding.popularRecyclerView.isLaidOut == isPopularAtLastItem) {

                        binding.popularLoadMore.visibility = VISIBLE

                    } else {
                        binding.popularLoadMore.visibility = GONE

                    }
                }
            }

            addOnScrollListener(scrollListener)

        }

        TopRatedMoviesRecyclerView.apply {
            adapter = topRatedMoviesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

            val scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val isTopRatedAtLastItem =
                        firstVisibleItemPosition + visibleItemCount >= totalItemCount


                    if (binding.TopRatedMoviesRecyclerView.isLaidOut == isTopRatedAtLastItem) {

                        binding.topRatedLoadMore.visibility = VISIBLE

                    } else {
                        binding.topRatedLoadMore.visibility = GONE

                    }
                }
            }
            addOnScrollListener(scrollListener)

        }

        upcomingMoviesRecyclerView.apply {
            adapter = upcomingMoviesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            val scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val isUpcomingAtLastItem =
                        firstVisibleItemPosition + visibleItemCount >= totalItemCount


                    if (binding.upcomingMoviesRecyclerView.isLaidOut == isUpcomingAtLastItem) {

                        binding.upComingLoadMore.visibility = VISIBLE

                    } else {
                        binding.upComingLoadMore.visibility = GONE

                    }
                }
            }


            addOnScrollListener(scrollListener)

        }


    }

    private fun observingData() {
        viewModel.popularMoviesMutableList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.popularRecyclerView.visibility = VISIBLE
                    binding.shimmerViwe.visibility = GONE
                    response.data?.let { movieResponse ->
                        popularMoviesAdapter.differ.submitList(movieResponse.results.toList())

                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "An error occured ${message}",
                            Toast.LENGTH_LONG
                        )
                            .show()

                        binding.shimmerViwe.visibility = VISIBLE


                    }
                }
                is Resource.Loading -> {
                    binding.shimmerViwe.visibility = VISIBLE
                }

            }
        })

        viewModel.topRatesMoviesMutableList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.topRatedshimmerViwe.visibility = GONE

                    response.data?.let { movieResponse ->
                        topRatedMoviesAdapter.differ.submitList(movieResponse.results.toList())
                        binding.TopRatedMoviesRecyclerView.visibility = VISIBLE

                    }

                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "An error occured ${message.toString()}",
                            Toast.LENGTH_LONG
                        )

                            .show()
                        topRatedshimmerViwe.visibility = VISIBLE

                    }
                }
                is Resource.Loading -> {
                    topRatedshimmerViwe.visibility = VISIBLE
                }

            }
        })

        viewModel.upcomingMoviesMutableList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.upcomingshimmerViwe.visibility = GONE
                    response.data?.let { movieResponse ->
                        upcomingMoviesAdapter.differ.submitList(movieResponse.results.toList())
                        binding.upcomingMoviesRecyclerView.visibility = VISIBLE

                    }
                }

                is Resource.Error -> {
                    binding.upcomingshimmerViwe.visibility = GONE
                    binding.upcomingMoviesRecyclerView.visibility = VISIBLE
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "An error occured ${message.toString()}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        binding.upcomingshimmerViwe.visibility = VISIBLE
                    }
                }
                is Resource.Loading -> {
                    binding.upcomingshimmerViwe.visibility = VISIBLE


                }

            }
        })

        viewModel.moviePlayNowMutableList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.shimmerViwePoster.visibility = GONE

                    response.data?.let { movieResponse ->
                        Glide.with(this)
                            .load(IMAGE_URL + movieResponse.results[0].poster_path)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.ic_error)
                            .into(binding.moviePlayNowPoster)
                        binding.homePlayNowVote.text =
                            movieResponse.results[0].vote_average.toString()
                        binding.posterMovieName.text = movieResponse.results[0].title
                        binding.movieHomePosterCard.visibility = VISIBLE


                    }
                }
                is Resource.Error -> {
                    binding.shimmerViwePoster.visibility = GONE
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "An error occured ${message}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        binding.shimmerViwePoster.visibility = VISIBLE
                    }
                }
                is Resource.Loading -> {
                    binding.shimmerViwePoster.visibility = VISIBLE

                }

            }
        })


    }

    private fun homeEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.moviesEvents.collect { event ->
                when (event) {
                    is HomeViewModel.HomeFragmentEvents.NavigateToDetailsFragment -> {
                        val action =
                            HomeFragmentDirections.actionGlobalDetailsFragment(event.movie)
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.HomeFragmentEvents.OnLoadMorePopularClicked -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToPopularMoviesFragment()
                        findNavController().navigate(action)
                    }

                    is HomeViewModel.HomeFragmentEvents.OnLoadMoreTopRatedClicked -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToTopRatedMoviesFragment()
                        findNavController().navigate(action)
                    }

                    is HomeViewModel.HomeFragmentEvents.OnLoadMoreUpcomingClicked -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToUpcomingMoviesFragment()
                        findNavController().navigate(action)
                    }
                }

            }
        }
    }

    private fun loadMoreButtons() {
        binding.popularLoadMore.setOnClickListener {
            viewModel.onBtnPopularLoadMoreClicked()
        }

        binding.topRatedLoadMore.setOnClickListener {
            viewModel.onBtnTopRatedLoadMoreClicked()
        }

        binding.upComingLoadMore.setOnClickListener {
            viewModel.onBtnUpcomingLoadMoreClicked()
        }
    }

    override fun onItemMovieClicked(movie: Result) {
        viewModel.onMovieItemClicked(movie)
    }


}



