package com.joey.catmusic.data.playlist

import com.google.gson.annotations.SerializedName

data class PlaylistIds(
        @SerializedName("list") val list: MutableList<Long>
)