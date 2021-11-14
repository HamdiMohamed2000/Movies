package com.example.movie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movie.databinding.MovieAdapterFooterBinding

class FooterAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<FooterAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = MovieAdapterFooterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadStateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }


    inner class LoadStateViewHolder(private val binding: MovieAdapterFooterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.buttonLoadMore.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                buttonLoadMore.isVisible = loadState is LoadState.Loading
            }
            }
        }
    }



