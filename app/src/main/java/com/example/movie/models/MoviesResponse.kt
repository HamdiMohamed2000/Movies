package com.example.movie.models

data class MoviesResponse(
    val page: Int,
    var results: MutableList<Result>,
    val total_pages: Int,
    val total_results: Int
)