package com.joey.catmusic.data.playlist

import com.google.gson.annotations.SerializedName

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
data class Playlist(
        @SerializedName("tracks") val tracks: List<Track>,
        @SerializedName("coverImgUrl") val coverImgUrl: String,
        @SerializedName("trackNumberUpdateTime") val trackNumberUpdateTime: Long,
        @SerializedName("name") val name: String,
        @SerializedName("id") val id: Long
)