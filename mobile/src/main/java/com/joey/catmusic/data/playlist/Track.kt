package com.joey.catmusic.data.playlist

import com.google.gson.annotations.SerializedName

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
data class Track(
        @SerializedName("name") val name: String,
        @SerializedName("id") val id: Int,
        @SerializedName("al") val al: Album,
        @SerializedName("publishTime") val publishTime: Long
)


