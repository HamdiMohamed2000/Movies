package com.example.movie.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity(
    tableName = "movies"
)
@Parcelize
data class Result(

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val adult: Boolean?,
    val backdrop_path: String?,
    //val genre_ids: List<Int>,
    val original_language: String?,
    val original_title: String?,
    val overview: String?,
    val popularity: Double?,
    val poster_path: String?,
    val release_date: String?,
    val title: String?,
    val video: Boolean?,
    val vote_average: Double?,
    val vote_count: Int?
):Parcelable