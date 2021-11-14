package com.example.movie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.movie.R
import com.example.movie.models.Result
import com.example.movie.util.Constants.Companion.IMAGE_URL
import kotlinx.android.synthetic.main.movie_item.view.*

class HomeAdapter(private val listener: OnMovieItemClicked) :
    PagingDataAdapter<Result, HomeAdapter.MoviesViewHolder>(differCallback) {

    inner class MoviesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)



    companion object {
        private val differCallback = object : DiffUtil.ItemCallback<Result>() {
            override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
                return oldItem == newItem
            }
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesViewHolder {
        return MoviesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.movie_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        if (differ.currentList.size > 5) {
            return 5
        } else {

            return differ.currentList.size
        }

    }


    override fun onBindViewHolder(holder: MoviesViewHolder, position: Int) {
        val movieItem = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this)
                .load(IMAGE_URL + movieItem.poster_path)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_error)
                .into(videoPoster)
            textViewVoteItem.text = movieItem.vote_average.toString()
            movieItemMovieName.text = movieItem.title
            holder.itemView.setOnClickListener {
                listener.onItemMovieClicked(movieItem)
            }

        }
    }


    interface OnMovieItemClicked {
        fun onItemMovieClicked(movie: Result)

    }


}


