package com.example.movie.ui.details

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movie.R
import com.example.movie.adapter.HomeAdapter
import com.example.movie.databinding.FragmentDetailsBinding
import com.example.movie.models.Result
import com.example.movie.util.Constants
import com.example.movie.util.Constants.Companion.MOVIE_LANGUAGE
import com.example.movie.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details), HomeAdapter.OnMovieItemClicked {


    private lateinit var binding: FragmentDetailsBinding
    private val args: DetailsFragmentArgs by navArgs()
    private val viewModel by viewModels<DetailsViewModel>()
    private var moviesAdapter = HomeAdapter(this)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDetailsBinding.bind(view)
        getDetailsFromArgs()
        setupTopRecyclerView()
        observingData()

        binding.textViewTitleBackToPre.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.recommendedLoadMore.setOnClickListener {
            viewModel.onLoadMoreRecommendedClicked(args.movie)
        }
        val movie = args.movie

        binding.btnSaveMovie.setOnClickListener {
            viewModel.onSaveButtonClicked(movie)
            binding.btnSaveMovie.setBackgroundResource(R.drawable.ic_save_clicked)

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.moviesEvents.collect { event ->
                when (event) {
                    is DetailsViewModel.DetailsFragmentEvents.SaveDetailsMovie -> {
                        viewModel.saveMovie(event.movie)
                        Snackbar.make(view, "Movie saved successfully", Snackbar.LENGTH_LONG).show()
                    }

                    is DetailsViewModel.DetailsFragmentEvents.NavigateToDetailsFragment -> {
                        val action =
                            DetailsFragmentDirections.actionGlobalDetailsFragment(event.movie)
                        findNavController().navigate(action)
                    }

                    is DetailsViewModel.DetailsFragmentEvents.OnLoadMoreRecommendedClicked -> {
                        val action =
                            DetailsFragmentDirections.actionGlobalRecommendedMoviesFragment(event.movie)
                        findNavController().navigate(action)
                    }
                }
            }
        }


    }

    private fun observingData() {

        viewModel.movies.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    binding.shimmerViwe.visibility = GONE
                    response.data?.let { movieResponse ->
                        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                            try {
                                val btnShape = viewModel.changSaveButton(args.movie.id!!)
                                if (btnShape) {
                                    binding.btnSaveMovie.setBackgroundResource(R.drawable.ic_save_clicked)
                                }

                            } catch (e: Exception) {
                                Log.e("HMD", e.message.toString())
                            }

                        }

                        moviesAdapter.differ.submitList(movieResponse.results.toList())
                        binding.recommendedRecyclerView.visibility = VISIBLE


                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    binding.shimmerViwe.visibility = GONE
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "An error occured ${message}",
                            Toast.LENGTH_LONG
                        )
                            .show()

                        Log.e("HMD", message.toString())
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                    binding.shimmerViwe.visibility = VISIBLE
                }

            }
        })

        viewModel.getRecommendedMovies(MOVIE_LANGUAGE, args.movie.id!!)
    }

    private fun hideProgressBar() {
        detailsProgressBar.visibility = View.INVISIBLE

    }

    private fun showProgressBar() {
        detailsProgressBar.visibility = View.VISIBLE


    }

    private fun getDetailsFromArgs() {
        binding.detailsMovieName.text = args.movie.title
        binding.movieDetailsEng.text = args.movie.release_date
        binding.descriptionStoryLine.text = args.movie.overview
        Glide.with(requireContext()).load(Constants.IMAGE_URL + args.movie.poster_path)
            .into(binding.moviePosterDetails)

    }


    private fun setupTopRecyclerView() {
        recommendedRecyclerView.apply {
            adapter = moviesAdapter
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


                    if (isTopRatedAtLastItem) {

                        binding.recommendedLoadMore.visibility = VISIBLE

                    } else {
                        binding.recommendedLoadMore.visibility = GONE

                    }
                }
            }
            addOnScrollListener(scrollListener)

        }

    }

    override fun onItemMovieClicked(movie: Result) {
        viewModel.onMovieItemClicked(movie)

    }


}

